/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * Provides methods to correlate existing and incoming events to process
 * instances using correlation rules.
 */
public class RuleCorrelator {

	/**
	 * Defines the correlation for a process using correlation rules and
	 * correlates existing events for the process.
	 * 
	 * @param correlationRules
	 *            set of correlation rules (pairs of attributes belonging to the
	 *            same or different event type) defining the correlation of the
	 *            given process (e.g. E1.A=E2.B, E2.G=E3.H, E3.H=E4.H for event
	 *            types E1, E2, E3, E4)
	 * @param process
	 *            the process from which the process instances are derived and
	 *            created
	 * @param timeCondition
	 *            (optional) rule for advanced time correlation related to the
	 *            process
	 */
	public static void correlate(final Set<CorrelationRule> correlationRules, final CorrelationProcess process,
			final TimeCondition timeCondition) {
		final Set<EapEventType> eventTypes = new HashSet<EapEventType>();
		for (final CorrelationRule rule : correlationRules) {
			rule.setProcess(process);
			rule.save();
			process.addCorrelationRule(rule);
			eventTypes.add(rule.getFirstAttribute().getEventType());
			eventTypes.add(rule.getSecondAttribute().getEventType());
		}
		if (timeCondition != null) {
			timeCondition.save();
			process.setTimeCondition(timeCondition);
		}
		process.setEventTypes(eventTypes);
		process.merge();
		final Set<EapEvent> eventsToCorrelate = new HashSet<EapEvent>();
		for (final EapEventType actualEventType : eventTypes) {
			eventsToCorrelate.addAll(EapEvent.findByEventType(actualEventType));
		}
		final Iterator<EapEvent> eventIterator = eventsToCorrelate.iterator();
		while (eventIterator.hasNext()) {
			final EapEvent actualEvent = eventIterator.next();
			RuleCorrelator.correlateEventToProcessInstance(actualEvent, correlationRules, process, timeCondition);
		}
	}

	/**
	 * Correlates an event to a process instance using correlation rules. If no
	 * matching process instance is found, a new process instance is created and
	 * the event is be correlated to this instance.
	 * 
	 * @param actualEvent
	 *            the event to be correlated to a process instance
	 * @param correlationRules
	 *            set of correlation rules (pairs of attributes belonging to the
	 *            same or different event type) defining the correlation of the
	 *            given process (e.g. E1.A=E2.B, E2.G=E3.H, E3.H=E4.H for event
	 *            types E1, E2, E3, E4)
	 * @param process
	 *            the process from which the process instances are derived and
	 *            created
	 * @param timeCondition
	 *            (optional) rule for advanced time correlation related to the
	 *            process
	 */
	static void correlateEventToProcessInstance(final EapEvent actualEvent,
			final Set<CorrelationRule> correlationRules, final CorrelationProcess process,
			final TimeCondition timeCondition) {

		boolean insertedInExistingProcessInstance = false;
		final List<CorrelationProcessInstance> processInstances = CorrelationProcessInstance.findByProcess(process);
		final Set<CorrelationProcessInstance> matchedProcessInstances = new HashSet<CorrelationProcessInstance>();

		/*
		 * Looking for matching existing process instances. If no rule for
		 * advanced time correlation is provided, the event is related to a
		 * process instance if their values defined through the correlation
		 * rules are equal.
		 */
		for (final CorrelationProcessInstance actualProcessInstance : processInstances) {
			boolean processInstanceAndEventMatch = false;
			final Map<String, Serializable> valuesOfProcessInstance = actualProcessInstance
					.getCorrelationAttributesAndValues();
			final Map<String, Serializable> valuesOfEvent = actualEvent.getValues();
			for (final CorrelationRule actualCorrelationRule : correlationRules) {
				if (actualCorrelationRule.getFirstAttribute().getEventType().getID() == actualEvent.getEventType()
						.getID()) {
					final String qualifiedAttributeName = actualCorrelationRule.getSecondAttribute()
							.getQualifiedAttributeName();
					final String attributeExpression = actualCorrelationRule.getFirstAttribute()
							.getAttributeExpression();
					if (valuesOfProcessInstance.get(qualifiedAttributeName) != null) {
						if (valuesOfProcessInstance.get(qualifiedAttributeName).equals(
								valuesOfEvent.get(attributeExpression))) {
							processInstanceAndEventMatch = true;
						} else {
							processInstanceAndEventMatch = false;
							break;
						}
						continue;
					}
				}
				if (actualCorrelationRule.getSecondAttribute().getEventType().getID() == actualEvent.getEventType()
						.getID()) {
					final String qualifiedAttributeName = actualCorrelationRule.getFirstAttribute()
							.getQualifiedAttributeName();
					final String attributeExpression = actualCorrelationRule.getSecondAttribute()
							.getAttributeExpression();
					if (valuesOfProcessInstance.get(qualifiedAttributeName) != null) {
						if (valuesOfProcessInstance.get(qualifiedAttributeName).equals(
								valuesOfEvent.get(attributeExpression))) {
							processInstanceAndEventMatch = true;
						} else {
							processInstanceAndEventMatch = false;
							break;
						}
						continue;
					}
				}
			}
			if (processInstanceAndEventMatch) {
				matchedProcessInstances.add(actualProcessInstance);
			}
		}
		/*
		 * If the event matches to exactly one process instance: If no rule for
		 * advanced time correlation is provided, the event is added to the
		 * process instance. If a rule for advanced time correlation is
		 * provided, the event must additionally belong to the time period
		 * defined in the rule for advanced time correlation.
		 * 
		 * If the event matches to more than one process instance: If no rule
		 * for advanced time correlation is provided, the process instances are
		 * merged to one process instance and the event is added to the process
		 * instance. If a rule for advanced time correlation is provided, the
		 * event must additionally belong to the time period defined in the rule
		 * for advanced time correlation. The process instances will be merged
		 * only if the timer events are equal.
		 * 
		 * The event is finally added to the process instance(s).
		 */
		if (!matchedProcessInstances.isEmpty()) {
			for (final CorrelationProcessInstance actualProcessInstance : matchedProcessInstances) {
				if (timeCondition == null
						|| timeCondition.belongsEventToTimerEvent(actualEvent, actualProcessInstance.getTimerEvent())) {
					final Iterator<CorrelationProcessInstance> processInstanceIterator = matchedProcessInstances
							.iterator();
					while (processInstanceIterator.hasNext()) {
						final CorrelationProcessInstance processInstanceToMerge = processInstanceIterator.next();
						if (processInstanceToMerge != actualProcessInstance
								&& (processInstanceToMerge.getTimerEvent() == null || processInstanceToMerge
										.getTimerEvent().equals(actualProcessInstance.getTimerEvent()))) {
							for (final EapEvent eventToMerge : processInstanceToMerge.getEvents()) {
								if (!actualProcessInstance.getEvents().contains(eventToMerge)) {
									actualProcessInstance.addEvent(eventToMerge);
									eventToMerge.addProcessInstance(actualProcessInstance);
									eventToMerge.save();
								}
							}
							// The correlation values are merged here.
							actualProcessInstance.addCorrelationAttributesAndValues(processInstanceToMerge
									.getCorrelationAttributesAndValues());
							processInstanceToMerge.remove();
							// System.out.println("Process instance merged and removed.");
						}
					}
					RuleCorrelator.storeCorrelationValuesOfProcessInstance(actualProcessInstance, correlationRules,
							actualEvent);

					actualProcessInstance.addEvent(actualEvent);
					actualProcessInstance.merge();

					actualEvent.addProcessInstance(actualProcessInstance);
					actualEvent.merge();

					insertedInExistingProcessInstance = true;
					break;
				}
			}
		}

		/*
		 * If no process instance is matched and no rule for advanced time
		 * correlation is defined, a new process instance is created here, the
		 * correlation values are taken from the event and stored in the process
		 * instance. The event is finally added to the new process instance. If
		 * no process instance is matched and a rule for advanced time
		 * correlation is defined, a new process instance is created here only
		 * if a timer event serving as the benchmark to which the advanced time
		 * correlation can be related exists and is not already related to a
		 * process instance.
		 */
		if (!insertedInExistingProcessInstance) {
			CorrelationProcessInstance newProcessInstance = new CorrelationProcessInstance();
			if (timeCondition != null) {
				final EapEvent timerEvent = timeCondition.getTimerEventForEvent(actualEvent, correlationRules);
				if (timerEvent == null) {
					return;
				} else {
					newProcessInstance.setTimerEvent(timerEvent);
				}
			}

			newProcessInstance.addEvent(actualEvent);
			newProcessInstance = newProcessInstance.save();

			process.addProcessInstance(newProcessInstance);
			process.merge();

			RuleCorrelator.storeCorrelationValuesOfProcessInstance(newProcessInstance, correlationRules, actualEvent);

			actualEvent.addProcessInstance(newProcessInstance);
			actualEvent.merge();
			newProcessInstance.merge();

			// System.out.println("New process instance added.");
		}
	}

	/**
	 * Helper method to store correlation values in a given process instance
	 * based on the new event. May cascade over the given correlation rules.
	 * Example: Event from type E2 has values c=3 and d=4. Correlation rules are
	 * E1.a=E2.c and E2.d=E3.d - correlation values are. E1.a=3, E2.c=3, E2.d=4
	 * and E3.d=4.
	 * 
	 * @param processInstance
	 *            the process instance where the correlation values have to be
	 *            stored
	 * @param correlationRules
	 *            set of correlation rules defining the correlation of the given
	 *            process
	 * @param event
	 *            the event where the values for correlation are derived from
	 */
	private static void storeCorrelationValuesOfProcessInstance(final CorrelationProcessInstance processInstance,
			final Set<CorrelationRule> correlationRules, final EapEvent event) {
		final List<TypeTreeNode> correlationAttributes = RuleCorrelator.extractCorrelationAttributes(correlationRules);
		// Set<CorrelationRule> correlationRules = new
		// HashSet<CorrelationRule>(correlationRulesOfProcess);
		for (final TypeTreeNode actualCorrelationAttribute : correlationAttributes) {
			if (actualCorrelationAttribute.getEventType().getID() == event.getEventType().getID()) {
				final String qualifiedAttributeName = actualCorrelationAttribute.getQualifiedAttributeName();
				final String attributeExpression = actualCorrelationAttribute.getAttributeExpression();
				final Serializable correlationValue = event.getValues().get(attributeExpression);
				if (!processInstance.getCorrelationAttributesAndValues().containsKey(qualifiedAttributeName)) {
					processInstance.addCorrelationAttributeAndValue(qualifiedAttributeName, correlationValue);
					// correlationAttributes.remove(actualCorrelationAttribute);
					// find related attribute
					for (final CorrelationRule actualCorrelationRule : correlationRules) {
						if (actualCorrelationAttribute.equalsWithEventType(actualCorrelationRule.getFirstAttribute())) {
							final TypeTreeNode relatedAttribute = actualCorrelationRule.getSecondAttribute();
							if (!processInstance.getCorrelationAttributesAndValues().containsKey(
									relatedAttribute.getQualifiedAttributeName())) {
								processInstance.addCorrelationAttributeAndValue(
										relatedAttribute.getQualifiedAttributeName(), correlationValue);
								// correlationAttributes.remove(relatedAttribute);
								// correlationRules.remove(actualCorrelationRule);
								RuleCorrelator.storeCorrelationValuesOfProcessInstance(processInstance,
										correlationRules, correlationAttributes, relatedAttribute, correlationValue);
							}
						}
						if (actualCorrelationAttribute.equalsWithEventType(actualCorrelationRule.getSecondAttribute())) {
							final TypeTreeNode relatedAttribute = actualCorrelationRule.getFirstAttribute();
							if (!processInstance.getCorrelationAttributesAndValues().containsKey(
									relatedAttribute.getQualifiedAttributeName())) {
								processInstance.addCorrelationAttributeAndValue(
										relatedAttribute.getQualifiedAttributeName(), correlationValue);
								// correlationAttributes.remove(relatedAttribute);
								// correlationRules.remove(actualCorrelationRule);
								RuleCorrelator.storeCorrelationValuesOfProcessInstance(processInstance,
										correlationRules, correlationAttributes, relatedAttribute, correlationValue);
							}
						}
					}
				}
			}
		}
	}

	private static void storeCorrelationValuesOfProcessInstance(final CorrelationProcessInstance processInstance,
			final Set<CorrelationRule> correlationRules, final List<TypeTreeNode> correlationAttributes,
			final TypeTreeNode actualAttribute, final Serializable correlationValue) {
		for (final CorrelationRule actualCorrelationRule : correlationRules) {
			if (actualAttribute.equalsWithEventType(actualCorrelationRule.getFirstAttribute())) {
				final TypeTreeNode relatedAttribute = actualCorrelationRule.getSecondAttribute();
				if (!processInstance.getCorrelationAttributesAndValues().containsKey(
						relatedAttribute.getQualifiedAttributeName())) {
					processInstance.addCorrelationAttributeAndValue(relatedAttribute.getQualifiedAttributeName(),
							correlationValue);
					RuleCorrelator.storeCorrelationValuesOfProcessInstance(processInstance, correlationRules,
							correlationAttributes, relatedAttribute, correlationValue);
				}
			}
			if (actualAttribute.equalsWithEventType(actualCorrelationRule.getSecondAttribute())) {
				final TypeTreeNode relatedAttribute = actualCorrelationRule.getFirstAttribute();
				if (!processInstance.getCorrelationAttributesAndValues().containsKey(
						relatedAttribute.getQualifiedAttributeName())) {
					processInstance.addCorrelationAttributeAndValue(relatedAttribute.getQualifiedAttributeName(),
							correlationValue);
					RuleCorrelator.storeCorrelationValuesOfProcessInstance(processInstance, correlationRules,
							correlationAttributes, relatedAttribute, correlationValue);
				}
			}
		}
	}

	/**
	 * Helper method to extract all attributes from the correlation rules.
	 * Unique by attribute expression and event type.
	 * 
	 * @param correlationRules
	 *            correlation rules where the attributes have to be extracted
	 *            from
	 * @return list of event type attributes
	 */
	private static List<TypeTreeNode> extractCorrelationAttributes(final Set<CorrelationRule> correlationRules) {
		final List<TypeTreeNode> correlationAttributes = new ArrayList<TypeTreeNode>();
		for (final CorrelationRule actualCorrelationRule : correlationRules) {
			boolean firstAttributeInList = false, secondAttributeInList = false;
			for (final TypeTreeNode correlationAttribute : correlationAttributes) {
				if (correlationAttribute.equalsWithEventType(actualCorrelationRule.getFirstAttribute())) {
					firstAttributeInList = true;
				}
				if (correlationAttribute.equalsWithEventType(actualCorrelationRule.getSecondAttribute())) {
					secondAttributeInList = true;
				}
			}
			if (!firstAttributeInList) {
				correlationAttributes.add(actualCorrelationRule.getFirstAttribute());
			}
			if (!secondAttributeInList) {
				correlationAttributes.add(actualCorrelationRule.getSecondAttribute());
			}
		}
		return correlationAttributes;
	}
}
