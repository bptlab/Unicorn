/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class is the provider for {@link ProcessInstanceMonitor}s.
 * 
 * @author micha
 * 
 */
public class ProcessInstanceMonitoringProvider extends AbstractDataProvider implements
		ISortableDataProvider<ProcessInstanceMonitor, String>, IFilterStateLocator {

	private static final long serialVersionUID = 1L;
	private static List<ProcessInstanceMonitor> processInstanceMonitors;
	private static List<ProcessInstanceMonitor> selectedProcessInstanceMonitors;
	private final ISortState sortState = new SingleSortState();
	private ProcessInstanceMonitoringFilter processInstanceMonitorFilter = new ProcessInstanceMonitoringFilter();
	private CorrelationProcess process;

	/**
	 * Constructor for providing {@link ProcessInstanceMonitor}s.
	 */
	public ProcessInstanceMonitoringProvider(final CorrelationProcess process) {
		this.process = process;
		ProcessInstanceMonitoringProvider.processInstanceMonitors = this.filterProcessInstanceMonitors(BPMNQueryMonitor
				.getInstance().getProcessInstanceMonitors(process), this.processInstanceMonitorFilter);
		ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors = new ArrayList<ProcessInstanceMonitor>();
	}

	@Override
	public void detach() {
		// events = null;
	}

	@Override
	public Iterator<? extends ProcessInstanceMonitor> iterator(final long first, final long count) {
		final List<ProcessInstanceMonitor> data = ProcessInstanceMonitoringProvider.processInstanceMonitors;
		Collections.sort(data, new Comparator<ProcessInstanceMonitor>() {
			@Override
			public int compare(final ProcessInstanceMonitor e1, final ProcessInstanceMonitor e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	private List<ProcessInstanceMonitor> filterProcessInstanceMonitors(
			final List<ProcessInstanceMonitor> processInstanceMonitorsToFilter,
			final ProcessInstanceMonitoringFilter processInstanceMonitorFilter) {
		final List<ProcessInstanceMonitor> returnedProcessInstanceMonitors = new ArrayList<ProcessInstanceMonitor>();
		if (processInstanceMonitorsToFilter != null) {
			for (final ProcessInstanceMonitor processInstanceMonitor : processInstanceMonitorsToFilter) {
				if (processInstanceMonitorFilter.match(processInstanceMonitor)) {
					returnedProcessInstanceMonitors.add(processInstanceMonitor);
				}
			}
		}
		return returnedProcessInstanceMonitors;
	}

	@Override
	public IModel<ProcessInstanceMonitor> model(final ProcessInstanceMonitor processInstanceMonitor) {
		return Model.of(processInstanceMonitor);
	}

	@Override
	public long size() {
		return ProcessInstanceMonitoringProvider.processInstanceMonitors.size();
	}

	public List<ProcessInstanceMonitor> getProcessInstanceMonitors() {
		return ProcessInstanceMonitoringProvider.processInstanceMonitors;
	}

	public List<ProcessInstanceMonitor> getSelectedProcessInstanceMonitors() {
		return ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors;
	}

	public static void setProcessInstanceMonitors(final List<ProcessInstanceMonitor> eventList) {
		ProcessInstanceMonitoringProvider.processInstanceMonitors = eventList;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public Object getFilterState() {
		return this.processInstanceMonitorFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.processInstanceMonitorFilter = (ProcessInstanceMonitoringFilter) state;
	}

	public ProcessInstanceMonitoringFilter getProcessInstanceMonitorFilter() {
		return this.processInstanceMonitorFilter;
	}

	public void setProcessInstanceMonitorFilter(final ProcessInstanceMonitoringFilter processInstanceMonitorFilter) {
		this.processInstanceMonitorFilter = processInstanceMonitorFilter;
		ProcessInstanceMonitoringProvider.processInstanceMonitors = this.filterProcessInstanceMonitors(BPMNQueryMonitor
				.getInstance().getProcessInstanceMonitors(this.process), processInstanceMonitorFilter);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : ProcessInstanceMonitoringProvider.processInstanceMonitors) {
			final ProcessInstanceMonitor processInstanceMonitor = (ProcessInstanceMonitor) element;
			if (processInstanceMonitor.getID() == entryId) {
				ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors.add(processInstanceMonitor);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final ProcessInstanceMonitor processInstanceMonitor : ProcessInstanceMonitoringProvider.processInstanceMonitors) {
			if (processInstanceMonitor.getID() == entryId) {
				ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors.remove(processInstanceMonitor);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final ProcessInstanceMonitor processInstanceMonitor : ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors) {
			if (processInstanceMonitor.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void selectAllEntries() {
		for (final ProcessInstanceMonitor processInstanceMonitor : ProcessInstanceMonitoringProvider.processInstanceMonitors) {
			ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors.add(processInstanceMonitor);
		}
	}

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public void setProcess(final CorrelationProcess process) {
		this.process = process;
		ProcessInstanceMonitoringProvider.processInstanceMonitors = this.filterProcessInstanceMonitors(BPMNQueryMonitor
				.getInstance().getProcessInstanceMonitors(process), this.processInstanceMonitorFilter);
		ProcessInstanceMonitoringProvider.selectedProcessInstanceMonitors = new ArrayList<ProcessInstanceMonitor>();
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final ProcessInstanceMonitor processInstanceMonitor : ProcessInstanceMonitoringProvider.processInstanceMonitors) {
			if (processInstanceMonitor.getID() == entryId) {
				return processInstanceMonitor;
			}
		}
		return null;
	}

}
