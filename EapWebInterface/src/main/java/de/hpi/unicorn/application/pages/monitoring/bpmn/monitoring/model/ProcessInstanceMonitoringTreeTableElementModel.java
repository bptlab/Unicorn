/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.util.Collection;
import java.util.List;

import org.apache.wicket.model.LoadableDetachableModel;

/**
 * This model provides a tree of
 * {@link ProcessInstanceMonitoringTreeTableElement}s.
 * 
 * @author micha
 */
public class ProcessInstanceMonitoringTreeTableElementModel extends
		LoadableDetachableModel<ProcessInstanceMonitoringTreeTableElement> {

	private static final long serialVersionUID = 1L;
	private final int ID;
	private final List<ProcessInstanceMonitoringTreeTableElement> treeNodes;

	public ProcessInstanceMonitoringTreeTableElementModel(
			final List<ProcessInstanceMonitoringTreeTableElement> treeNodes,
			final ProcessInstanceMonitoringTreeTableElement node) {
		super(node);
		this.treeNodes = treeNodes;
		this.ID = node.getID();
	}

	public int getID() {
		return this.ID;
	}

	@Override
	protected ProcessInstanceMonitoringTreeTableElement load() {
		return this.findTreeElement(this.treeNodes, this.ID);
	}

	@Override
	public boolean equals(final Object object) {
		if (object instanceof ProcessInstanceMonitoringTreeTableElementModel) {
			return ((ProcessInstanceMonitoringTreeTableElementModel) object).getID() == this.ID;
		}
		return false;
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	private ProcessInstanceMonitoringTreeTableElement findTreeElement(
			final Collection<ProcessInstanceMonitoringTreeTableElement> treeNodes, final int id) {
		for (final ProcessInstanceMonitoringTreeTableElement treeElement : treeNodes) {
			if (treeElement.getID() == id) {
				return treeElement;
			}
			final ProcessInstanceMonitoringTreeTableElement child = this.findTreeElement(treeElement.getChildren(), id);
			if (child != null) {
				return child;
			}
		}
		return null;
	}
}
