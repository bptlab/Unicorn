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
 * This class represents a event-based gateway in a BPMN process.
 * 
 * @author micha
 */
@Entity
@Table(name = "BPMNEventBasedGateway")
@Inheritance(strategy = InheritanceType.JOINED)
public class BPMNEventBasedGateway extends AbstractBPMNGateway {

	private static final long serialVersionUID = 1L;

	@Column(name = "eventBasedGatewayType")
	private BPMNEventBasedGatewayType type;

	public BPMNEventBasedGateway() {
		super();
	}

	public BPMNEventBasedGateway(final String ID, final String name, final List<MonitoringPoint> monitoringPoints,
			final BPMNEventBasedGatewayType type) {
		super(ID, name, monitoringPoints);
		this.type = type;
	}

	public BPMNEventBasedGatewayType getType() {
		return this.type;
	}

	public void setType(final BPMNEventBasedGatewayType type) {
		this.type = type;
	}

}
