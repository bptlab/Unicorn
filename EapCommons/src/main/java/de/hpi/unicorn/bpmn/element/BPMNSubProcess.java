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
 * This class represents a sub process in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNSubProcess")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNSubProcess extends BPMNProcess implements AttachableElement {

	private static final long serialVersionUID = 1L;
	private BPMNBoundaryEvent attachedIntermediateEvent;

	public BPMNSubProcess() {
		super();
	}

	public BPMNSubProcess(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

	@Override
	public boolean isProcess() {
		return true;
	}

	public String print() {
		return "SubProcess";
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
