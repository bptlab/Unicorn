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
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;

/**
 * @author micha
 */
public class ProcessInstanceMonitor implements Serializable {

	private static final long serialVersionUID = 1L;
	private final int ID;
	private final CorrelationProcessInstance processInstance;
	// TODO: Tree der QueryMonitors
	private final List<QueryMonitor> queryMonitors;
	private ProcessInstanceStatus status;
	private Date startTime;
	private Date endTime;
	private final ViolationMonitor violationMonitor;

	public ProcessInstanceMonitor(final CorrelationProcessInstance processInstance) {
		this.processInstance = processInstance;
		this.ID = processInstance.getID();
		this.queryMonitors = new ArrayList<QueryMonitor>();
		this.violationMonitor = new ViolationMonitor(this);
		this.refreshStatus();
	}

	public void addQuery(final PatternQuery query) {
		this.queryMonitors.add(new QueryMonitor(query, QueryStatus.Started, this.processInstance));
		this.refreshStatus();
	}

	public void addQueries(final Set<PatternQuery> queries) {
		for (final PatternQuery query : queries) {
			this.addQuery(query);
		}
	}

	public CorrelationProcessInstance getProcessInstance() {
		return this.processInstance;
	}

	public void setQueryFinished(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			this.findQueryMonitorByQuery(query).setQueryStatus(QueryStatus.Finished);
		}
		this.refreshStatus();
	}

	public void setQuerySkipped(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			this.findQueryMonitorByQuery(query).setQueryStatus(QueryStatus.Skipped);
		}
		this.refreshStatus();
	}

	public ProcessInstanceStatus getStatus() {
		return this.status;
	}

	public void setStatus(final ProcessInstanceStatus processInstanceStatus) {
		this.status = processInstanceStatus;
	}

	private boolean allQueriesTerminated() {
		int executionCount = 0;
		if (!this.queryMonitors.isEmpty()) {
			executionCount = this.queryMonitors.get(0).getExecutionCount();
		}
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.isRunning() && queryMonitor.getExecutionCount() == executionCount) {
				return false;
			}
		}
		return true;
	}

	private ProcessInstanceStatus refreshStatus() {
		this.startTime = this.calculateStartTime();
		this.endTime = this.calculateEndTime();
		if (this.processInstance == null) {
			this.status = ProcessInstanceStatus.NotExisting;
		} else {
			this.adaptQueryStatus();
			if (this.allQueriesTerminated()) {
				this.status = ProcessInstanceStatus.Finished;
			} else {
				this.status = ProcessInstanceStatus.Running;
			}
		}
		return this.status;
	}

	/**
	 * Searches for queries for the given process instance, that could be set
	 * started, skipped or finished.
	 */
	private void adaptQueryStatus() {
		for (final PatternQuery query : this.getQueriesWithStatus(QueryStatus.Finished)) {
			// XORQuery finished, SubQueries mit Running auf Skipped setzten
			if (query.getPatternQueryType().equals(PatternQueryType.XOR)) {
				this.skipStartedSubQueries(query);
			}
		}

		this.violationMonitor.searchForViolations();
	}

	private QueryMonitor getRootQueryMonitor() {
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQuery().getParentQuery() == null) {
				return queryMonitor;
			}
		}
		return null;
	}

	List<QueryMonitor> getSubQueryMonitors(final QueryMonitor queryMonitor) {
		final Set<PatternQuery> childQueries = queryMonitor.getQuery().getChildQueries();
		final List<QueryMonitor> subQueryMonitors = new ArrayList<QueryMonitor>();
		for (final PatternQuery childQuery : childQueries) {
			subQueryMonitors.add(this.getQueryMonitorForQuery(childQuery));
		}
		return subQueryMonitors;
	}

	QueryMonitor getQueryMonitorForQuery(final PatternQuery query) {
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQuery().equals(query)) {
				return queryMonitor;
			}
		}
		return null;
	}

	public List<QueryMonitor> getQueryMonitorsWithStatus(final QueryStatus status) {
		final List<QueryMonitor> queryMonitorsWithStatus = new ArrayList<QueryMonitor>();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQueryStatus().equals(status)) {
				queryMonitorsWithStatus.add(queryMonitor);
			}
		}
		return queryMonitorsWithStatus;
	}

	/**
	 * Searches recursively for child queries with {@link QueryStatus} Started
	 * and sets their {@link QueryStatus} to Skipped.
	 * 
	 * @param parentQuery
	 */
	private void skipStartedSubQueries(final PatternQuery parentQuery) {
		for (final PatternQuery query : parentQuery.getChildQueries()) {
			if (this.getStatusForQuery(query).equals(QueryStatus.Started)) {
				this.setStatusForQuery(query, QueryStatus.Skipped);
				if (query.hasChildQueries()) {
					this.skipStartedSubQueries(query);
				}
			}
		}
	}

	public int getID() {
		return this.ID;
	}

	/**
	 * Returns all {@link PatternQuery} from the contained {@link QueryMonitor}
	 * s.
	 * 
	 * @param queryStatus
	 * @return
	 */
	public Set<PatternQuery> getQueries() {
		final Set<PatternQuery> queries = new HashSet<PatternQuery>();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			queries.add(queryMonitor.getQuery());
		}
		return queries;
	}

	/**
	 * Returns all {@link PatternQuery} from the contained {@link QueryMonitor}
	 * s, which have the specified {@link QueryStatus}.
	 * 
	 * @param queryStatus
	 * @return
	 */
	public Set<PatternQuery> getQueriesWithStatus(final QueryStatus queryStatus) {
		final Set<PatternQuery> queries = new HashSet<PatternQuery>();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQueryStatus().equals(queryStatus)) {
				queries.add(queryMonitor.getQuery());
			}
		}
		return queries;
	}

	/**
	 * Returns all {@link QueryMonitor}, which have the specified
	 * {@link PatternQueryType}.
	 * 
	 * @param queryStatus
	 * @return
	 */
	public Set<QueryMonitor> getQueryMonitorsWithQueryType(final PatternQueryType patternQueryType) {
		final Set<QueryMonitor> queryMonitorsWithPatternQueryType = new HashSet<QueryMonitor>();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQuery().getPatternQueryType().equals(patternQueryType)) {
				queryMonitorsWithPatternQueryType.add(queryMonitor);
			}
		}
		return queryMonitorsWithPatternQueryType;
	}

	/**
	 * Returns all {@link QueryMonitor}, which have the specified monitored
	 * {@link AbstractBPMNElement}.
	 * 
	 * @param queryStatus
	 * @return
	 */
	public Set<QueryMonitor> getQueryMonitorsWithMonitoredElements(
			final Collection<AbstractBPMNElement> monitoredElements) {
		final Set<QueryMonitor> queryMonitorsWithMonitoredElements = new HashSet<QueryMonitor>();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			final List<AbstractBPMNElement> queryMonitoredElements = queryMonitor.getQuery().getMonitoredElements();
			if (queryMonitoredElements.containsAll(monitoredElements)
					&& monitoredElements.containsAll(queryMonitoredElements)) {
				queryMonitorsWithMonitoredElements.add(queryMonitor);
			}
		}
		return queryMonitorsWithMonitoredElements;
	}

	public QueryStatus getStatusForQuery(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			return this.findQueryMonitorByQuery(query).getQueryStatus();
		} else {
			return QueryStatus.NotExisting;
		}
	}

	public Set<ViolationStatus> getViolationStatusForQuery(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			return this.findQueryMonitorByQuery(query).getViolationStatus();
		} else {
			return null;
		}
	}

	public void setStatusForQuery(final PatternQuery query, final QueryStatus queryStatus) {
		final QueryMonitor queryMonitor = this.findQueryMonitorByQuery(query);
		if (queryMonitor != null) {
			queryMonitor.setQueryStatus(queryStatus);
		}
	}

	private QueryMonitor findQueryMonitorByQuery(final PatternQuery query) {
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getQuery().equals(query)) {
				return queryMonitor;
			}
		}
		return null;
	}

	public Date getStartTime() {
		return (this.startTime != null) ? this.startTime : new Date();
	}

	public Date getEndTime() {
		return (this.endTime != null) ? this.endTime : new Date();
	}

	private Date calculateStartTime() {
		Date startTime = new Date();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getStartTime().before(startTime)) {
				startTime = queryMonitor.getStartTime();
			}
		}
		return startTime;
	}

	private Date calculateEndTime() {
		Date endTime = this.calculateStartTime();
		for (final QueryMonitor queryMonitor : this.queryMonitors) {
			if (queryMonitor.getEndTime().after(endTime)) {
				endTime = queryMonitor.getStartTime();
			}
		}
		return endTime;
	}

	public Date getStartTimeForQuery(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			return this.findQueryMonitorByQuery(query).getStartTime();
		}
		return new Date();
	}

	public Date getEndTimeForQuery(final PatternQuery query) {
		if (this.findQueryMonitorByQuery(query) != null) {
			return this.findQueryMonitorByQuery(query).getEndTime();
		}
		return new Date();
	}

	public long getRuntimeForQuery(final PatternQuery query) {
		return this.getEndTimeForQuery(query).getTime() - this.getStartTimeForQuery(query).getTime();
	}

	public EventTree<DetailedQueryStatus> getDetailedStatus() {
		final EventTree<DetailedQueryStatus> detailedQueryStatusTree = new EventTree<DetailedQueryStatus>();
		final QueryMonitor rootQueryMonitor = this.getRootQueryMonitor();
		if (rootQueryMonitor != null) {
			detailedQueryStatusTree.addRootElement(rootQueryMonitor.getDetailedQueryStatus());
			this.addSubQueryMonitorsToTree(rootQueryMonitor, detailedQueryStatusTree);
		}
		return detailedQueryStatusTree;
	}

	private void addSubQueryMonitorsToTree(final QueryMonitor parentQueryMonitor,
			final EventTree<DetailedQueryStatus> detailedQueryStatusTree) {
		final List<QueryMonitor> subQueryMonitors = this.getSubQueryMonitors(parentQueryMonitor);
		if (!subQueryMonitors.isEmpty()) {
			for (final QueryMonitor queryMonitor : subQueryMonitors) {
				detailedQueryStatusTree.addChild(parentQueryMonitor.getDetailedQueryStatus(),
						queryMonitor.getDetailedQueryStatus());
				this.addSubQueryMonitorsToTree(queryMonitor, detailedQueryStatusTree);
			}
		}
	}

}
