/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.visualisation;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityTransaction;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.user.EapUser;

/**
 * This class represents an event view. It saves the information needed to
 * visualizes the occurrence of events of certain event types in a certain
 * period of time.
 */
@Entity
@Table(name = "EventView")
public class EventView extends Persistable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@ManyToMany(fetch = FetchType.EAGER)
	List<EapEventType> eventTypes;

	@Column(name = "timeRadius")
	@Enumerated(EnumType.STRING)
	private TimePeriodEnum timePeriod;

	@ManyToOne
	private EapUser user;

	@Override
	public int getID() {
		return this.ID;
	}
	
	/**
	 * Creates a default event view.
	 * 
	 * @param user
	 * @param eventTypes
	 * @param timePeriod
	 */
	public EventView() {
		this.user = user;
		this.eventTypes = eventTypes;
		this.timePeriod = timePeriod;
	}

	/**
	 * Creates an event view.
	 * 
	 * @param user
	 * @param eventTypes
	 * @param timePeriod
	 */
	public EventView(final EapUser user, final ArrayList<EapEventType> eventTypes, final TimePeriodEnum timePeriod) {
		this.user = user;
		this.eventTypes = eventTypes;
		this.timePeriod = timePeriod;
	}

	// Getter and Setter

	public List<EapEventType> getEventTypes() {
		return this.eventTypes;
	}

	public void setEventTypes(final List<EapEventType> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public TimePeriodEnum getTimePeriod() {
		return this.timePeriod;
	}

	public void setTimePeriod(final TimePeriodEnum timePeriod) {
		this.timePeriod = timePeriod;
	}

	public EapUser getUser() {
		return this.user;
	}

	public void setUser(final EapUser user) {
		this.user = user;
	}

	// JPA-Methods

	/**
	 * Finds all event views form the database.
	 * 
	 * @return all event views
	 */
	public static List<EventView> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM EventView t");
		return q.getResultList();
	}

	/**
	 * Finds all event views that match a certain condition.
	 * 
	 * @param columnName
	 * @param value
	 * @return event views that matches the condition
	 */
	private static List<EventView> findByAttribute(final String columnName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM EventView WHERE " + columnName + " = '" + value + "'", EventView.class);
		return query.getResultList();
	}

	/**
	 * Finds all event views that contain a certain event type.
	 * 
	 * @param eventType
	 * @return event views that contain a certain event type
	 */
	public static List<EventView> findByEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM EventView WHERE ID IN ( SELECT EventView_ID FROM EventView_EventType WHERE eventTypes_ID  = '"
						+ eventType.getID() + "' )", EventView.class);
		return query.getResultList();
	}

	/**
	 * Finds an event view by ID from database.
	 * 
	 * @param ID
	 * @return event view
	 */
	public static EventView findByID(final int ID) {
		final List<EventView> list = EventView.findByAttribute("ID", new Integer(ID).toString());
		if (list.size() > 0) {
			return list.get(0);
		} else {
			return null;
		}
	}

	/**
	 * Deletes all event views from the database.
	 */
	public static void removeAll() {
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM EventView");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

}
