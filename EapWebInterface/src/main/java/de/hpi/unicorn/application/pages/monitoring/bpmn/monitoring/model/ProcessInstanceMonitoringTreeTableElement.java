/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;
import de.hpi.unicorn.query.PatternQuery;

/**
 * Representation of a tree node of the process instance treetable. Each element
 * contains a {@link PatternQuery} and associated informations for these query.
 * 
 * @param <T>
 *            type of content to be stored
 */
public class ProcessInstanceMonitoringTreeTableElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ID;
	private PatternQuery query;
	private ProcessInstanceMonitoringTreeTableElement parent;
	private final Set<ProcessInstanceMonitoringTreeTableElement> children = new HashSet<ProcessInstanceMonitoringTreeTableElement>();
	private final List<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();
	private Set<AbstractBPMNElement> monitoredElements;
	private final ProcessInstanceMonitor processInstanceMonitor;
	private final String startTime;
	private final String endTime;

	/**
	 * creates a root node
	 * 
	 * @param processInstanceMonitor
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public ProcessInstanceMonitoringTreeTableElement(final int ID, final PatternQuery query,
			final ProcessInstanceMonitor processInstanceMonitor) {
		this.ID = ID;
		this.query = query;
		this.processInstanceMonitor = processInstanceMonitor;
		this.monitoredElements = new HashSet<AbstractBPMNElement>(query.getMonitoredElements());
		this.startTime = processInstanceMonitor.getStartTimeForQuery(query).toString();
		this.endTime = processInstanceMonitor.getEndTimeForQuery(query).toString();
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public ProcessInstanceMonitoringTreeTableElement(final ProcessInstanceMonitoringTreeTableElement parent,
			final int ID, final PatternQuery query, final ProcessInstanceMonitor processInstanceMonitor) {
		this(ID, query, processInstanceMonitor);
		this.parent = parent;
		this.parent.getChildren().add(this);
	}

	public Integer getID() {
		return this.ID;
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public PatternQuery getContent() {
		return this.query;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public ProcessInstanceMonitoringTreeTableElement getParent() {
		return this.parent;
	}

	public Set<ProcessInstanceMonitoringTreeTableElement> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		if (this.query == null) {
			return new String();
		}
		return this.query.toString();
	}

	public void remove() {
		if (this.parent != null) {
			this.parent.getChildren().remove(this);
		}
		// Müssen Kinder noch explizit entfernt werden?
	}

	public void setParent(final ProcessInstanceMonitoringTreeTableElement parent) {
		this.parent = parent;
		if (parent != null) {
			this.parent.getChildren().add(this);
		}
	}

	public boolean hasMonitoringPoints() {
		return !this.monitoringPoints.isEmpty();
	}

	public void addMonitoringPoint(final MonitoringPoint monitoringPoint) {
		if (this.getMonitoringPoint(monitoringPoint.getStateTransitionType()) != null) {
			this.monitoringPoints.remove(this.getMonitoringPoint(monitoringPoint.getStateTransitionType()));
		}
		this.monitoringPoints.add(monitoringPoint);
	}

	public void addMonitoringPoints(final List<MonitoringPoint> monitoringPoints) {
		for (final MonitoringPoint monitoringPoint : monitoringPoints) {
			this.addMonitoringPoint(monitoringPoint);
		}
	}

	public MonitoringPoint getMonitoringPoint(final MonitoringPointStateTransition type) {
		for (final MonitoringPoint monitoringPoint : this.monitoringPoints) {
			if (monitoringPoint.getStateTransitionType().equals(type)) {
				return monitoringPoint;
			}
		}
		return null;
	}

	public boolean hasMonitoringPoint(final MonitoringPointStateTransition type) {
		return this.getMonitoringPoint(type) != null;
	}

	public Set<AbstractBPMNElement> getMonitoredElements() {
		return this.monitoredElements;
	}

	public void setMonitoredElements(final Set<AbstractBPMNElement> monitoredElements) {
		this.monitoredElements = monitoredElements;
	}

	public PatternQuery getQuery() {
		return this.query;
	}

	public void setQuery(final PatternQuery query) {
		this.query = query;
	}

	public ProcessInstanceMonitor getProcessInstanceMonitor() {
		return this.processInstanceMonitor;
	}

	public String getStartTime() {
		return this.startTime;
	}

	public String getEndTime() {
		return this.endTime;
	}

}
