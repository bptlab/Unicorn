/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * This model provides a tree of {@link ProcessAnalysingTreeTableElement}s.
 * 
 * @author micha
 */
public class ProcessAnalysingTreeTableElementModel extends LoadableDetachableModel<ProcessAnalysingTreeTableElement> {

	private static final long serialVersionUID = 1L;
	private final int ID;
	private final List<ProcessAnalysingTreeTableElement> treeNodes;

	public ProcessAnalysingTreeTableElementModel(final List<ProcessAnalysingTreeTableElement> treeNodes,
			final ProcessAnalysingTreeTableElement node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID();
	}

	public int getID() {
		return this.ID;
	}

	@Override
	protected ProcessAnalysingTreeTableElement load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof ProcessAnalysingTreeTableElementModel) {
			return ((ProcessAnalysingTreeTableElementModel) object).getID() == this.ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private ProcessAnalysingTreeTableElement findTreeElement(
			final Collection<ProcessAnalysingTreeTableElement> treeNodes, final int id) {
		for (final ProcessAnalysingTreeTableElement treeElement : treeNodes) {
			if (treeElement.getID() == id) {
				return treeElement;
			}
			final ProcessAnalysingTreeTableElement child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
