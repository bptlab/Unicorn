/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.PatternQuery;

/**
 * The central instance to get information for monitoring and analysing of the
 * status of BPMN queries.
 * 
 * @author micha
 */
public class BPMNQueryMonitor {

	/*
	 * Eingehende Informationen: - ProcessInstances nach Korrelation - Queries
	 * für Process aus BPMN-Modell - Query-Matches von Esper-Listener
	 */

	private static BPMNQueryMonitor instance;
	private final Set<ProcessMonitor> processMonitors;

	public BPMNQueryMonitor() {
		this.processMonitors = new HashSet<ProcessMonitor>();
	}

	public static BPMNQueryMonitor getInstance() {
		// lazy initialize
		if (BPMNQueryMonitor.instance == null) {
			BPMNQueryMonitor.instance = new BPMNQueryMonitor();
		}
		return BPMNQueryMonitor.instance;
	}

	public final ProcessInstanceStatus getStatus(final CorrelationProcessInstance processInstance) {
		if (processInstance != null) {
			final ProcessMonitor processMonitor = this.getProcessMonitorForProcess(processInstance.getProcess());
			return processMonitor.getProcessInstanceStatus(processInstance);
		}
		return null;
	}

	public final EventTree<DetailedQueryStatus> getDetailedStatus(final CorrelationProcessInstance processInstance) {
		if (this.getProcessInstanceMonitor(processInstance) != null) {
			return this.getProcessInstanceMonitor(processInstance).getDetailedStatus();
		}
		return null;
	}

	public final void addQueryForProcess(final PatternQuery query, final CorrelationProcess process) {
		if (query != null && process != null) {
			final ProcessMonitor processMonitor = this.getProcessMonitorForProcess(process);
			processMonitor.addQuery(query);
		}
	}

	public final void setQueryFinishedForProcessInstance(final PatternQuery query,
			final CorrelationProcessInstance processInstance) {
		if (query != null && processInstance != null) {
			final ProcessMonitor processMonitor = this.getProcessMonitorForProcess(processInstance.getProcess());
			processMonitor.setQueryFinishedForProcessInstance(query, processInstance);
		}
	}

	public final ProcessMonitor getProcessMonitorForProcess(final CorrelationProcess process) {
		if (process != null) {
			for (final ProcessMonitor processMonitor : this.processMonitors) {
				/*
				 * TODO: equals auf dem Process funktioniert nicht, da gleiche
				 * Prozesse mit verschiedenen IDs als Parameter kommen können:
				 * CorrelationProcess.equals überschreiben? Erstmal mit ID
				 * prüfen
				 */
				if (processMonitor.getProcess() != null && processMonitor.getProcess().getID() == process.getID()) {
					return processMonitor;
				}
			}
			final ProcessMonitor processMonitor = new ProcessMonitor(process);
			this.processMonitors.add(processMonitor);
			return processMonitor;
		}
		return null;
	}

	public final List<ProcessInstanceMonitor> getProcessInstanceMonitors(final CorrelationProcess process) {
		if (process != null) {
			final ProcessMonitor processMonitor = this.getProcessMonitorForProcess(process);
			return new ArrayList<>(processMonitor.getProcessInstanceMonitors());
		} else {
			return null;
		}

	}

	public final List<ProcessMonitor> getProcessMonitors() {
		return new ArrayList<ProcessMonitor>(this.processMonitors);
	}

	public static void reset() {
		BPMNQueryMonitor.instance = null;
	}

	private ProcessInstanceMonitor getProcessInstanceMonitor(final CorrelationProcessInstance processInstance) {
		final ProcessMonitor processMonitor = this.getProcessMonitorForProcess(processInstance.getProcess());
		if (processMonitor.getProcessInstanceMonitor(processInstance) != null) {
			return processMonitor.getProcessInstanceMonitor(processInstance);
		}
		return null;
	}

}
