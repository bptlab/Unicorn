/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model;

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
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;
import de.hpi.unicorn.query.PatternQuery;

/**
 * Wraps the tree of BPMN components and their event types from the monitoring
 * points for a tree table visualization.
 * 
 * @author micha
 * 
 * @param
 */
public class ProcessAnalysingTreeTableProvider extends AbstractDataProvider implements
		ISortableTreeProvider<ProcessAnalysingTreeTableElement, String> {

	private static final long serialVersionUID = 1L;
	private List<ProcessAnalysingTreeTableElement> treeTableElements = new ArrayList<ProcessAnalysingTreeTableElement>();
	private List<ProcessAnalysingTreeTableElement> treeTableRootElements = new ArrayList<ProcessAnalysingTreeTableElement>();
	private List<ProcessAnalysingTreeTableElement> selectedTreeTableElements = new ArrayList<ProcessAnalysingTreeTableElement>();
	private ProcessMonitor processMonitor;
	private EventTree<PatternQuery> queryTree;
	private EventTree<AbstractBPMNElement> bpmnProcessDecompositionTree;

	public ProcessAnalysingTreeTableProvider(final ProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
		this.refreshTreeTable();
	}

	private void refreshTreeTable() {
		if (this.processMonitor != null) {
			this.createQueryTree();
			this.createTreeTableElements(this.queryTree);
		}
	}

	private void createQueryTree() {
		this.bpmnProcessDecompositionTree = this.processMonitor.getProcess().getProcessDecompositionTree();
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
		for (final PatternQuery query : this.processMonitor.getQueries()) {
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
	public Iterator<? extends ProcessAnalysingTreeTableElement> getRoots() {
		return this.getRootElements().iterator();
	}

	private List<ProcessAnalysingTreeTableElement> getRootElements() {
		this.treeTableRootElements = new ArrayList<ProcessAnalysingTreeTableElement>();
		for (final ProcessAnalysingTreeTableElement element : this.treeTableElements) {
			if (element.getParent() == null) {
				this.treeTableRootElements.add(element);
			}
		}
		return this.treeTableRootElements;
	}

	@Override
	public boolean hasChildren(final ProcessAnalysingTreeTableElement node) {
		return !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends ProcessAnalysingTreeTableElement> getChildren(final ProcessAnalysingTreeTableElement node) {
		return node.getChildren().iterator();
	}

	@Override
	public ProcessAnalysingTreeTableElementModel model(final ProcessAnalysingTreeTableElement node) {
		return new ProcessAnalysingTreeTableElementModel(this.getRootElements(), node);
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final ProcessAnalysingTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.add(treeTableElement);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final ProcessAnalysingTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				this.selectedTreeTableElements.remove(treeTableElement);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final ProcessAnalysingTreeTableElement treeTableElement : this.selectedTreeTableElements) {
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
		for (final ProcessAnalysingTreeTableElement element : this.treeTableElements) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

	public List<ProcessAnalysingTreeTableElement> getTreeTableElements() {
		return this.treeTableElements;
	}

	public void addTreeTableElement(final ProcessAnalysingTreeTableElement treeTableElement) {
		this.treeTableElements.add(treeTableElement);
		if (!this.selectedTreeTableElements.isEmpty()) {
			final ProcessAnalysingTreeTableElement parent = this.selectedTreeTableElements.get(0);
			parent.getChildren().add(treeTableElement);
			treeTableElement.setParent(parent);
		}
	}

	public void addTreeTableElementWithParent(final ProcessAnalysingTreeTableElement treeTableElement,
			final ProcessAnalysingTreeTableElement parent) {
		this.treeTableElements.add(treeTableElement);
		parent.getChildren().add(treeTableElement);
		treeTableElement.setParent(parent);
	}

	public void setTreeTableElements(final List<ProcessAnalysingTreeTableElement> treeTableElements) {
		this.treeTableElements = treeTableElements;
	}

	public void deleteSelectedEntries() {
		for (final ProcessAnalysingTreeTableElement element : this.selectedTreeTableElements) {
			element.remove();
		}
		this.treeTableElements.removeAll(this.selectedTreeTableElements);
		this.selectedTreeTableElements.clear();
	}

	public List<ProcessAnalysingTreeTableElement> getSelectedTreeTableElements() {
		return this.selectedTreeTableElements;
	}

	public List<ProcessAnalysingTreeTableElement> getRootTreeTableElements() {
		return this.getRootElements();
	}

	private void createTreeTableElements(final EventTree<PatternQuery> queryTree) {
		for (final PatternQuery rootElement : queryTree.getRootElements()) {
			this.addElementToTree(null, rootElement, queryTree);
		}
	}

	private void addElementToTree(final ProcessAnalysingTreeTableElement parent, final PatternQuery query,
			final EventTree<PatternQuery> queryTree) {
		final ProcessAnalysingTreeTableElement treeTableElement = this.createTreeTableElement(query);
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

	private ProcessAnalysingTreeTableElement createTreeTableElement(final PatternQuery query) {
		final ProcessAnalysingTreeTableElement element = new ProcessAnalysingTreeTableElement(this.getNextID(), query,
				this.processMonitor);
		return element;
	}

	public void deleteAllEntries() {
		for (final ProcessAnalysingTreeTableElement element : this.treeTableElements) {
			element.remove();
		}
		this.selectedTreeTableElements.clear();
		this.treeTableElements.clear();
	}

	public ProcessMonitor getProcessMonitor() {
		return this.processMonitor;
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final ProcessAnalysingTreeTableElement treeTableElement : this.treeTableElements) {
			if (treeTableElement.getID() == entryId) {
				return treeTableElement;
			}
		}
		return null;
	}

	public void setProcessMonitor(final ProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
		// Alte Werte entfernen
		this.treeTableElements = new ArrayList<ProcessAnalysingTreeTableElement>();
		this.treeTableRootElements = new ArrayList<ProcessAnalysingTreeTableElement>();
		this.selectedTreeTableElements = new ArrayList<ProcessAnalysingTreeTableElement>();
		this.refreshTreeTable();

	}

}
