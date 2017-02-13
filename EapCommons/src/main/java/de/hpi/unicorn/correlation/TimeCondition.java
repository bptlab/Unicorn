/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * 
 * Container object for advanced correlation over time. Used for event
 * correlation. Related to a process.
 * 
 * @author Micha
 * @author Tsun
 */
@Entity
@Table(name = "TimeCondition")
public class TimeCondition extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@ManyToOne
	private EapEventType selectedEventType;

	@Column(name = "ConditionString")
	private String conditionString;

	@Column(name = "TimePeriod")
	private int timePeriod;

	@Column(name = "IsTimePeriodAfter")
	private boolean isTimePeriodAfterEvent;

	@OneToOne(mappedBy = "timeCondition")
	private CorrelationProcess process;

	@Transient
	private Set<EapEvent> timerEvents;

	@ElementCollection
	@CollectionTable(name = "TimeCondition_EventAttributes", joinColumns = @JoinColumn(name = "Id"))
	@MapKeyColumn(name = "EventAttributeName", length = 100)
	@Column(name = "EventAttributeValue", length = 100)
	private Map<String, Serializable> attributeExpressionsAndValues = new HashMap<String, Serializable>();

	public TimeCondition() {
		this.ID = 0;
		this.selectedEventType = null;
		this.conditionString = null;
		this.isTimePeriodAfterEvent = true;
		this.timePeriod = 0;
	}

	/**
	 * Constructor.
	 * 
	 * @param eventType
	 * @param timePeriod
	 *            in minutes
	 * @param isTimePeriodAfterEvent
	 * @param conditionString
	 *            Pair(s) of attributes and values that narrow down the choice
	 *            of events to which events from other types can be related to.
	 *            These events are called timer events.
	 */
	public TimeCondition(final EapEventType eventType, final int timePeriod, final boolean isTimePeriodAfterEvent,
			final String conditionString) {
		this();
		this.selectedEventType = eventType;
		this.timePeriod = timePeriod;
		this.isTimePeriodAfterEvent = isTimePeriodAfterEvent;
		this.conditionString = conditionString;
	}

	public EapEventType getSelectedEventType() {
		return this.selectedEventType;
	}

	public void setSelectedEventType(final EapEventType selectedEventType) {
		this.selectedEventType = selectedEventType;
	}

	public String getConditionString() {
		return this.conditionString;
	}

	public void setConditionString(final String conditionString) {
		this.conditionString = conditionString;
	}

	public int getTimePeriod() {
		return this.timePeriod;
	}

	public void setTimePeriod(final int timePeriod) {
		this.timePeriod = timePeriod;
	}

	public boolean isTimePeriodAfterEvent() {
		return this.isTimePeriodAfterEvent;
	}

	public void setTimePeriodAfterEvent(final boolean isTimePeriodAfterEvent) {
		this.isTimePeriodAfterEvent = isTimePeriodAfterEvent;
	}

	public Set<EapEvent> getTimerEvents() {
		this.attributeExpressionsAndValues = ConditionParser.extractEventAttributes(this.conditionString);
		this.timerEvents = new HashSet<EapEvent>(EapEvent.findByEventTypeAndAttributeExpressionsAndValues(
				this.selectedEventType, this.attributeExpressionsAndValues));
		return this.timerEvents;
	}

	/**
	 * Fetches the timer event to be correlated to from the choice of timer
	 * events. Timer event and given event must have the same values for the
	 * given correlation attributes. Timestamp of given event must be in the
	 * time period related to the timer event.
	 * 
	 * @param event
	 *            the event for which the timer event is searched
	 * @param correlationAttributes
	 *            list of single event type attributes
	 * @return the timer event closest to the given event
	 */
	public EapEvent getTimerEventForEvent(final EapEvent event, final List<TypeTreeNode> correlationAttributes) {
		long timerEventTime;
		final long eventTime = event.getTimestamp().getTime();
		final long timePeriodInMillis = this.timePeriod * 60 * 1000;
		final Map<EapEvent, Long> possibleTimerEventsAndTimeDifferences = new HashMap<EapEvent, Long>();
		for (final EapEvent timerEvent : this.getTimerEvents()) {
			boolean processInstanceAndEventMatch = true;
			for (final TypeTreeNode actualCorrelationAttribute : correlationAttributes) {
				if (!timerEvent.getValues().get(actualCorrelationAttribute.getAttributeExpression())
						.equals(event.getValues().get(actualCorrelationAttribute.getAttributeExpression()))) {
					processInstanceAndEventMatch = false;
					break;
				}
			}
			if (processInstanceAndEventMatch) {
				timerEventTime = timerEvent.getTimestamp().getTime();
				if (this.isTimePeriodAfterEvent) {
					if (timerEventTime <= eventTime && eventTime <= timerEventTime + timePeriodInMillis) {
						possibleTimerEventsAndTimeDifferences.put(timerEvent, eventTime - timerEventTime);
					}
				} else {
					if (timerEventTime - timePeriodInMillis <= eventTime && eventTime <= timerEventTime) {
						possibleTimerEventsAndTimeDifferences.put(timerEvent, timerEventTime - eventTime);
					}
				}
			}
		}
		if (possibleTimerEventsAndTimeDifferences.isEmpty()) {
			return null;
		} else {
			EapEvent closestTimerEvent = null;
			for (final EapEvent timerEvent : possibleTimerEventsAndTimeDifferences.keySet()) {
				if (closestTimerEvent == null
						|| possibleTimerEventsAndTimeDifferences.get(timerEvent) < possibleTimerEventsAndTimeDifferences
								.get(closestTimerEvent)) {
					closestTimerEvent = timerEvent;
				}
			}
			return closestTimerEvent;
		}
	}

	/**
	 * Fetches the timer event to be correlated to from the choice of timer
	 * events. Timer event and given event must have the same values for the
	 * attributes of the given correlation rules. Timestamp of given event must
	 * be in the time period related to the timer event.
	 * 
	 * @param event
	 *            the event for which the timer event is searched
	 * @param correlationRules
	 *            set of correlation rules
	 * @return the timer event closest to the given event
	 */
	public EapEvent getTimerEventForEvent(final EapEvent actualEvent, final Set<CorrelationRule> correlationRules) {
		long timerEventTime;
		final long eventTime = actualEvent.getTimestamp().getTime();
		final long timePeriodInMillis = this.timePeriod * 60 * 1000;
		final Map<EapEvent, Long> possibleTimerEventsAndTimeDifferences = new HashMap<EapEvent, Long>();
		for (final EapEvent timerEvent : this.getTimerEvents()) {
			boolean processInstanceAndEventMatch = true;
			for (final CorrelationRule actualCorrelationRule : correlationRules) {
				if (actualCorrelationRule.getFirstAttribute().getEventType().equals(timerEvent.getEventType())
						&& actualCorrelationRule.getSecondAttribute().getEventType().equals(actualEvent.getEventType())) {
					final String attributeExpressionForTimerEvent = actualCorrelationRule.getFirstAttribute()
							.getAttributeExpression();
					final String attributeExpressionForActualEvent = actualCorrelationRule.getSecondAttribute()
							.getAttributeExpression();
					if (!timerEvent.getValues().get(attributeExpressionForTimerEvent)
							.equals(actualEvent.getValues().get(attributeExpressionForActualEvent))) {
						processInstanceAndEventMatch = false;
						break;
					}
				}
				if (actualCorrelationRule.getSecondAttribute().getEventType().equals(timerEvent.getEventType())
						&& actualCorrelationRule.getFirstAttribute().getEventType().equals(actualEvent.getEventType())) {
					final String attributeExpressionForTimerEvent = actualCorrelationRule.getSecondAttribute()
							.getAttributeExpression();
					final String attributeExpressionForActualEvent = actualCorrelationRule.getFirstAttribute()
							.getAttributeExpression();
					if (!timerEvent.getValues().get(attributeExpressionForTimerEvent)
							.equals(actualEvent.getValues().get(attributeExpressionForActualEvent))) {
						processInstanceAndEventMatch = false;
						break;
					}
				}
			}
			/**
			 * If there are attributes in the timer event that are used in the
			 * correlation rules but not for the determination of timer events,
			 * a check is required whether its values are equal to the values
			 * from the given event.
			 */
			if (timerEvent.getEventType().equals(actualEvent.getEventType())) {
				final Set<TypeTreeNode> relatedAttributes = new HashSet<TypeTreeNode>();
				for (final CorrelationRule actualCorrelationRule : correlationRules) {
					if (actualCorrelationRule.getEventTypeOfFirstAttribute().equals(timerEvent.getEventType())) {
						relatedAttributes.add(actualCorrelationRule.getFirstAttribute());
					}
					if (actualCorrelationRule.getEventTypeOfSecondAttribute().equals(timerEvent.getEventType())) {
						relatedAttributes.add(actualCorrelationRule.getSecondAttribute());
					}
				}
				for (final TypeTreeNode attribute : relatedAttributes) {
					final String attributeExpression = attribute.getAttributeExpression();
					if (!timerEvent.getValues().get(attributeExpression)
							.equals(actualEvent.getValues().get(attributeExpression))) {
						processInstanceAndEventMatch = false;
					}
				}
			}

			if (processInstanceAndEventMatch) {
				timerEventTime = timerEvent.getTimestamp().getTime();
				if (this.isTimePeriodAfterEvent) {
					if (timerEventTime <= eventTime && eventTime <= timerEventTime + timePeriodInMillis) {
						possibleTimerEventsAndTimeDifferences.put(timerEvent, eventTime - timerEventTime);
					}
				} else {
					if (timerEventTime - timePeriodInMillis <= eventTime && eventTime <= timerEventTime) {
						possibleTimerEventsAndTimeDifferences.put(timerEvent, timerEventTime - eventTime);
					}
				}
			}
		}
		if (possibleTimerEventsAndTimeDifferences.isEmpty()) {
			return null;
		} else {
			EapEvent closestTimerEvent = null;
			for (final EapEvent timerEvent : possibleTimerEventsAndTimeDifferences.keySet()) {
				if (closestTimerEvent == null
						|| possibleTimerEventsAndTimeDifferences.get(timerEvent) < possibleTimerEventsAndTimeDifferences
								.get(closestTimerEvent)) {
					closestTimerEvent = timerEvent;
				}
			}
			return closestTimerEvent;
		}
	}

	public boolean belongsEventToTimerEvent(final EapEvent event, final EapEvent timerEvent) {
		final long timerEventTime = timerEvent.getTimestamp().getTime();
		final long eventTime = event.getTimestamp().getTime();
		final long timePeriodInMillis = this.timePeriod * 60 * 1000;
		if (this.isTimePeriodAfterEvent) {
			return (timerEventTime <= eventTime && eventTime <= timerEventTime + timePeriodInMillis) ? true : false;
		} else {
			return (timerEventTime - timePeriodInMillis <= eventTime && eventTime <= timerEventTime) ? true : false;
		}
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	@Override
	public TimeCondition remove() {
		if (this.process != null) {
			this.process.setTimeCondition(null);
			this.process.merge();
			this.process = null;
		}
		return (TimeCondition) super.remove();
	}

	public void removeTimerEvent(final EapEvent timerEvent) {
		this.timerEvents.remove(timerEvent);
	}

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public void setProcess(final CorrelationProcess process) {
		this.process = process;
	}

}
