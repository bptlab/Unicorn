/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;

/**
 * Representation of a tree node of the BPMN treetable. Each element contains a
 * {@link AbstractBPMNElement} and associated informations for these element.
 * 
 * @param <T>
 *            type of content to be stored
 */
public class BPMNTreeTableElement implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ID;
	private final AbstractBPMNElement content;
	private BPMNTreeTableElement parent;
	private final Set<BPMNTreeTableElement> children = new HashSet<BPMNTreeTableElement>();
	private final List<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();

	/**
	 * creates a root node
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public BPMNTreeTableElement(final int ID, final AbstractBPMNElement content) {
		this.ID = ID;
		this.content = content;
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public BPMNTreeTableElement(final BPMNTreeTableElement parent, final int ID, final AbstractBPMNElement content) {
		this(ID, content);
		this.parent = parent;
		this.parent.getChildren().add(this);
	}

	public Integer getID() {
		return this.ID;
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public AbstractBPMNElement getContent() {
		return this.content;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public BPMNTreeTableElement getParent() {
		return this.parent;
	}

	public Set<BPMNTreeTableElement> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		if (this.content == null) {
			return new String();
		}
		return this.content.toString();
	}

	public void remove() {
		if (this.parent != null) {
			this.parent.getChildren().remove(this);
		}
		// Müssen Kinder noch explizit entfernt werden?
	}

	public void setParent(final BPMNTreeTableElement parent) {
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

}
