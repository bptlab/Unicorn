/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.RPSTBuilder;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.event.collection.EventTree;

/**
 * Wraps the tree of BPMN components and their event types from the monitoring
 * points for a tree table visualization.
 * 
 * @author micha
 * 
 * @param
 */
public class BPMNTreeTableProvider extends AbstractDataProvider implements
		ISortableTreeProvider<BPMNTreeTableElement, String> {

	private static final long serialVersionUID = 1L;
	private List<BPMNTreeTableElement> treeTableElements;
	private List<BPMNTreeTableElement> treeTableRootElements;
	private final List<BPMNTreeTableElement> selectedTreeTableElements = new ArrayList<BPMNTreeTableElement>();
	private BPMNProcess process;
	private EventTree<AbstractBPMNElement> bpmnComponentTree;

	public BPMNTreeTableProvider(final BPMNProcess process) {
		this.treeTableElements = new ArrayList<BPMNTreeTableElement>();
		this.process = process;
		if (process != null) {
			this.bpmnComponentTree = new RPSTBuilder(process).getProcessDecompositionTree();
			this.createTreeTableElements(this.bpmnComponentTree);
		}
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends BPMNTreeTableElement> getRoots() {
		return this.getRootElements().iterator();
	}

	private List<BPMNTreeTableElement> getRootElements() {
		this.treeTableRootElements = new ArrayList<BPMNTreeTableElement>();
		for (final BPMNTreeTableElement element : this.treeTableElements) {
			if (element.getParent() == null) {
				this.treeTableRootElements.add(element);
			}
		}
		return this.treeTableRootElements;
	}

	@Override
	public boolean hasChildren(final BPMNTreeTableElement node) {
		return !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends BPMNTreeTableElement> getChildren(final BPMNTreeTableElement node) {
		return node.getChildren().iterator();
	}

	@Override
	public BPMNTreeTableElementModel model(final BPMNTreeTableElement node) {
		return new BPMNTreeTableElementModel(this.getRootElements(), node);
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final BPMNTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.add(treeTableElement);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final BPMNTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.remove(treeTableElement);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final BPMNTreeTableElement treeTableElement : this.selectedTreeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns the next free ID for an new element.
	 * 
	 * @return
	 */
	public int getNextID() {
		int highestNumber = 0;
		for (final BPMNTreeTableElement element : this.treeTableElements) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

	public List<BPMNTreeTableElement> getTreeTableElements() {
		return this.treeTableElements;
	}

	public void addTreeTableElement(final BPMNTreeTableElement treeTableElement) {
		this.treeTableElements.add(treeTableElement);
		if (!this.selectedTreeTableElements.isEmpty()) {
			final BPMNTreeTableElement parent = this.selectedTreeTableElements.get(0);
			parent.getChildren().add(treeTableElement);
			treeTableElement.setParent(parent);
		}
	}

	public void addTreeTableElementWithParent(final BPMNTreeTableElement treeTableElement,
			final BPMNTreeTableElement parent) {
		this.treeTableElements.add(treeTableElement);
		parent.getChildren().add(treeTableElement);
		treeTableElement.setParent(parent);
	}

	public void setTreeTableElements(final List<BPMNTreeTableElement> treeTableElements) {
		this.treeTableElements = treeTableElements;
	}

	public void deleteSelectedEntries() {
		for (final BPMNTreeTableElement element : this.selectedTreeTableElements) {
			element.remove();
		}
		this.treeTableElements.removeAll(this.selectedTreeTableElements);
		this.selectedTreeTableElements.clear();
	}

	public List<BPMNTreeTableElement> getSelectedTreeTableElements() {
		return this.selectedTreeTableElements;
	}

	public List<BPMNTreeTableElement> getRootTreeTableElements() {
		return this.getRootElements();
	}

	private void createTreeTableElements(final EventTree<AbstractBPMNElement> bpmnComponentTree) {
		for (final AbstractBPMNElement rootElement : bpmnComponentTree.getRootElements()) {
			this.addElementToTree(null, rootElement, bpmnComponentTree);
		}
	}

	private void addElementToTree(final BPMNTreeTableElement parent, final AbstractBPMNElement bpmnElement,
			final EventTree<AbstractBPMNElement> bpmnComponentTree) {
		final BPMNTreeTableElement treeTableElement = this.createTreeTableElement(bpmnElement);
		treeTableElement.setParent(parent);
		this.treeTableElements.add(treeTableElement);
		if (parent == null) {
			this.treeTableRootElements.add(treeTableElement);
		}
		if (bpmnComponentTree.hasChildren(bpmnElement)) {
			for (final AbstractBPMNElement child : bpmnComponentTree.getChildren(bpmnElement)) {
				this.addElementToTree(treeTableElement, child, bpmnComponentTree);
			}
		}
	}

	private BPMNTreeTableElement createTreeTableElement(final AbstractBPMNElement bpmnElement) {
		final BPMNTreeTableElement element = new BPMNTreeTableElement(this.getNextID(), bpmnElement);
		if (bpmnElement != null && bpmnElement.hasMonitoringPoints() && !(bpmnElement instanceof Component)) {
			// TODO: Was ist bei mehreren Monitoring-Points?
			element.addMonitoringPoints(bpmnElement.getMonitoringPoints());
		}
		return element;
	}

	public void deleteAllEntries() {
		for (final BPMNTreeTableElement element : this.treeTableElements) {
			element.remove();
		}
		this.selectedTreeTableElements.clear();
		this.treeTableElements.clear();
	}

	public BPMNProcess getProcess() {
		return this.process;
	}

	public void setProcess(final BPMNProcess process) {
		this.process = process;
		if (process != null) {
			this.bpmnComponentTree = new RPSTBuilder(process).getProcessDecompositionTree();
			this.createTreeTableElements(this.bpmnComponentTree);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final BPMNTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return treeTableElement;
			}
		}
		return null;
	}

}
