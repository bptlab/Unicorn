/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;
import de.hpi.unicorn.query.PatternQuery;

/**
 * Wraps the tree of BPMN components and their event types from the monitoring
 * points for a tree table visualization.
 * 
 * @author micha
 * 
 * @param
 */
public class ProcessInstanceMonitoringTreeTableProvider extends AbstractDataProvider implements
		ISortableTreeProvider<ProcessInstanceMonitoringTreeTableElement, String> {

	private static final long serialVersionUID = 1L;
	private List<ProcessInstanceMonitoringTreeTableElement> treeTableElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
	private List<ProcessInstanceMonitoringTreeTableElement> treeTableRootElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
	private List<ProcessInstanceMonitoringTreeTableElement> selectedTreeTableElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
	private ProcessInstanceMonitor processInstanceMonitor;
	private EventTree<PatternQuery> queryTree;
	private EventTree<AbstractBPMNElement> bpmnProcessDecompositionTree;

	public ProcessInstanceMonitoringTreeTableProvider(final ProcessInstanceMonitor processInstanceMonitor) {
		this.processInstanceMonitor = processInstanceMonitor;
		this.refreshTreeTable();
	}

	private void refreshTreeTable() {
		if (this.processInstanceMonitor != null) {
			this.createQueryTree();
			this.createTreeTableElements(this.queryTree);
		}
	}

	private void createQueryTree() {
		this.bpmnProcessDecompositionTree = this.processInstanceMonitor.getProcessInstance().getProcess()
				.getProcessDecompositionTree();
		this.queryTree = new EventTree<PatternQuery>();
		// Query enthält ihre BPMN-Elemente --> And-Component, enthält Childs
		for (final AbstractBPMNElement rootElement : this.bpmnProcessDecompositionTree.getRootElements()) {
			this.addQueryToTree(null, this.bpmnProcessDecompositionTree.getChildren(rootElement));
		}
	}

	private void addQueryToTree(final PatternQuery parentQuery, final List<AbstractBPMNElement> bpmnElements) {
		final PatternQuery query = this.findQueryWithElements(new HashSet<AbstractBPMNElement>(bpmnElements));
		if (query != null) {
			this.queryTree.addChild(parentQuery, query);
		}
		// Childs
		for (final AbstractBPMNElement element : bpmnElements) {
			if (this.bpmnProcessDecompositionTree.hasChildren(element)) {
				this.addQueryToTree(query, this.bpmnProcessDecompositionTree.getChildren(element));
			} else if (element.hasMonitoringPoints()) { /*
														 * Element (Task oder
														 * Event) mit
														 * MonitoringPoints
														 */
				final Set<AbstractBPMNElement> elements = new HashSet<AbstractBPMNElement>();
				elements.add(element);
				final PatternQuery elementQuery = this.findQueryWithElements(elements);
				if (elementQuery != null) {
					this.queryTree.addChild(query, elementQuery);
				}
			}
		}
	}

	private PatternQuery findQueryWithElements(final Set<AbstractBPMNElement> bpmnElements) {
		for (final PatternQuery query : this.processInstanceMonitor.getQueries()) {
			if (query.getMonitoredElements().containsAll(bpmnElements)
					&& bpmnElements.containsAll(query.getMonitoredElements())) {
				return query;
			}
		}
		return null;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends ProcessInstanceMonitoringTreeTableElement> getRoots() {
		return this.getRootElements().iterator();
	}

	private List<ProcessInstanceMonitoringTreeTableElement> getRootElements() {
		this.treeTableRootElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
		for (final ProcessInstanceMonitoringTreeTableElement element : this.treeTableElements) {
			if (element.getParent() == null) {
				this.treeTableRootElements.add(element);
			}
		}
		return this.treeTableRootElements;
	}

	@Override
	public boolean hasChildren(final ProcessInstanceMonitoringTreeTableElement node) {
		return !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends ProcessInstanceMonitoringTreeTableElement> getChildren(
			final ProcessInstanceMonitoringTreeTableElement node) {
		return node.getChildren().iterator();
	}

	@Override
	public ProcessInstanceMonitoringTreeTableElementModel model(final ProcessInstanceMonitoringTreeTableElement node) {
		return new ProcessInstanceMonitoringTreeTableElementModel(this.getRootElements(), node);
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final ProcessInstanceMonitoringTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.add(treeTableElement);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final ProcessInstanceMonitoringTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.remove(treeTableElement);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final ProcessInstanceMonitoringTreeTableElement treeTableElement : this.selectedTreeTableElements) {
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
		for (final ProcessInstanceMonitoringTreeTableElement element : this.treeTableElements) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

	public List<ProcessInstanceMonitoringTreeTableElement> getTreeTableElements() {
		return this.treeTableElements;
	}

	public void addTreeTableElement(final ProcessInstanceMonitoringTreeTableElement treeTableElement) {
		this.treeTableElements.add(treeTableElement);
		if (!this.selectedTreeTableElements.isEmpty()) {
			final ProcessInstanceMonitoringTreeTableElement parent = this.selectedTreeTableElements.get(0);
			parent.getChildren().add(treeTableElement);
			treeTableElement.setParent(parent);
		}
	}

	public void addTreeTableElementWithParent(final ProcessInstanceMonitoringTreeTableElement treeTableElement,
			final ProcessInstanceMonitoringTreeTableElement parent) {
		this.treeTableElements.add(treeTableElement);
		parent.getChildren().add(treeTableElement);
		treeTableElement.setParent(parent);
	}

	public void setTreeTableElements(final List<ProcessInstanceMonitoringTreeTableElement> treeTableElements) {
		this.treeTableElements = treeTableElements;
	}

	public void deleteSelectedEntries() {
		for (final ProcessInstanceMonitoringTreeTableElement element : this.selectedTreeTableElements) {
			element.remove();
		}
		this.treeTableElements.removeAll(this.selectedTreeTableElements);
		this.selectedTreeTableElements.clear();
	}

	public List<ProcessInstanceMonitoringTreeTableElement> getSelectedTreeTableElements() {
		return this.selectedTreeTableElements;
	}

	public List<ProcessInstanceMonitoringTreeTableElement> getRootTreeTableElements() {
		return this.getRootElements();
	}

	private void createTreeTableElements(final EventTree<PatternQuery> queryTree) {
		for (final PatternQuery rootElement : queryTree.getRootElements()) {
			this.addElementToTree(null, rootElement, queryTree);
		}
	}

	private void addElementToTree(final ProcessInstanceMonitoringTreeTableElement parent, final PatternQuery query,
			final EventTree<PatternQuery> queryTree) {
		final ProcessInstanceMonitoringTreeTableElement treeTableElement = this.createTreeTableElement(query);
		treeTableElement.setParent(parent);
		this.treeTableElements.add(treeTableElement);
		if (parent == null) {
			this.treeTableRootElements.add(treeTableElement);
		}
		if (queryTree.hasChildren(query)) {
			for (final PatternQuery child : queryTree.getChildren(query)) {
				this.addElementToTree(treeTableElement, child, queryTree);
			}
		}
	}

	private ProcessInstanceMonitoringTreeTableElement createTreeTableElement(final PatternQuery query) {
		final ProcessInstanceMonitoringTreeTableElement element = new ProcessInstanceMonitoringTreeTableElement(
				this.getNextID(), query, this.processInstanceMonitor);
		return element;
	}

	public void deleteAllEntries() {
		for (final ProcessInstanceMonitoringTreeTableElement element : this.treeTableElements) {
			element.remove();
		}
		this.selectedTreeTableElements.clear();
		this.treeTableElements.clear();
	}

	public ProcessInstanceMonitor getProcessInstanceMonitor() {
		return this.processInstanceMonitor;
	}

	public void setProcessInstanceMonitor(final ProcessInstanceMonitor processInstanceMonitor) {
		this.processInstanceMonitor = processInstanceMonitor;
		// Alte Werte entfernen
		this.treeTableElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
		this.treeTableRootElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
		this.selectedTreeTableElements = new ArrayList<ProcessInstanceMonitoringTreeTableElement>();
		this.refreshTreeTable();
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final ProcessInstanceMonitoringTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return treeTableElement;
			}
		}
		return null;
	}

}
