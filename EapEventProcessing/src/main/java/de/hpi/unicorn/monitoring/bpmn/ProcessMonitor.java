/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.query.PatternQuery;

/**
 * @author micha
 */
public class ProcessMonitor implements Serializable {

	private static final long serialVersionUID = 1L;
	private CorrelationProcess process;
	private final Set<PatternQuery> queries;
	private final Set<ProcessInstanceMonitor> processInstanceMonitors;
	private final int ID;
	private int numberOfProcessInstances;
	private float averageRuntimeMillis;

	public ProcessMonitor(final CorrelationProcess process) {
		this.process = process;
		this.queries = new HashSet<PatternQuery>();
		this.processInstanceMonitors = new HashSet<ProcessInstanceMonitor>();
		this.ID = BPMNQueryMonitor.getInstance().getProcessMonitors().size();
	}

	public Set<PatternQuery> getQueries() {
		return this.queries;
	}

	public void addQuery(final PatternQuery query) {
		this.queries.add(query);
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			processInstanceMonitor.addQuery(query);
		}
	}

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public void setProcess(final CorrelationProcess process) {
		this.process = process;
	}

	public void setQueryFinishedForProcessInstance(final PatternQuery query,
			final CorrelationProcessInstance processInstance) {
		final ProcessInstanceMonitor processInstanceMonitor = this
				.getProcessInstanceMonitorForProcessInstance(processInstance);
		processInstanceMonitor.setQueryFinished(query);
	}

	private ProcessInstanceMonitor getProcessInstanceMonitorForProcessInstance(
			final CorrelationProcessInstance processInstance) {
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			// if(processInstanceMonitor.getProcessInstance().equals(processInstance)){
			/*
			 * TODO: equals auf dem Process funktioniert nicht, da gleiche
			 * Prozesse mit verschiedenen IDs als Parameter kommen können:
			 * CorrelationProcess.equals überschreiben? Erstmal mit ID prüfen
			 */
			if (processInstanceMonitor.getProcessInstance() != null
					&& processInstanceMonitor.getProcessInstance().getID() == processInstance.getID()) {
				return processInstanceMonitor;
			}
		}
		final ProcessInstanceMonitor processInstanceMonitor = new ProcessInstanceMonitor(processInstance);
		processInstanceMonitor.addQueries(this.queries);
		this.processInstanceMonitors.add(processInstanceMonitor);
		this.numberOfProcessInstances = this.processInstanceMonitors.size();
		return processInstanceMonitor;
	}

	public ProcessInstanceStatus getProcessInstanceStatus(final CorrelationProcessInstance processInstance) {
		final ProcessInstanceMonitor processInstanceMonitor = this
				.getProcessInstanceMonitorForProcessInstance(processInstance);
		return processInstanceMonitor.getStatus();
	}

	public Set<ProcessInstanceMonitor> getProcessInstanceMonitors() {
		return this.processInstanceMonitors;
	}

	/**
	 * Returns all monitored process instances with the requested status.
	 * 
	 * @param processInstanceStatus
	 * @return
	 */
	public Set<CorrelationProcessInstance> getProcessInstances(final ProcessInstanceStatus processInstanceStatus) {
		final Set<CorrelationProcessInstance> processInstances = new HashSet<CorrelationProcessInstance>();
		for (final ProcessInstanceMonitor monitor : this.processInstanceMonitors) {
			if (monitor.getStatus().equals(processInstanceStatus)) {
				processInstances.add(monitor.getProcessInstance());
			}
		}
		return processInstances;
	}

	public int getID() {
		return this.ID;
	}

	public int getNumberOfProcessInstances() {
		return this.numberOfProcessInstances;
	}

	public float getAverageRuntimeMillis() {
		float sum = 0;
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			sum += processInstanceMonitor.getEndTime().getTime() - processInstanceMonitor.getStartTime().getTime();
		}
		this.averageRuntimeMillis = sum / this.processInstanceMonitors.size();
		return this.averageRuntimeMillis;
	}

	public float getAverageRuntimeForQuery(final PatternQuery query) {
		if (this.processInstanceMonitors.isEmpty()) {
			return 0;
		}
		float sum = 0;
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			sum += processInstanceMonitor.getRuntimeForQuery(query);
		}
		return sum / this.processInstanceMonitors.size();
	}

	public float getPathFrequencyForQuery(final PatternQuery query) {
		if (this.processInstanceMonitors.isEmpty()) {
			return 0;
		}
		float count = 0;
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			if (processInstanceMonitor.getStatusForQuery(query).equals(QueryStatus.Finished)) {
				count++;
			}
		}
		return count / this.processInstanceMonitors.size();
	}

	public ProcessInstanceMonitor getProcessInstanceMonitor(final CorrelationProcessInstance processInstance) {
		for (final ProcessInstanceMonitor processInstanceMonitor : this.processInstanceMonitors) {
			if (processInstanceMonitor.getProcessInstance().equals(processInstance)) {
				return processInstanceMonitor;
			}
		}
		return null;
	}

}
