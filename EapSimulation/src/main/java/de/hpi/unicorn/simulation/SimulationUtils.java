/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AbstractBPMNGateway;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.Tuple;

public class SimulationUtils {

	// TODO: methodenname nicht aussagekräftig
	public static Map<AbstractBPMNElement, String> getBPMNElementsFromEventTypes(
			final Map<EapEventType, String> eventTypesDurationStringMap, final BPMNProcess bpmnProcess) {
		final Map<AbstractBPMNElement, String> tasksDurationString = new HashMap<AbstractBPMNElement, String>();
		for (final AbstractBPMNElement bpmnElement : bpmnProcess.getSubElementsWithMonitoringpoints()) {
			for (final MonitoringPoint monitoringPoint : bpmnElement.getMonitoringPoints()) {
				tasksDurationString.put(bpmnElement, eventTypesDurationStringMap.get(monitoringPoint.getEventType()));
			}
		}
		return tasksDurationString;
	}

	public static Map<AbstractBPMNElement, DerivationType> getBPMNElementsFromEventTypes2(
			final Map<EapEventType, DerivationType> eventTypesDurationStringMap, final BPMNProcess bpmnProcess) {
		final Map<AbstractBPMNElement, DerivationType> tasksDurationString = new HashMap<AbstractBPMNElement, DerivationType>();
		for (final AbstractBPMNElement bpmnElement : bpmnProcess.getSubElementsWithMonitoringpoints()) {
			for (final MonitoringPoint monitoringPoint : bpmnElement.getMonitoringPoints()) {
				tasksDurationString.put(bpmnElement, eventTypesDurationStringMap.get(monitoringPoint.getEventType()));
			}
		}
		return tasksDurationString;
	}

	public static AbstractBPMNElement getBPMNElementFromEventType(final EapEventType eventType,
			final BPMNProcess bpmnProcess) {
		for (final AbstractBPMNElement bpmnElement : bpmnProcess.getSubElementsWithMonitoringpoints()) {
			for (final MonitoringPoint monitoringPoint : bpmnElement.getMonitoringPoints()) {
				if (eventType.equals(monitoringPoint.getEventType())) {
					return bpmnElement;
				}
			}
		}
		return null;

	}

	public static Map<AbstractBPMNElement, Long> getDurationsFromMap(
			final Map<AbstractBPMNElement, String> tasksDurationString) {
		final Map<AbstractBPMNElement, Long> bpmnElementsDuration = new HashMap<AbstractBPMNElement, Long>();
		for (final AbstractBPMNElement bpmnElement : tasksDurationString.keySet()) {
			bpmnElementsDuration.put(bpmnElement,
					SimulationUtils.getDurationFromString(tasksDurationString.get(bpmnElement)));
		}
		return bpmnElementsDuration;
	}

	private static long getDurationFromString(final String string) {
		Long duration = (long) 0;
		if (string != null) {
			if (string.contains(":")) {
				final String[] timestrings = string.split(":");
				for (int i = 0; i < timestrings.length; i++) {
					duration = duration + (Long.parseLong(timestrings[i]) * (1000 * 60 * 60 * 60 / (60 * (i + 1))));
				}
			} else {
				duration = Long.parseLong(string);
				duration = duration * 1000 * 60 * 60;
			}
		}
		return duration;
	}

	public static Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, EapEventType>>> getXORSplitsWithFollowingEventTypes(
			final BPMNProcess model) {
		final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, EapEventType>>> xorSplitsWithFolowingElementAndEventType = new HashMap<BPMNXORGateway, List<Tuple<AbstractBPMNElement, EapEventType>>>();
		for (final AbstractBPMNGateway gateway : model.getAllSplitGateways()) {
			if (gateway instanceof BPMNXORGateway) {
				final List<Tuple<AbstractBPMNElement, EapEventType>> successorAndFollowingEventTypes = new ArrayList<Tuple<AbstractBPMNElement, EapEventType>>();
				for (final AbstractBPMNElement successor : gateway.getSuccessors()) {
					AbstractBPMNElement element = successor;
					// falls es einen Leeren Pfad zum join gibt
					if (model.getAllJoinGateways().contains(successor)) {
						successorAndFollowingEventTypes.add(new Tuple<AbstractBPMNElement, EapEventType>(null, null));
					} else {
						while (!(element instanceof BPMNTask)) {
							element = element.getSuccessors().iterator().next();
						}
						final Tuple<AbstractBPMNElement, EapEventType> tuple = new Tuple(successor, element
								.getMonitoringPoints().get(0).getEventType());
						successorAndFollowingEventTypes.add(tuple);
						xorSplitsWithFolowingElementAndEventType.put((BPMNXORGateway) gateway,
								successorAndFollowingEventTypes);
					}
				}
			}
		}
		return xorSplitsWithFolowingElementAndEventType;
	}

	public static Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> convertProbabilityStrings(
			final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> xorSplitsWithSuccessorProbabilityStrings) {
		final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> xorSplitsWithSuccessorProbabilities = new HashMap<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>>();
		Integer percent;
		for (final BPMNXORGateway xorGateway : xorSplitsWithSuccessorProbabilityStrings.keySet()) {
			percent = 0;
			final List<Tuple<AbstractBPMNElement, Integer>> successorProbabilityList = new ArrayList<Tuple<AbstractBPMNElement, Integer>>();
			for (final Tuple<AbstractBPMNElement, String> successorProbabilityString : xorSplitsWithSuccessorProbabilityStrings
					.get(xorGateway)) {
				percent = percent + Integer.parseInt(successorProbabilityString.y);
				successorProbabilityList.add(new Tuple<AbstractBPMNElement, Integer>(successorProbabilityString.x,
						percent));
			}
			xorSplitsWithSuccessorProbabilities.put(xorGateway, successorProbabilityList);
		}
		return xorSplitsWithSuccessorProbabilities;
	}
}
