/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.monitoringpoint;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * A monitoring point is a binding between a monitorable
 * {@link AbstractBPMNElement} and a {@link EapEventType} to monitor the
 * execution of a BPMN process with Esper.
 * 
 * @author micha
 */
@Entity
@Table(name = "MonitoringPoint")
public class MonitoringPoint extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	protected int ID;

	@Column(name = "stateTransitionType")
	private MonitoringPointStateTransition monitoringPointstateTransitionType;

	@ManyToOne(cascade = CascadeType.PERSIST)
	private EapEventType eventType;

	@Column(name = "eventTypeCondition")
	private String condition;

	public MonitoringPoint() {
		this.ID = 0;
	}

	public MonitoringPoint(final EapEventType eventType, final MonitoringPointStateTransition stateTransitionType,
			final String condition) {
		this.eventType = eventType;
		this.monitoringPointstateTransitionType = stateTransitionType;
		this.condition = condition;
	}

	public MonitoringPointStateTransition getStateTransitionType() {
		return this.monitoringPointstateTransitionType;
	}

	public void setStateTransitionType(final MonitoringPointStateTransition stateTransitionType) {
		this.monitoringPointstateTransitionType = stateTransitionType;
	}

	public EapEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(final EapEventType eventType) {
		this.eventType = eventType;
	}

	public String getCondition() {
		return this.condition;
	}

	public void setCondition(final String condition) {
		this.condition = condition;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	@Override
	public String toString() {
		return "MonitoringPoint: " + this.getStateTransitionType();
	}

	/**
	 * Returns all monitoring points from the database, which belong to the
	 * given event type.
	 * 
	 * @param eventType
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<MonitoringPoint> findByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager()
				.createNativeQuery("SELECT * FROM MonitoringPoint WHERE EVENTTYPE_ID = '" + eventType.getID() + "'",
						MonitoringPoint.class);
		return query.getResultList();
	}

}
