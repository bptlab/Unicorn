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

import de.hpi.unicorn.event.collection.EventTreeElement;

public class TreeElementModel<T> extends LoadableDetachableModel<EventTreeElement<T>> {

	private static final long serialVersionUID = 1L;
	private final String ID;
	private final List<EventTreeElement<T>> treeNodes;

	public TreeElementModel(final List<EventTreeElement<T>> treeNodes, final EventTreeElement<T> node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID() + node.getXPath();
	}

	public String getID() {
		return this.ID;
	}

	@Override
	protected EventTreeElement<T> load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof TreeElementModel) {
			return ((TreeElementModel<T>) object).getID().equals(this.ID);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.ID.hashCode();
	}

	private EventTreeElement<T> findTreeElement(final List<EventTreeElement<T>> treeNodes, final String id) {
		for (final EventTreeElement<T> treeElement : treeNodes) {
			if ((treeElement.getID() + treeElement.getXPath()).equals(id)) {
				return treeElement;
			}
			final EventTreeElement<T> child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
