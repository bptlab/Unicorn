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
 * This class represents the end events in a BPMN process.
 * 
 * @author micha
 * 
 */
@Entity
@Table(name = "BPMNEndEvent")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNEndEvent extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;

	public BPMNEndEvent() {
		super();
	}

	public BPMNEndEvent(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

	public String print() {
		return "EndEvent";
	}

}
