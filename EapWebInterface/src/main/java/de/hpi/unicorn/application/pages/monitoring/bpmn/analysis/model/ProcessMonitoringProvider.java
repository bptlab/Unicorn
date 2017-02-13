/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.model;

import java.util.ArrayList;
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
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;

/**
 * This class is the provider for {@link ProcessMonitor}s. A filter can be
 * specified to return only some ProcessMonitors.
 * 
 * @author micha
 */
public class ProcessMonitoringProvider extends AbstractDataProvider implements
		ISortableDataProvider<ProcessMonitor, String>, IFilterStateLocator {

	private static final long serialVersionUID = 1L;
	private static List<ProcessMonitor> processMonitors;
	private static List<ProcessMonitor> selectedProcessMonitors;
	private final ISortState sortState = new SingleSortState();
	private ProcessMonitoringFilter processMonitorFilter = new ProcessMonitoringFilter();

	/**
	 * Constructor for providing {@link ProcessMonitor}s.
	 */
	public ProcessMonitoringProvider() {
		ProcessMonitoringProvider.processMonitors = BPMNQueryMonitor.getInstance().getProcessMonitors();
		ProcessMonitoringProvider.selectedProcessMonitors = new ArrayList<ProcessMonitor>();
	}

	@Override
	public void detach() {
		// processMonitors = null;
	}

	@Override
	public Iterator<? extends ProcessMonitor> iterator(final long first, final long count) {
		return ProcessMonitoringProvider.processMonitors.subList((int) first,
				(int) Math.min(first + count, ProcessMonitoringProvider.processMonitors.size())).iterator();
	}

	private List<ProcessMonitor> filterProcessMonitors(final List<ProcessMonitor> processMonitorsToFilter,
			final ProcessMonitoringFilter processMonitorFilter) {
		final List<ProcessMonitor> returnedProcessMonitors = new ArrayList<ProcessMonitor>();
		for (final ProcessMonitor processMonitor : processMonitorsToFilter) {
			if (processMonitorFilter.match(processMonitor)) {
				returnedProcessMonitors.add(processMonitor);
			}
		}
		return returnedProcessMonitors;
	}

	@Override
	public IModel<ProcessMonitor> model(final ProcessMonitor processMonitor) {
		return Model.of(processMonitor);
	}

	@Override
	public long size() {
		return ProcessMonitoringProvider.processMonitors.size();
	}

	public List<ProcessMonitor> getProcessMonitors() {
		return ProcessMonitoringProvider.processMonitors;
	}

	public List<ProcessMonitor> getSelectedProcessMonitors() {
		return ProcessMonitoringProvider.selectedProcessMonitors;
	}

	public static void setProcessMonitors(final List<ProcessMonitor> processMonitorList) {
		ProcessMonitoringProvider.processMonitors = processMonitorList;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public Object getFilterState() {
		return this.processMonitorFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.processMonitorFilter = (ProcessMonitoringFilter) state;
	}

	public ProcessMonitoringFilter getProcessMonitorFilter() {
		return this.processMonitorFilter;
	}

	public void setProcessMonitorFilter(final ProcessMonitoringFilter processMonitorFilter) {
		this.processMonitorFilter = processMonitorFilter;
		ProcessMonitoringProvider.processMonitors = this.filterProcessMonitors(
				ProcessMonitoringProvider.processMonitors, processMonitorFilter);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : ProcessMonitoringProvider.processMonitors) {
			final ProcessMonitor processMonitor = (ProcessMonitor) element;
			if (processMonitor.getID() == entryId) {
				ProcessMonitoringProvider.selectedProcessMonitors.add(processMonitor);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final ProcessMonitor processMonitor : ProcessMonitoringProvider.processMonitors) {
			if (processMonitor.getID() == entryId) {
				ProcessMonitoringProvider.selectedProcessMonitors.remove(processMonitor);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final ProcessMonitor processMonitor : ProcessMonitoringProvider.selectedProcessMonitors) {
			if (processMonitor.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final ProcessMonitor processMonitor : ProcessMonitoringProvider.selectedProcessMonitors) {
			ProcessMonitoringProvider.processMonitors.remove(processMonitor);
		}
	}

	public void selectAllEntries() {
		for (final ProcessMonitor processMonitor : ProcessMonitoringProvider.processMonitors) {
			ProcessMonitoringProvider.selectedProcessMonitors.add(processMonitor);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final ProcessMonitor processMonitor : ProcessMonitoringProvider.processMonitors) {
			if (processMonitor.getID() == entryId) {
				return processMonitor;
			}
		}
		return null;
	}

}
