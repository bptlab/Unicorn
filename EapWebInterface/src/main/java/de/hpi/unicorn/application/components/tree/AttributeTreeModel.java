/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class AttributeTreeModel extends LoadableDetachableModel<TypeTreeNode> {

	private static final long serialVersionUID = 1L;
	private final String ID;
	private final List<TypeTreeNode> treeNodes;

	public AttributeTreeModel(final List<TypeTreeNode> treeNodes, final TypeTreeNode node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getIdentifier();
	}

	public String getID() {
		return this.ID;
	}

	@Override
	protected TypeTreeNode load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof AttributeTreeModel) {
			return ((AttributeTreeModel) object).getID().equals(this.ID);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.ID.hashCode();
	}

	private TypeTreeNode findTreeElement(final List<TypeTreeNode> treeNodes, final String id) {
		for (final TypeTreeNode treeElement : treeNodes) {
			if (treeElement.getIdentifier().equals(id)) {
				return treeElement;
			}
			final TypeTreeNode child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
