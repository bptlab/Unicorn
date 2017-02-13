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

import org.apache.wicket.extensions.markup.html.repeater.tree.ITreeProvider;

import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * wraps the given tree nodes
 * 
 * @param <T>
 */
public class TreeProvider<T> implements ITreeProvider<EventTreeElement<T>> {

	private static final long serialVersionUID = 1L;
	private ArrayList<EventTreeElement<T>> treeNodes;

	/**
	 * constructor
	 * 
	 * @param treeNodes
	 *            root nodes of the tree, child nodes are accessed by this
	 *            component automatically
	 */
	public TreeProvider(final ArrayList<EventTreeElement<T>> treeNodes) {
		this.treeNodes = treeNodes;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends EventTreeElement<T>> getRoots() {
		return this.treeNodes.iterator();
	}

	public void setRoots(final ArrayList<EventTreeElement<T>> treeNodes) {
		this.treeNodes = treeNodes;
	}

	@Override
	public boolean hasChildren(final EventTreeElement<T> node) {
		return node.getParent() == null || !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends EventTreeElement<T>> getChildren(final EventTreeElement<T> node) {
		return node.getChildren().iterator();
	}

	@Override
	public TreeElementModel<T> model(final EventTreeElement<T> node) {
		return new TreeElementModel<T>(this.treeNodes, node);
	}

	/**
	 * Returns the next free ID for an new element.
	 * 
	 * @return
	 */
	public int getNextID() {
		int highestNumber = 0;
		for (final EventTreeElement<T> element : this.treeNodes) {
			highestNumber = element.getID() > highestNumber ? element.getID() : highestNumber;
		}
		return ++highestNumber;
	}

}
