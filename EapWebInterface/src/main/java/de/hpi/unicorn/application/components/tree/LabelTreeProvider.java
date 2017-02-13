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

/**
 * wraps the given tree nodes
 */
public class LabelTreeProvider<T> extends AbstractDataProvider implements
		ISortableTreeProvider<LabelTreeElement<T>, String> {

	private static final long serialVersionUID = 1L;
	private List<LabelTreeElement<T>> treeNodes;
	private List<LabelTreeElement<T>> selectedTreeNodes;

	/**
	 * constructor
	 */
	public LabelTreeProvider() {
		this.treeNodes = new ArrayList<LabelTreeElement<T>>();
		this.selectedTreeNodes = new ArrayList<LabelTreeElement<T>>();
	}

	/**
	 * constructor
	 * 
	 * @param listhttp
	 *            ://marketplace.eclipse.org/marketplace-client-intro?
	 *            mpc_install=150 root nodes of the tree, child nodes are
	 *            accessed by this component automatically
	 */
	public LabelTreeProvider(final List<LabelTreeElement<T>> list) {
		this();
		this.treeNodes = list;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends LabelTreeElement<T>> getRoots() {
		return this.treeNodes.iterator();
	}

	@Override
	public boolean hasChildren(final LabelTreeElement<T> node) {
		return node.getParent() == null || !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends LabelTreeElement<T>> getChildren(final LabelTreeElement<T> node) {
		return node.getChildren().iterator();
	}

	@Override
	public LabelTreeElementModel<T> model(final LabelTreeElement<T> node) {
		return new LabelTreeElementModel<T>(this.treeNodes, node);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final LabelTreeElement<T> node : this.treeNodes) {
			if (node.getID() == entryId) {
				this.selectedTreeNodes.add(node);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final LabelTreeElement<T> node : this.treeNodes) {
			if (node.getID() == entryId) {
				this.selectedTreeNodes.remove(node);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final LabelTreeElement<T> node : this.selectedTreeNodes) {
			if (node.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public List<LabelTreeElement<T>> getSelectedAttributes() {
		return this.selectedTreeNodes;
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final LabelTreeElement<T> node : this.treeNodes) {
			if (node.getID() == entryId) {
				return node;
			}
		}
		return null;
	}
}
