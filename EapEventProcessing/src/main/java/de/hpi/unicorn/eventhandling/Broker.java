/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.eventhandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.espertech.esper.client.EPStatement;

import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.correlation.Correlator;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EventTypeRule;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.notification.NotificationRule;
import de.hpi.unicorn.notification.NotificationRuleForEvent;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.transformation.TransformationRule;
import de.hpi.unicorn.transformation.TransformationRuleLogic;
import de.hpi.unicorn.visualisation.ChartConfiguration;
import de.hpi.unicorn.visualisation.EventView;

/**
 * This class is a central point for events, and event types to enter the
 * platform. This enables the consistency between streaming engine and database.
 */
public class Broker implements EventImporter, EventAdministrator {

	private static Broker instance;
	private static final Logger logger = Logger.getLogger(Broker.class);

	private Broker() {

	}

	public static Broker getInstance() {
		if (Broker.instance == null) {
			Broker.instance = new Broker();
		}
		return Broker.instance;
	}

	public static EventImporter getEventImporter() {
		return Broker.getInstance();
	}

	public static EventAdministrator getEventAdministrator() {
		return Broker.getInstance();
	}

	/**
	 * This method should be used to insert an event into the platform. It will
	 * be correlated, saved in the database, send to the streaming engine and
	 * possibly invoke notifications.
	 * 
	 * @param event
	 * @return
	 */
	@Override
	public synchronized EapEvent importEvent(final EapEvent event, final boolean rawSending) {
		for (final String attribute : event.getValuesForExport().keySet()) {
			final String value = event.getValuesForExport().get(attribute);
			if (value.contains("'") || value.contains("\\")) {
				event.getValuesForExport().put(attribute, value.replaceAll("\\\\", "/").replaceAll("\'", "\\\'"));
			}
		}
		Broker.logger.debug("==== Importing event " + event.getEventType().getTypeName());
		if (!rawSending && EapConfiguration.persistEvents) {
			final long saveStart = System.currentTimeMillis();
			event.save();
			final long saveEnd = System.currentTimeMillis();
			Broker.logger.debug(String.format("Saving event took %.2f seconds", (double) (saveEnd - saveStart) / 1000));
		}
		if (!rawSending && EapConfiguration.persistEvents) {
			final long corrStart = System.currentTimeMillis();
			Correlator.correlate(Arrays.asList(event));
			final long corrEnd = System.currentTimeMillis();
			Broker.logger.debug(String.format("Correlating event took %.2f seconds",
					(double) (corrEnd - corrStart) / 1000));
		}
		final long addStart = System.currentTimeMillis();
		StreamProcessingAdapter.getInstance().addEvent(event);
		final long addEnd = System.currentTimeMillis();
		Broker.logger.debug(String.format("Inserting event took %.2f seconds", (double) (addEnd - addStart) / 1000));

		if (!rawSending) {
			final long notifyStart = System.currentTimeMillis();
			NotificationObservable.getInstance().trigger(event);
			final long notifyEnd = System.currentTimeMillis();
			Broker.logger.debug(String.format("Notification of event took %.2f seconds",
					(double) (notifyEnd - notifyStart) / 1000));
		}
		return event;
	}

	/**
	 * This method should be used to save eventNotificationRules. They will be
	 * added to the observable and saved in the database.
	 * 
	 * @param rule
	 * @return
	 */
	public synchronized NotificationRule send(final NotificationRule rule) {
		rule.save();
		final NotificationRuleForEvent eventRule = (NotificationRuleForEvent) rule;
		NotificationObservable.getInstance().addNotificationObserver(eventRule);

		return rule;
	}

	@Override
	public synchronized EapEventType importEventType(final EapEventType eventType) {
		final EapEventType registered = EapEventType.findByTypeName(eventType.getTypeName());
		if (registered != null) {
			System.err.println("An EventType with name :" + eventType.getTypeName() + " is already saved.");
			return registered;
		}
		StreamProcessingAdapter.getInstance().addEventType(eventType);
		eventType.save();
		return eventType;
	}

	/**
	 * This method should be used to send several events to the platform. They
	 * will be correlated, saved in the database, send to the streaming engine
	 * and possibly invoke notifications.
	 * 
	 * @param events
	 * @return
	 */
	@Override
	public synchronized List<EapEvent> importEvents(final List<EapEvent> events) {
		if (events != null && !events.isEmpty()) {
			for (final EapEvent event : events) {
				for (final String attribute : event.getValuesForExport().keySet()) {
					final String value = event.getValuesForExport().get(attribute);
					if (value.contains("'") || value.contains("\\")) {
						event.getValuesForExport().put(attribute,
								value.replaceAll("\\\\", "/").replaceAll("\'", "\\\\\'"));
					}
				}
			}
			if (EapConfiguration.persistEvents) {
				EapEvent.save(events);
				Correlator.correlate(events);
			}
			StreamProcessingAdapter.getInstance().addEvents(events);
			NotificationObservable.getInstance().trigger(events);
		}
		return events;
	}

	/**
	 * This method should be used to remove event types from the platform. It
	 * will be removed from the database and deleted from the streaming engine.
	 * 
	 * @param eventType
	 * @return
	 */
	@Override
	public synchronized EapEventType removeEventType(final EapEventType eventType) {

		try {
			// remove eventtype from process
			// delete correlation rule if correlation rule contains attribute of
			// this event type
			for (final CorrelationProcess process : CorrelationProcess.findByEventType(eventType)) {
				process.setEventTypes(new HashSet<EapEventType>());
				if (process.getTimeCondition() != null) {
					process.getTimeCondition().remove();
				}
				process.setCorrelationAttributes(new ArrayList<TypeTreeNode>());
				final Set<CorrelationRule> correlationRulesOfProcess = new HashSet<CorrelationRule>(
						process.getCorrelationRules());
				process.setCorrelationRules(new HashSet<CorrelationRule>());
				process.merge();
				for (final CorrelationRule correlationRule : correlationRulesOfProcess) {
					correlationRule.remove();
				}
			}

			// delete events of eventType
			for (final EapEvent event : EapEvent.findByEventType(eventType)) {
				this.removeEvent(event);
			}
			// delete eventTypeRule, that create this eventType
			final EventTypeRule rule = EventTypeRule.findEventTypeRuleForCreatedEventType(eventType);
			if (rule != null) {
				rule.remove();
			}
			// update eventTypeRule, remove if no usedType remains
			final List<EventTypeRule> containingRules = EventTypeRule.findEventTypeRuleForContainedEventType(eventType);
			for (final EventTypeRule containingRule : containingRules) {
				containingRule.getUsedEventTypes().remove(this);
				containingRule.merge();
				if (containingRule.getUsedEventTypes().isEmpty()) {
					containingRule.remove();
				}
			}
			// update eventView, remove if no usedType remains
			final List<EventView> eventViews = EventView.findByEventType(eventType);
			for (final EventView eventView : eventViews) {
				eventView.getEventTypes().remove(this);
				if (eventView.getEventTypes().isEmpty()) {
					eventView.remove();
				}
			}
			// remove ChartOptions
			final List<ChartConfiguration> charts = ChartConfiguration.findByEventType(eventType);
			for (final ChartConfiguration chart : charts) {
				chart.remove();
			}
			// remove event types from monitoring points
			final List<MonitoringPoint> monitoringPoints = MonitoringPoint.findByEventType(eventType);
			for (final MonitoringPoint monitoringPoint : monitoringPoints) {
				monitoringPoint.setEventType(null);
				monitoringPoint.merge();
			}
			// remove transformation rules referencing this event type
			final List<TransformationRule> transformationRules = TransformationRule.findByEventType(eventType);
			for (final TransformationRule transformationRule : transformationRules) {
				this.remove(transformationRule);
			}
		} catch (final Exception e) {
			e.printStackTrace();
		}
		StreamProcessingAdapter.getInstance().removeEventType(eventType);
		eventType.remove();

		return eventType;
	}

	@Override
	public List<EapEventType> getAllEventTypes() {
		return EapEventType.findAll();
	}

	/**
	 * This method should be used to remove an event from the platform. It will
	 * be removed from the database and deleted from the streaming engine.
	 * 
	 * @param eventType
	 * @return
	 */
	@Override
	public synchronized EapEvent removeEvent(final EapEvent event) {
		StreamProcessingAdapter.getInstance().removeEvent(event);
		event.remove();

		return event;
	}

	@Override
	public EapEvent importEvent(final EapEvent event) {
		return this.importEvent(event, false);
	}

	@Override
	public void importEventsWithSchedule(final Runnable eventAdapter) {
		// TODO Auto-generated method stub
	}

	public void register(final TransformationRule transformationRule) {
		// TransformationRuleLogic.checkForValidity(transformationRule);
		StreamProcessingAdapter.getInstance().addTransformationRule(transformationRule);
	}

	public void activateTransformationRule(final TransformationRule transformationRule) {
		final EPStatement statement = StreamProcessingAdapter.getInstance().getStatement(
				TransformationRuleLogic.generateStatementName(transformationRule));
		statement.start();
	}

	public void deactivateTransformationRule(final TransformationRule transformationRule) {
		final EPStatement statement = StreamProcessingAdapter.getInstance().getStatement(
				TransformationRuleLogic.generateStatementName(transformationRule));
		statement.stop();
	}

	/**
	 * Unregisters the transformation rule with Esper and removes it from the
	 * database. TODO: Naming
	 * 
	 * @param transformationRule
	 */
	public void remove(final TransformationRule transformationRule) {
		StreamProcessingAdapter.getInstance().removeTransformationRule(transformationRule);
		transformationRule.remove();
	}
}
