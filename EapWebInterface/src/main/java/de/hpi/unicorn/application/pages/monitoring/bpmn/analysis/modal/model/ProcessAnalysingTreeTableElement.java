/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;
import de.hpi.unicorn.query.PatternQuery;

/**
 * Representation of a tree node of the process analysing treetable. Each
 * element contains a {@link PatternQuery} and associated informations for these
 * query.
 * 
 * @param <T>
 *            type of content to be stored
 */
public class ProcessAnalysingTreeTableElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ID;
	private PatternQuery query;
	private ProcessAnalysingTreeTableElement parent;
	private final Set<ProcessAnalysingTreeTableElement> children = new HashSet<ProcessAnalysingTreeTableElement>();
	private final List<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();
	private Set<AbstractBPMNElement> monitoredElements;
	private final ProcessMonitor processMonitor;
	private final float averageRuntime;
	private final String pathFrequency;

	/**
	 * creates a root node
	 * 
	 * @param processMonitor
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public ProcessAnalysingTreeTableElement(final int ID, final PatternQuery query, final ProcessMonitor processMonitor) {
		this.ID = ID;
		this.query = query;
		this.processMonitor = processMonitor;
		this.monitoredElements = new HashSet<AbstractBPMNElement>(query.getMonitoredElements());
		this.averageRuntime = processMonitor.getAverageRuntimeForQuery(query);
		this.pathFrequency = NumberFormat.getPercentInstance().format(processMonitor.getPathFrequencyForQuery(query));
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public ProcessAnalysingTreeTableElement(final ProcessAnalysingTreeTableElement parent, final int ID,
			final PatternQuery query, final ProcessMonitor processMonitor) {
		this(ID, query, processMonitor);
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

	public ProcessAnalysingTreeTableElement getParent() {
		return this.parent;
	}

	public Set<ProcessAnalysingTreeTableElement> getChildren() {
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

	public void setParent(final ProcessAnalysingTreeTableElement parent) {
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

	public ProcessMonitor getProcessMonitor() {
		return this.processMonitor;
	}

	public float getAverageRuntime() {
		return this.averageRuntime;
	}

	public String getPathFrequency() {
		return this.pathFrequency;
	}

}
