/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

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
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class is the provider for {@link CorrelationProcess}es. A filter can be
 * specified to return only some processes.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class ProcessProvider extends AbstractDataProvider implements ISortableDataProvider<CorrelationProcess, String>,
		IFilterStateLocator {

	private static List<CorrelationProcess> processes;
	private final ISortState sortState = new SingleSortState();
	private ProcessFilter processFilter = new ProcessFilter();
	private final List<CorrelationProcess> selectedProcesses;

	/**
	 * Constructor for providing {@link CorrelationProcess}es.
	 */
	public ProcessProvider() {
		ProcessProvider.processes = this.filterProcesses(CorrelationProcess.findAll(), this.processFilter);
		this.selectedProcesses = new ArrayList<CorrelationProcess>();
	}

	@Override
	public void detach() {
		// Processs = null;
	}

	@Override
	public Iterator<? extends CorrelationProcess> iterator(final long first, final long count) {
		final List<CorrelationProcess> data = ProcessProvider.processes;
		Collections.sort(data, new Comparator<CorrelationProcess>() {
			@Override
			public int compare(final CorrelationProcess e1, final CorrelationProcess e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	private List<CorrelationProcess> filterProcesses(final List<CorrelationProcess> processesToFilter,
			final ProcessFilter processFilter) {
		final List<CorrelationProcess> returnedProcesses = new ArrayList<CorrelationProcess>();
		for (final CorrelationProcess process : processesToFilter) {
			if (processFilter.match(process)) {
				returnedProcesses.add(process);
			}
		}
		return returnedProcesses;
	}

	@Override
	public IModel<CorrelationProcess> model(final CorrelationProcess Process) {
		return Model.of(Process);
	}

	@Override
	public long size() {
		return ProcessProvider.processes.size();
	}

	public static List<CorrelationProcess> getProcesses() {
		return ProcessProvider.processes;
	}

	public static void setProcesses(final List<CorrelationProcess> processList) {
		ProcessProvider.processes = processList;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public Object getFilterState() {
		return this.processFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.processFilter = (ProcessFilter) state;
	}

	public ProcessFilter getProcessFilter() {
		return this.processFilter;
	}

	public void setProcessFilter(final ProcessFilter processFilter) {
		this.processFilter = processFilter;
		ProcessProvider.processes = this.filterProcesses(CorrelationProcess.findAll(), processFilter);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : ProcessProvider.processes) {
			final CorrelationProcess process = (CorrelationProcess) element;
			if (process.getID() == entryId) {
				this.selectedProcesses.add(process);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final CorrelationProcess process : ProcessProvider.processes) {
			if (process.getID() == entryId) {
				this.selectedProcesses.remove(process);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final CorrelationProcess process : this.selectedProcesses) {
			if (process.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final CorrelationProcess process : this.selectedProcesses) {
			ProcessProvider.processes.remove(process);
			process.remove();
		}
	}

	public void selectAllEntries() {
		for (final CorrelationProcess process : ProcessProvider.processes) {
			this.selectedProcesses.add(process);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final CorrelationProcess process : ProcessProvider.processes) {
			if (process.getID() == entryId) {
				return process;
			}
		}
		return null;
	}
}
