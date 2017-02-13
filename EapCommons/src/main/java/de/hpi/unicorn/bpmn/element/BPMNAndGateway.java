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
 * This class represents the parallel gateways in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNAndGateway")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNAndGateway extends AbstractBPMNGateway {

	private static final long serialVersionUID = 1L;

	public BPMNAndGateway() {
		super();
	}

	public BPMNAndGateway(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name, monitoringPoints);
	}

}
