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
 * This class represents the sequence flows in a BPMN process between BPMN
 * elements.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNSequenceFlow")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNSequenceFlow extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;
	private String sourceRef;
	private String targetRef;

	public BPMNSequenceFlow() {
		super();
	}

	public BPMNSequenceFlow(final String ID, final String name, final String extractSourceRef,
			final String extractTargetRef) {
		super(ID, name);
		this.sourceRef = extractSourceRef;
		this.targetRef = extractTargetRef;
	}

	@Override
	public boolean isSequenceFlow() {
		return true;
	}

	public String getSourceRef() {
		return this.sourceRef;
	}

	public void setSourceRef(final String sourceRef) {
		this.sourceRef = sourceRef;
	}

	public String getTargetRef() {
		return this.targetRef;
	}

	public void setTargetRef(final String targetRef) {
		this.targetRef = targetRef;
	}

	public BPMNSequenceFlow(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

	@Override
	public String toString() {
		return "SequenceFlow from " + this.sourceRef + " to " + this.targetRef;
	}

}
