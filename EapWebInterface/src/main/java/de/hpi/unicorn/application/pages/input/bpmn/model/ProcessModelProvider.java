/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.bpmn.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;

/**
 * This class is the provider for {@link BPMNProcess}es.
 * 
 * @author micha
 */
public class ProcessModelProvider extends AbstractDataProvider implements IDataProvider<AbstractBPMNElement>,
		ISortableDataProvider<AbstractBPMNElement, String> {

	private static final long serialVersionUID = 1L;
	private BPMNProcess process;
	private List<AbstractBPMNElement> elements;
	private List<AbstractBPMNElement> selectedElements;

	/**
	 * Constructor for providing {@link BPMNProcess}es.
	 */
	public ProcessModelProvider() {
		this.elements = new ArrayList<AbstractBPMNElement>();
		this.selectedElements = new ArrayList<AbstractBPMNElement>();
	}

	public ProcessModelProvider(final BPMNProcess process) {
		this.process = process;
		this.elements = process.getBPMNElements();
		this.selectedElements = new ArrayList<AbstractBPMNElement>();
	}

	@Override
	public void detach() {
		// events = null;
	}

	@Override
	public Iterator<AbstractBPMNElement> iterator(final long first, final long count) {
		final List<AbstractBPMNElement> data = this.elements;
		Collections.sort(data, new Comparator<AbstractBPMNElement>() {
			@Override
			public int compare(final AbstractBPMNElement e1, final AbstractBPMNElement e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	@Override
	public IModel<AbstractBPMNElement> model(final AbstractBPMNElement element) {
		return Model.of(element);
	}

	@Override
	public long size() {
		return this.elements.size();
	}

	public List<AbstractBPMNElement> getElements() {
		return this.elements;
	}

	public List<AbstractBPMNElement> getSelectedElements() {
		return this.selectedElements;
	}

	public void setElements(final List<AbstractBPMNElement> elementList) {
		this.elements = elementList;
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final AbstractBPMNElement element : this.elements) {
			if (element.getID() == entryId) {
				this.selectedElements.add(element);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final AbstractBPMNElement element : this.elements) {
			if (element.getID() == entryId) {
				this.selectedElements.remove(element);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final AbstractBPMNElement element : this.selectedElements) {
			if (element.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final AbstractBPMNElement element : this.selectedElements) {
			this.elements.remove(element);
			element.remove();
		}
	}

	public void selectAllEntries() {
		for (final AbstractBPMNElement element : this.elements) {
			if (!this.selectedElements.contains(element)) {
				this.selectedElements.add(element);
			}
		}
	}

	public void setProcessModel(final BPMNProcess processModel) {
		this.process = processModel;
		if (this.process != null) {
			this.elements = this.process.getBPMNElementsWithOutSequenceFlows();
		} else {
			this.elements = new ArrayList<AbstractBPMNElement>();
		}
		this.selectedElements = new ArrayList<AbstractBPMNElement>();
	}

	@Override
	public ISortState<String> getSortState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final AbstractBPMNElement element : this.elements) {
			if (element.getID() == entryId) {
				return element;
			}
		}
		return null;
	}

}
