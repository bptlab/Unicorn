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
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * wraps the given tree nodes
 */
public class AttributeTreeProvider extends AbstractDataProvider implements ISortableTreeProvider<TypeTreeNode, String> {

	private static final long serialVersionUID = 1L;
	private List<TypeTreeNode> treeNodes;
	private final List<TypeTreeNode> selectedTreeNodes;

	/**
	 * constructor
	 */
	public AttributeTreeProvider() {
		this.treeNodes = new ArrayList<TypeTreeNode>();
		this.selectedTreeNodes = new ArrayList<TypeTreeNode>();
	}

	/**
	 * constructor
	 * 
	 * @param list
	 *            root nodes of the tree, child nodes are accessed by this
	 *            component automatically
	 */
	public AttributeTreeProvider(final List<TypeTreeNode> list) {
		this();
		this.treeNodes = list;
	}

	@Override
	public void detach() {
	}

	@Override
	public Iterator<? extends TypeTreeNode> getRoots() {
		return this.treeNodes.iterator();
	}

	@Override
	public boolean hasChildren(final TypeTreeNode node) {
		return node.getParent() == null || !node.getChildren().isEmpty();
	}

	@Override
	public Iterator<? extends TypeTreeNode> getChildren(final TypeTreeNode node) {
		return node.getChildren().iterator();
	}

	@Override
	public AttributeTreeModel model(final TypeTreeNode node) {
		return new AttributeTreeModel(this.treeNodes, node);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final TypeTreeNode node : this.treeNodes) {
			if (node.getID() == entryId) {
				this.selectedTreeNodes.add(node);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final TypeTreeNode node : this.treeNodes) {
			if (node.getID() == entryId) {
				this.selectedTreeNodes.remove(node);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final TypeTreeNode node : this.selectedTreeNodes) {
			if (node.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public List<TypeTreeNode> getSelectedAttributes() {
		return this.selectedTreeNodes;
	}

	@Override
	public ISortState<String> getSortState() {
		return new SingleSortState<String>();
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final TypeTreeNode node : this.treeNodes) {
			if (node.getID() == entryId) {
				return node;
			}
		}
		return null;
	}
}
