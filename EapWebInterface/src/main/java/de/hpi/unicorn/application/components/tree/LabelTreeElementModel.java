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

public class LabelTreeElementModel<T> extends LoadableDetachableModel<LabelTreeElement<T>> {

	private static final long serialVersionUID = 1L;
	private final String ID;
	private final List<LabelTreeElement<T>> treeNodes;

	public LabelTreeElementModel(final List<LabelTreeElement<T>> treeNodes, final LabelTreeElement<T> node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID() + node.getXPath();
	}

	public String getID() {
		return this.ID;
	}

	@Override
	protected LabelTreeElement<T> load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof LabelTreeElementModel) {
			return ((LabelTreeElementModel<T>) object).getID().equals(this.ID);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.ID.hashCode();
	}

	private LabelTreeElement<T> findTreeElement(final List<LabelTreeElement<T>> treeNodes, final String id) {
		for (final LabelTreeElement<T> treeElement : treeNodes) {
			if ((treeElement.getID() + treeElement.getXPath()).equals(id)) {
				return treeElement;
			}
			final LabelTreeElement<T> child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
