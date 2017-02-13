/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.tree.ISortableTreeProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * wraps the given tree nodes
 *
 * @param <T>
 */
/**
 * @author micha
 * 
 * @param <T>
 */
public class TreeTableProvider<T> extends AbstractDataProvider implements
		ISortableTreeProvider<EventTreeElement<T>, String> {

	private static final long serialVersionUID = 1L;
	private List<EventTreeElement<T>> rootElements;
	private final List<EventTreeElement<T>> selectedElements = new ArrayList<EventTreeElement<T>>();

	public TreeTableProvider() {
		this.rootElements = new ArrayList<EventTreeElement<T>>();
	}

	/**
	 * constructor
	 * 
	 * @param treeNodes
	 *            root nodes of the tree, child nodes are accessed by this
	 *            component automatically
	 */
	public TreeTableProvider(final List<EventTreeElement<T>> treeNodes) {
		this.rootElements = treeNodes;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends EventTreeElement<T>> getRoots() {
		return this.getRootElements().iterator();
	}

	private List<EventTreeElement<T>> getRootElements() {
		return this.rootElements;
	}

	private List<EventTreeElement<T>> getElements() {
		final List<EventTreeElement<T>> elements = new ArrayList<EventTreeElement<T>>();
		for (final EventTreeElement<T> root : this.rootElements) {
			if (root.hasChildren()) {
				this.addElementToSet(root, elements);
			}
			elements.add(root);
		}
		return elements;
	}

	public void setRootElements(final List<EventTreeElement<T>> rootElements) {
		this.rootElements = rootElements;
	}

	private void addElementToSet(final EventTreeElement<T> element, final List<EventTreeElement<T>> elements) {
		for (final EventTreeElement<T> child : element.getChildren()) {
			if (child.hasChildren()) {
				this.addElementToSet(child, elements);
			}
			elements.add(child);
		}
	}

	@Override
	public boolean hasChildren(final EventTreeElement<T> node) {
		return node.hasChildren();
	}

	@Override
	public Iterator<? extends EventTreeElement<T>> getChildren(final EventTreeElement<T> node) {
		return node.getChildren().iterator();
	}

	@Override
	public TreeElementModel<T> model(final EventTreeElement<T> node) {
		return new TreeElementModel<T>(this.getRootElements(), node);
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final EventTreeElement<T> treeTableElement : this.getElements()) {
			if (treeTableElement.getID() == entryId) {
				this.selectedElements.add(treeTableElement);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final EventTreeElement<T> treeTableElement : this.getElements()) {
			if (treeTableElement.getID() == entryId) {
				this.selectedElements.remove(treeTableElement);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final EventTreeElement<T> treeTableElement : this.selectedElements) {
			if (treeTableElement.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final EventTreeElement<T> treeTableElement : this.getElements()) {
			if (treeTableElement.getID() == entryId) {
				return treeTableElement;
			}
		}
		return null;
	}

	/**
	 * Returns the next free ID for an new element.
	 * 
	 * @return
	 */
	public int getNextID() {
		int highestNumber = 0;
		for (final EventTreeElement<T> element : this.getElements()) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

	public List<EventTreeElement<T>> getTreeTableElements() {
		return this.getElements();
	}

	public List<EventTreeElement<T>> getSelectedTreeTableElements() {
		return this.selectedElements;
	}

	public List<EventTreeElement<T>> getRootTreeTableElements() {
		return this.getRootElements();
	}

	public EventTree<T> getModelAsTree() {
		final EventTree<T> tree = new EventTree<T>();
		for (final EventTreeElement<T> element : this.rootElements) {
			this.addElementToTree(null, element, tree);
		}
		return tree;
	}

	private void addElementToTree(final EventTreeElement<T> parent, final EventTreeElement<T> element,
			final EventTree<T> tree) {
		if (parent != null) {
			tree.addChild(parent.getValue(), element.getValue());
		} else {
			tree.addChild(null, element.getValue());
		}
		for (final EventTreeElement<T> child : element.getChildren()) {
			this.addElementToTree(element, child, tree);
		}
	}

}
