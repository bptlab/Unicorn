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
 * This class represents the events in a BPMN process, which are attached to
 * another BPMN element.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNBoundaryEvent")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNBoundaryEvent extends BPMNIntermediateEvent {

	private static final long serialVersionUID = 1L;
	private boolean isCancelActivity = false;

	private AbstractBPMNElement attachedToElement = null;

	public BPMNBoundaryEvent() {
		super();
	}

	public BPMNBoundaryEvent(final String ID, final String name, final List<MonitoringPoint> monitoringPoints,
			final BPMNEventType intermediateEventType) {
		super(ID, name, monitoringPoints, intermediateEventType);
	}

	public boolean isCancelActivity() {
		return this.isCancelActivity;
	}

	public void setCancelActivity(final boolean isCancelActivity) {
		this.isCancelActivity = isCancelActivity;
	}

	public AbstractBPMNElement getAttachedToElement() {
		return this.attachedToElement;
	}

	public void setAttachedToElement(final AbstractBPMNElement attachedToElement) {
		this.attachedToElement = attachedToElement;
	}

	@Override
	public boolean isBoundaryEvent() {
		return true;
	}

	public String print() {
		if (this.isCancelActivity()) {
			return "cancelling Boundary event";
		}
		return "Boundary Event";
	}

}
