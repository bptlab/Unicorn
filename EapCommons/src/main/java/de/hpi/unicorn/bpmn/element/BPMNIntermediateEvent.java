/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.element;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;

/**
 * This class represents an intermediate event in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNIntermediateCatchEvent")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNIntermediateEvent extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;
	protected float timeDuration;

	@Column(name = "intermediateEventType")
	private BPMNEventType intermediateEventType;

	@Column(name = "isCatchEvent")
	private boolean isCatchEvent;

	public BPMNIntermediateEvent() {
		super();
	}

	public BPMNIntermediateEvent(final String ID, final String name, final List<MonitoringPoint> monitoringPoints,
			final BPMNEventType intermediateEventType) {
		super(ID, name, monitoringPoints);
		this.intermediateEventType = intermediateEventType;
	}

	public BPMNEventType getIntermediateEventType() {
		return this.intermediateEventType;
	}

	public void setIntermediateEventType(final BPMNEventType intermediateEventType) {
		this.intermediateEventType = intermediateEventType;
	}

	public boolean isCatchEvent() {
		return this.isCatchEvent;
	}

	public void setCatchEvent(final boolean isCatchEvent) {
		this.isCatchEvent = isCatchEvent;
	}

	public float getTimeDuration() {
		return this.timeDuration;
	}

	public void setTimeDuration(final float duration) {
		this.timeDuration = duration;
	}

}
