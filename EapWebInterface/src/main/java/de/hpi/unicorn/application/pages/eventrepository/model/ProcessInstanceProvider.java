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
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This class is the provider for {@link CorrelationProcessInstance}s. A filter
 * can be specified to return only some process instances.
 * 
 * @author micha
 */
public class ProcessInstanceProvider extends AbstractDataProvider implements
		ISortableDataProvider<CorrelationProcessInstance, String>, IFilterStateLocator {

	private static final long serialVersionUID = 1L;
	private static List<CorrelationProcessInstance> processInstances;
	private final ISortState sortState = new SingleSortState();
	private ProcessInstanceFilter processInstanceFilter = new ProcessInstanceFilter();
	private final List<CorrelationProcessInstance> selectedProcessInstances;

	/**
	 * Constructor for providing {@link CorrelationProcessInstance}s.
	 */
	public ProcessInstanceProvider() {
		ProcessInstanceProvider.processInstances = this.filterProcessInstances(CorrelationProcessInstance.findAll(),
				this.processInstanceFilter);
		this.selectedProcessInstances = new ArrayList<CorrelationProcessInstance>();
	}

	@Override
	public void detach() {

	}

	@Override
	public Iterator<? extends CorrelationProcessInstance> iterator(final long first, final long count) {
		final List<CorrelationProcessInstance> data = new ArrayList<CorrelationProcessInstance>(
				ProcessInstanceProvider.processInstances);
		Collections.sort(data, new Comparator<CorrelationProcessInstance>() {
			@Override
			public int compare(final CorrelationProcessInstance e1, final CorrelationProcessInstance e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	@Override
	public IModel<CorrelationProcessInstance> model(final CorrelationProcessInstance processInstance) {
		return Model.of(processInstance);
	}

	@Override
	public long size() {
		return ProcessInstanceProvider.processInstances.size();
	}

	public static List<CorrelationProcessInstance> getProcessInstances() {
		return ProcessInstanceProvider.processInstances;
	}

	public static void setProcessInstances(final List<CorrelationProcessInstance> processInstances) {
		ProcessInstanceProvider.processInstances = processInstances;
	}

	public ProcessInstanceFilter getProcessInstanceFilter() {
		return this.processInstanceFilter;
	}

	public void setProcessInstanceFilter(final ProcessInstanceFilter processInstanceFilter) {
		this.processInstanceFilter = processInstanceFilter;
		ProcessInstanceProvider.processInstances = this.filterProcessInstances(CorrelationProcessInstance.findAll(),
				processInstanceFilter);
	}

	private List<CorrelationProcessInstance> filterProcessInstances(
			final List<CorrelationProcessInstance> processInstancesToFilter,
			final ProcessInstanceFilter processInstanceFilter) {
		final List<CorrelationProcessInstance> returnedProcessInstances = new ArrayList<CorrelationProcessInstance>();
		for (final CorrelationProcessInstance processInstance : processInstancesToFilter) {
			if (processInstanceFilter.match(processInstance)) {
				returnedProcessInstances.add(processInstance);
			}
		}
		return returnedProcessInstances;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public Object getFilterState() {
		return this.processInstanceFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.processInstanceFilter = (ProcessInstanceFilter) state;
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : ProcessInstanceProvider.processInstances) {
			final CorrelationProcessInstance processInstance = (CorrelationProcessInstance) element;
			if (processInstance.getID() == entryId) {
				this.selectedProcessInstances.add(processInstance);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final CorrelationProcessInstance processInstance : ProcessInstanceProvider.processInstances) {
			if (processInstance.getID() == entryId) {
				this.selectedProcessInstances.remove(processInstance);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final CorrelationProcessInstance processInstance : this.selectedProcessInstances) {
			if (processInstance.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final CorrelationProcessInstance processInstance : this.selectedProcessInstances) {
			ProcessInstanceProvider.processInstances.remove(processInstance);
			processInstance.remove();
		}
	}

	public void selectAllEntries() {
		for (final CorrelationProcessInstance processInstance : ProcessInstanceProvider.processInstances) {
			this.selectedProcessInstances.add(processInstance);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final CorrelationProcessInstance processInstance : ProcessInstanceProvider.processInstances) {
			if (processInstance.getID() == entryId) {
				return processInstance;
			}
		}
		return null;
	}

}
