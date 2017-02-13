/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;

public class BPMNProcessPreprocessor {

	/**
	 * Tries to adapt parts of the BPMN process for a processing with the RPST.
	 * 
	 * @param process
	 * @return
	 */
	public static BPMNProcess structureProcess(final BPMNProcess process) {
		BPMNProcessPreprocessor.mergeStartEvents(process);
		BPMNProcessPreprocessor.mergeEndEvents(process);
		return process;
	}

	/**
	 * Creates one start event for the process, if there is more than one.
	 * 
	 * @param process
	 */
	private static void mergeStartEvents(final BPMNProcess process) {
		// TODO: Was ist, wenn StartEvent MonitoringPoints hat
		if (process.getStartEvents().size() > 1) {
			final BPMNStartEvent newStartEvent = new BPMNStartEvent("Start1", "MergedStartEvent", null);
			final List<BPMNStartEvent> startEvents = new ArrayList<BPMNStartEvent>(process.getStartEvents());
			for (final BPMNStartEvent startEvent : startEvents) {
				for (final AbstractBPMNElement successor : startEvent.getSuccessors()) {
					AbstractBPMNElement.disconnectElements(startEvent, successor);
					AbstractBPMNElement.connectElements(newStartEvent, successor);
				}
				process.removeBPMNElement(startEvent);
			}
			process.addBPMNElement(newStartEvent);
		}
	}

	/**
	 * Creates one end event for the process, if there is more than one. The old
	 * events are removed and all predecessors of old end events are joined in
	 * one XOR-Gateway and a succeding new end event.
	 * 
	 * @param process
	 */
	private static void mergeEndEvents(final BPMNProcess process) {
		// TODO: MonitoringPoints der EndEvents in neues EndEvent übernehmen
		// Eigentlich müsste man die alten EndEvents mit XOR --> alte EndEvents
		// --> XOR --> neues EndEvent zusammenführen,
		// um nicht die Information über die MonitoringPoints zu verlieren
		if (process.getEndEvents().size() > 1) {
			final BPMNEndEvent newEndEvent = new BPMNEndEvent("End1", "MergedEndEvent", null);
			final BPMNXORGateway mergingXOR = new BPMNXORGateway("MergingXOR" + new Date().getTime(),
					"MergeXORBeforeEndEvent", null);
			final List<BPMNEndEvent> endEvents = new ArrayList<BPMNEndEvent>(process.getEndEvents());
			for (final BPMNEndEvent endEvent : endEvents) {
				for (final AbstractBPMNElement predecessor : endEvent.getPredecessors()) {
					AbstractBPMNElement.disconnectElements(predecessor, endEvent);
					AbstractBPMNElement.connectElements(predecessor, mergingXOR);
				}
				process.removeBPMNElement(endEvent);
			}
			process.addBPMNElement(mergingXOR);
			process.addBPMNElement(newEndEvent);
			AbstractBPMNElement.connectElements(mergingXOR, newEndEvent);
		}
	}

}
