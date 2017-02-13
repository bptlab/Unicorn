/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn.model;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * This model provides a tree of {@link BPMNTreeTableElement}s.
 * 
 * @author micha
 */
public class BPMNTreeTableElementModel extends LoadableDetachableModel<BPMNTreeTableElement> {

	private static final long serialVersionUID = 1L;
	private final int ID;
	private final List<BPMNTreeTableElement> treeNodes;

	public BPMNTreeTableElementModel(final List<BPMNTreeTableElement> treeNodes, final BPMNTreeTableElement node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID();
	}

	public int getID() {
		return this.ID;
	}

	@Override
	protected BPMNTreeTableElement load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof BPMNTreeTableElementModel) {
			return ((BPMNTreeTableElementModel) object).getID() == this.ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private BPMNTreeTableElement findTreeElement(final Collection<BPMNTreeTableElement> treeNodes, final int id) {
		for (final BPMNTreeTableElement treeElement : treeNodes) {
			if (treeElement.getID() == id) {
				return treeElement;
			}
			final BPMNTreeTableElement child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
