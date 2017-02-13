/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;

/**
 * This class represents the start events in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNStartEvent")
@Inheritance(strategy = InheritanceType.JOINED)
// @DiscriminatorValue("BPMNStartEvent")
public class BPMNStartEvent extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;
	private BPMNEventType startEventType;

	public BPMNStartEvent() {
		super();
	}

	public BPMNStartEvent(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
		this.startEventType = BPMNEventType.Blank;
	}

	public BPMNStartEvent(final String ID, final String name, final List<MonitoringPoint> monitoringPoints,
			final BPMNEventType startEventType) {
		super(ID, name, monitoringPoints);
		this.startEventType = startEventType;
	}

	public String print() {
		return "StartEvent";
	}

	public BPMNEventType getStartEventType() {
		return this.startEventType;
	}

	public void setStartEventType(final BPMNEventType startEventType) {
		this.startEventType = startEventType;
	}

}
