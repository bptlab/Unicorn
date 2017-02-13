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
 * This class is a logical representation for a BPMN gateway element.
 * 
 * @author micha
 */
@Entity
@Table(name = "AbstractBPMNGateway")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class AbstractBPMNGateway extends AbstractBPMNElement {

	private static final long serialVersionUID = 1L;

	public AbstractBPMNGateway() {
		super();
	}

	public AbstractBPMNGateway(final String ID, final String name) {
		super(ID, name);
	}

	public AbstractBPMNGateway(final String ID, final String name, final List<MonitoringPoint> monitoringPoints) {
		super(ID, name);
	}

	/**
	 * Proofs, if a gateway is a joining gateway.
	 * 
	 * @return
	 */
	public boolean isJoinGateway() {
		return (this.getPredecessors().size() > 1 && this.getSuccessors().size() == 1);
	}

	/**
	 * Proofs, if a gateway is a split gateway.
	 * 
	 * @return
	 */
	public boolean isSplitGateway() {
		return (this.getPredecessors().size() == 1 && this.getSuccessors().size() > 1);
	}

}
