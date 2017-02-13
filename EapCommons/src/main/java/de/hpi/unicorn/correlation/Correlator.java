/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import java.util.List;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * Provides methods to correlate existing and incoming events to process
 * instances.
 */
public class Correlator {

	/**
	 * Correlation for incoming events. Retrieves the processes that are related
	 * to each event via its event type. Tries to correlate the events to
	 * process instances. Definition of correlations by both single event type
	 * attributes (attributes with the same attribute expression belonging to
	 * all the given event types) and correlation rules (pairs of attributes
	 * belonging to the same or different event type) are supported.
	 * 
	 * @param events
	 *            events to be correlated
	 */
	public static void correlate(final List<EapEvent> events) {
		for (final EapEvent event : events) {
			final EapEventType eventType = event.getEventType();
			final List<CorrelationProcess> processes = CorrelationProcess.findByEventType(eventType);
			for (final CorrelationProcess process : processes) {
				if (process.isCorrelationWithCorrelationRules()) {
					RuleCorrelator.correlateEventToProcessInstance(event, process.getCorrelationRules(), process,
							process.getTimeCondition());
				} else {
					AttributeCorrelator.correlateEventToProcessInstance(event, process.getCorrelationAttributes(),
							process, process.getTimeCondition());
				}
			}
		}
	}

	/**
	 * Destroys all instances related to the given process.
	 * 
	 * @param selectedProcess
	 */
	public static void removeExistingCorrelation(final CorrelationProcess selectedProcess) {
		final List<CorrelationProcessInstance> existingProcessInstances = CorrelationProcessInstance
				.findByProcess(selectedProcess);
		for (final CorrelationProcessInstance processInstance : existingProcessInstances) {
			processInstance.remove();
		}
	}
}
