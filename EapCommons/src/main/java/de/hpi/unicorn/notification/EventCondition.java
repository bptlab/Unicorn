/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.DateUtils;

/**
 * This class represents a condition for an event, saying that a certain
 * attribute equals a certain value. The event either matches a condition or
 * not.
 */
@Entity
@Table(name = "EventCondition")
public class EventCondition extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@Column(name = "attribute")
	private final String attribute;
	@Column(name = "value")
	private final String value;

	/**
	 * Creates a condition.
	 * 
	 * @param attribute
	 * @param conditionValue
	 */
	public EventCondition(final String attribute, final String conditionValue) {
		this.attribute = attribute;
		this.value = conditionValue;
	}

	/**
	 * Default constructor for JPA
	 */
	public EventCondition() {
		this.attribute = null;
		this.value = null;
	}

	/**
	 * Checks whether an event matches a condition
	 * 
	 * @param event
	 * @return whether the condition matches the event
	 */
	public boolean matches(final EapEvent event) {
		final Serializable eventValue = event.getValues().get(this.attribute);
		if (eventValue instanceof Long) {
			final long longValue = (Long) eventValue;
			return longValue == Long.parseLong(this.value);
		} else if (eventValue instanceof Double) {
			final double doubleValue = (Double) eventValue;
			return doubleValue == Double.parseDouble(this.value);
		} else if (eventValue instanceof Date) {
			final Date dateValue = (Date) eventValue;
			return dateValue.getTime() == DateUtils.parseDate(this.value).getTime();
		} else {
			try {
				final String stringValue = (String) eventValue;
				return stringValue.equals(this.value);
			} catch (final Exception e) {
				return false;
			}
		}
	}

	/**
	 * generates a string representing the condition
	 * 
	 * @return string representation of condition
	 */
	public String getConditionString() {
		if (this.attribute == null || this.value == null || this.attribute == "" || this.value == "") {
			return "";
		}
		return this.attribute + "=" + this.value;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	// JPA-Methods

	/**
	 * retrieves all conditions from database
	 * 
	 * @return all conditions
	 */
	public static List<EventCondition> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM EventCondition t");
		return q.getResultList();
	}

	/**
	 * finds condition with ID
	 * 
	 * @param ID
	 * @return condition
	 */
	public static EventCondition findByID(final int ID) {
		return Persistor.getEntityManager().find(EventCondition.class, ID);
	}

}
