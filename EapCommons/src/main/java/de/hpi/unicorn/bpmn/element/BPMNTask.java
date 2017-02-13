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
 * This class represents an activity in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNTask")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNTask extends AbstractBPMNElement implements AttachableElement {

	private static final long serialVersionUID = 1L;
	private BPMNBoundaryEvent attachedIntermediateEvent;

	public BPMNTask() {
		super();
	}

	public BPMNTask(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

	public String print() {
		return "Task: " + this.getName();
	}

	@Override
	public boolean hasAttachedIntermediateEvent() {
		return this.attachedIntermediateEvent != null;
	}

	@Override
	public BPMNBoundaryEvent getAttachedIntermediateEvent() {
		return this.attachedIntermediateEvent;
	}

	@Override
	public void setAttachedIntermediateEvent(final BPMNBoundaryEvent attachedEvent) {
		this.attachedIntermediateEvent = attachedEvent;
	}

}
