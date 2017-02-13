/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import de.hpi.unicorn.correlation.ConditionParser;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.TransformationTree;
import de.hpi.unicorn.notification.EventCondition;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * An EventTypeRule states a rule for creating new Events from existing Events
 * in the database. Events from certain eventTypes (usedEventTypes), that
 * fulfill a certain condition (conditionString) are trigger the creation of a
 * new event of the eventType createdEventType.
 */
@Entity
@Table(name = "EventTypeRule")
public class EventTypeRule extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private final int ID;

	@ManyToMany(cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private ArrayList<EapEventType> usedEventTypes = new ArrayList<EapEventType>();;

	public ArrayList<EapEventType> getUsedEventTypes() {
		return this.usedEventTypes;
	}

	public void setUsedEventTypes(final ArrayList<EapEventType> usedEventTypes) {
		this.usedEventTypes = usedEventTypes;
	}

	@OneToOne(optional = true, cascade = CascadeType.PERSIST, fetch = FetchType.EAGER)
	private final EventCondition condition;

	@OneToOne(optional = true)
	@JoinColumn(name = "EventType_ID")
	private EapEventType createdEventType;

	// TODO: refactor to HashMap
	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE }, targetEntity = TransformationTree.class)
	@JoinColumn(name = "MapTreeID")
	private TransformationTree<String, Serializable> eventAttributes;

	public EventTypeRule() {
		this.ID = 0;
		this.condition = null;
		this.createdEventType = null;
	}

	/**
	 * An EventTypeRule states a rule for creating new Events from existing
	 * Events in the database. Events from certain eventTypes (usedEventTypes),
	 * that fulfill a certain condition (conditionString) are trigger the
	 * creation of a new event of the eventType createdEventType
	 * 
	 * @param usedEventTypes
	 *            : eventType of events that can trigger a new event
	 * @param conditionString
	 *            : condition, that must be fulfilled to trigger a new event
	 * @param createdEventType
	 *            : eventtype of the event created
	 */
	public EventTypeRule(final ArrayList<EapEventType> usedEventTypes, final EventCondition condition,
			final EapEventType createdEventType) {
		this.ID = 0;
		this.usedEventTypes = usedEventTypes;
		this.condition = condition;
		this.createdEventType = createdEventType;
		this.eventAttributes = ConditionParser.extractEventAttributes(condition.getConditionString());
	}

	/**
	 * This method executes the event type rule on the existing events from the
	 * database. It will create new events.
	 * 
	 * @return created events
	 */
	public ArrayList<EapEvent> execute() {
		final List<EapEvent> chosenEvents = new ArrayList<EapEvent>();
		// find Events for EventType
		for (final EapEventType usedEventType : this.usedEventTypes) {
			final List<EapEvent> chosenEventsForType = EapEvent.findByEventType(usedEventType);
			if (!this.eventAttributes.isEmpty()) {
				chosenEventsForType.retainAll(EapEvent.findByValues(this.eventAttributes));
			}
			chosenEvents.addAll(chosenEventsForType);
		}
		// create new Events
		final ArrayList<EapEvent> newEvents = new ArrayList<EapEvent>();
		for (final EapEvent event : chosenEvents) {
			final EapEvent newEvent = new EapEvent(this.createdEventType, event.getTimestamp(), this.createValues(event
					.getValues()));
			newEvents.add(newEvent);
		}
		return newEvents;
	}

	private TransformationTree<String, Serializable> createValues(final Map<String, Serializable> values) {
		final TransformationTree<String, Serializable> newValues = new TransformationTree<String, Serializable>();
		for (final TypeTreeNode attribute : this.createdEventType.getValueTypes()) {
			final String attributeName = attribute.getAttributeExpression();
			if (values.containsKey(attributeName)) {
				newValues.put(attributeName, values.get(attributeName));
			}
		}
		return newValues;
	}

	/**
	 * Removes the event type from the source event types of this rule. This is
	 * needed for instance if the event type will be deleted.
	 * 
	 * @param eventType
	 * @return
	 */
	public boolean removeUsedEventType(final EapEventType eventType) {
		for (final EapEventType usedEventType : this.usedEventTypes) {
			if (usedEventType.equals(eventType)) {
				final boolean result = this.usedEventTypes.remove(usedEventType);
				this.merge();
				return result;
			}
		}
		return false;
	}

	// Getter and Setter

	public EapEventType getCreatedEventType() {
		return this.createdEventType;
	}

	public void setCreatedEventType(final EapEventType createdEventType) {
		this.createdEventType = createdEventType;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	// JPA-Methods

	/**
	 * Finds the event type rules that use a certain event type as a source.
	 * 
	 * @param eventType
	 * @return event type rules
	 */
	public static List<EventTypeRule> findEventTypeRuleForContainedEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * " + "FROM EventTypeRule " + "WHERE ID IN (" + "	SELECT A.EventTypeRule_ID "
						+ "	FROM EventTypeRule_EventType AS A " + "	WHERE usedEventTypes_ID = " + eventType.getID()
						+ ")", EventTypeRule.class);
		return query.getResultList();
	}

	/**
	 * Finds the event type rules that use a certain event type as output.
	 * 
	 * @param eventType
	 * @return event type rules
	 */
	public static EventTypeRule findEventTypeRuleForCreatedEventType(final EapEventType eventType) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM EventTypeRule WHERE EventType_ID = " + eventType.getID(), EventTypeRule.class);
		assert (query.getResultList().size() < 2);
		if (query.getResultList().size() > 0) {
			return (EventTypeRule) query.getResultList().get(0);
		} else {
			return null;
		}
	}

}
