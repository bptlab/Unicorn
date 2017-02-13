/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator.model;

import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

public class SimulationTreeTableElementModel<T> extends LoadableDetachableModel<SimulationTreeTableElement<T>> {

	private static final long serialVersionUID = 1L;
	private final int ID;
	private final List<SimulationTreeTableElement<T>> treeNodes;

	public SimulationTreeTableElementModel(final List<SimulationTreeTableElement<T>> treeNodes,
			final SimulationTreeTableElement<T> node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID();
	}

	public int getID() {
		return this.ID;
	}

	@Override
	protected SimulationTreeTableElement<T> load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof SimulationTreeTableElementModel) {
			return ((SimulationTreeTableElementModel<T>) object).getID() == this.ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private SimulationTreeTableElement<T> findTreeElement(final List<SimulationTreeTableElement<T>> treeNodes,
			final int id) {
		for (final SimulationTreeTableElement<T> treeElement : treeNodes) {
			if (treeElement.getID() == id) {
				return treeElement;
			}
			final SimulationTreeTableElement<T> child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
