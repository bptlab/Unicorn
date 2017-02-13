/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.IPattern;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;

/**
 * @author micha
 */
public class QueryMonitor implements Serializable {

	private static final long serialVersionUID = 1L;
	private final PatternQuery query;
	// TODO: Map für QueryStatus: ExecutionCount --> QueryStatus
	// private List<QueryStatus> queryStatus;
	// private Set<ViolationStatus> violationStatus;
	private final CorrelationProcessInstance processInstance;
	private final Date startTime;
	private Date endTime;
	private final boolean isInLoop;
	private final List<DetailedQueryStatus> detailedQueryStatus;

	public QueryMonitor(final PatternQuery query, final QueryStatus queryStatus,
			final CorrelationProcessInstance processInstance) {
		this.query = query;
		// this.queryStatus = new ArrayList<QueryStatus>();
		// this.queryStatus.add(queryStatus);
		// this.violationStatus = new HashSet<ViolationStatus>();
		this.processInstance = processInstance;
		this.startTime = this.calculateStartTime();
		this.isInLoop = this.determineIsInLoop();
		this.detailedQueryStatus = new ArrayList<DetailedQueryStatus>();
		this.detailedQueryStatus.add(new DetailedQueryStatus(this.query, queryStatus, new HashSet<ViolationStatus>()));
	}

	public PatternQuery getQuery() {
		return this.query;
	}

	public QueryStatus getQueryStatus() {
		return this.getLastDetailedQueryStatus().getQueryStatus();
	}

	public void setQueryStatus(final QueryStatus queryStatus) {
		// TODO: Queries sollten mehrmals beendbar sein, falls sie sich in einer
		// Schleife befinden
		if (queryStatus.equals(QueryStatus.Finished) || queryStatus.equals(QueryStatus.Skipped)) {
			this.endTime = new Date();
			if (queryStatus.equals(QueryStatus.Finished) && this.detailedQueryStatus.size() > 1 && !this.isInLoop) {
				this.getLastDetailedQueryStatus().getViolationStatus().add(ViolationStatus.Duplication);
			}
		}

		if (this.getLastDetailedQueryStatus().getQueryStatus().equals(QueryStatus.Started)) {
			// Bei erstem Finishen der Query, danach ist Start nicht mehr so
			// einfach ermittelbar
			this.getLastDetailedQueryStatus().setQueryStatus(queryStatus);
		} else {
			// Falls Query nochmal fertiggestellt wird (Schleife)
			this.detailedQueryStatus.add(new DetailedQueryStatus(this.query, queryStatus,
					new HashSet<ViolationStatus>()));
		}

	}

	private Date calculateStartTime() {
		if (!this.query.getMonitoredElements().isEmpty()) {
			final AbstractBPMNElement firstElement = this.query.getMonitoredElements().get(0);
			final MonitoringPoint firstMonitoringPoint = this.getFirstMonitoringPoint(firstElement);
			if (firstMonitoringPoint != null) {
				final List<EapEvent> eventsWithMatchingEventType = EapEvent.findByEventType(firstMonitoringPoint
						.getEventType());
				for (final EapEvent event : eventsWithMatchingEventType) {
					if (event.getProcessInstances().contains(this.processInstance)) {
						return event.getTimestamp();
					}
				}
			}
		}
		return new Date();
	}

	private MonitoringPoint getFirstMonitoringPoint(final AbstractBPMNElement firstElement) {
		if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.initialize) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.initialize);
		} else if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.enable) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.enable);
		} else if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.begin) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.begin);
		} else if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.terminate) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.terminate);
		} else if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.skip) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.skip);
		} else if (firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.disrupt) != null) {
			return firstElement.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.disrupt);
		}
		return null;
	}

	public Date getStartTime() {
		return (this.startTime != null) ? this.startTime : new Date();
	}

	public Date getEndTime() {
		return (this.endTime != null) ? this.endTime : new Date();
	}

	public Set<ViolationStatus> getViolationStatus() {
		final Set<ViolationStatus> violations = new HashSet<ViolationStatus>();
		for (final DetailedQueryStatus detailedQueryStatus : this.detailedQueryStatus) {
			violations.addAll(detailedQueryStatus.getViolationStatus());
		}
		return violations;
	}

	public void setViolationStatus(final Set<ViolationStatus> violationStatus) {
		this.getLastDetailedQueryStatus().setViolationStatus(violationStatus);
	}

	public void addViolationStatus(final ViolationStatus violationStatus) {
		this.getLastDetailedQueryStatus().getViolationStatus().add(violationStatus);
	}

	public void setEndTime(final Date endTime) {
		this.endTime = endTime;
	}

	/**
	 * Returns, how often the query was triggered as finished or skipped.
	 * 
	 * @return
	 */
	public int getExecutionCount() {
		return this.detailedQueryStatus.size();
	}

	/**
	 * Returns true, if the monitored elements of this queries are contained in
	 * a loop.
	 * 
	 * @return
	 */
	public boolean isInLoop() {
		return this.isInLoop;
	}

	private boolean determineIsInLoop() {
		if (this.query.getPatternQueryType().equals(PatternQueryType.STATETRANSITION)) {
			// Query sollte hier nur ein monitored Element enthalten
			if (!this.query.getMonitoredElements().isEmpty()) {
				final AbstractBPMNElement monitoredElement = this.query.getMonitoredElements().get(0);
				return monitoredElement.getIndirectSuccessors().contains(monitoredElement);
			}
		} else {
			final EventTree<AbstractBPMNElement> processDecompositionTree = this.processInstance.getProcess()
					.getProcessDecompositionTree();
			if (processDecompositionTree != null) {
				final Set<AbstractBPMNElement> indirectParents = processDecompositionTree.getIndirectParents(this.query
						.getMonitoredElements());
				for (final AbstractBPMNElement parent : indirectParents) {
					if (parent instanceof Component && ((Component) parent).getType().equals(IPattern.LOOP)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Returns true, if the {@link QueryStatus} is Finished or Skipped.
	 * 
	 * @return
	 */
	public boolean isTerminated() {
		final QueryStatus currentStatus = this.getLastDetailedQueryStatus().getQueryStatus();
		return currentStatus.equals(QueryStatus.Finished) || currentStatus.equals(QueryStatus.Skipped)
				|| currentStatus.equals(QueryStatus.Aborted);
	}

	/**
	 * Returns true, if the {@link QueryStatus} is Finished.
	 * 
	 * @return
	 */
	public boolean isFinished() {
		return this.getLastDetailedQueryStatus().getQueryStatus().equals(QueryStatus.Finished);
	}

	public DetailedQueryStatus getDetailedQueryStatus() {
		return this.getLastDetailedQueryStatus();
	}

	private DetailedQueryStatus getLastDetailedQueryStatus() {
		return this.detailedQueryStatus.get(this.detailedQueryStatus.size() - 1);
	}

	public boolean isRunning() {
		return this.getLastDetailedQueryStatus().getQueryStatus().equals(QueryStatus.Started);
	}

}
