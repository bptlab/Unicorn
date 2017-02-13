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
import de.hpi.unicorn.bpmn.element.BPMNProcess;

/**
 * This class is the provider for {@link BPMNProcess}es. A filter can be
 * specified to return only some BPMN processes.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class BPMNProcessProvider extends AbstractDataProvider implements ISortableDataProvider<BPMNProcess, String>,
		IFilterStateLocator {

	private static List<BPMNProcess> processes;
	private final ISortState sortState = new SingleSortState();
	private BPMNProcessFilter processFilter = new BPMNProcessFilter();
	private final List<BPMNProcess> selectedProcesses;

	/**
	 * Constructor for providing {@link BPMNProcess}es.
	 */
	public BPMNProcessProvider() {
		BPMNProcessProvider.processes = this.filterBPMNProcesses(BPMNProcess.findAll(), this.processFilter);
		this.selectedProcesses = new ArrayList<BPMNProcess>();
	}

	@Override
	public void detach() {
		// Processs = null;
	}

	@Override
	public Iterator<? extends BPMNProcess> iterator(final long first, final long count) {
		final List<BPMNProcess> data = BPMNProcessProvider.processes;
		Collections.sort(data, new Comparator<BPMNProcess>() {
			@Override
			public int compare(final BPMNProcess e1, final BPMNProcess e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	private List<BPMNProcess> filterBPMNProcesses(final List<BPMNProcess> processesToFilter,
			final BPMNProcessFilter processFilter) {
		final List<BPMNProcess> returnedProcesses = new ArrayList<BPMNProcess>();
		for (final BPMNProcess process : processesToFilter) {
			if (processFilter.match(process)) {
				returnedProcesses.add(process);
			}
		}
		return returnedProcesses;
	}

	@Override
	public IModel<BPMNProcess> model(final BPMNProcess BPMNProcess) {
		return Model.of(BPMNProcess);
	}

	@Override
	public long size() {
		return BPMNProcessProvider.processes.size();
	}

	public static List<BPMNProcess> getBPMNProcesses() {
		return BPMNProcessProvider.processes;
	}

	public static void setBPMNProcesses(final List<BPMNProcess> processList) {
		BPMNProcessProvider.processes = processList;
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
		this.processFilter = (BPMNProcessFilter) state;
	}

	public BPMNProcessFilter getBPMNProcessFilter() {
		return this.processFilter;
	}

	public void setBPMNProcessFilter(final BPMNProcessFilter processFilter) {
		this.processFilter = processFilter;
		BPMNProcessProvider.processes = this.filterBPMNProcesses(BPMNProcess.findAll(), processFilter);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : BPMNProcessProvider.processes) {
			final BPMNProcess process = (BPMNProcess) element;
			if (process.getID() == entryId) {
				this.selectedProcesses.add(process);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final Object element : BPMNProcessProvider.processes) {
			final BPMNProcess process = (BPMNProcess) element;
			if (process.getID() == entryId) {
				this.selectedProcesses.remove(process);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final BPMNProcess process : this.selectedProcesses) {
			if (process.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final BPMNProcess process : this.selectedProcesses) {
			BPMNProcessProvider.processes.remove(process);
			process.remove();
		}
	}

	public void selectAllEntries() {
		for (final BPMNProcess bpmnProcess : BPMNProcessProvider.processes) {
			this.selectedProcesses.add(bpmnProcess);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final BPMNProcess process : BPMNProcessProvider.processes) {
			if (process.getID() == entryId) {
				return process;
			}
		}
		return null;
	}
}
