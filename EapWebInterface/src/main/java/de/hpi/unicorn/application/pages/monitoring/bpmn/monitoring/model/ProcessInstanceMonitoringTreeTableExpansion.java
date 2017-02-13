/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * Handles the expansion state of the tree.
 */
public class ProcessInstanceMonitoringTreeTableExpansion implements Set<ProcessInstanceMonitoringTreeTableElement>,
		Serializable {

	private static final long serialVersionUID = 1L;

	private static MetaDataKey<ProcessInstanceMonitoringTreeTableExpansion> KEY = new MetaDataKey<ProcessInstanceMonitoringTreeTableExpansion>() {
	};

	private final Set<Integer> IDs = new HashSet<Integer>();
	private boolean inverse;

	public void expandAll() {
		this.IDs.clear();
		this.inverse = true;
	}

	public void collapseAll() {
		this.IDs.clear();
		this.inverse = false;
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isEmpty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean contains(final Object object) {
		if (object instanceof ProcessInstanceMonitoringTreeTableElement) {
			final ProcessInstanceMonitoringTreeTableElement node = (ProcessInstanceMonitoringTreeTableElement) object;
			if (this.inverse) {
				return !this.IDs.contains(node.getID());
			} else {
				return this.IDs.contains(node.getID());
			}
		} else {
			return false;
		}
	}

	@Override
	public Iterator<ProcessInstanceMonitoringTreeTableElement> iterator() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(final T[] a) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean add(final ProcessInstanceMonitoringTreeTableElement node) {
		if (this.inverse) {
			return this.IDs.remove(node.getID());
		} else {
			return this.IDs.add(node.getID());
		}
	}

	@Override
	public boolean remove(final Object object) {
		if (object instanceof ProcessInstanceMonitoringTreeTableElement) {
			final ProcessInstanceMonitoringTreeTableElement node = (ProcessInstanceMonitoringTreeTableElement) object;
			if (this.inverse) {
				return this.IDs.add(node.getID());
			} else {
				return this.IDs.remove(node.getID());
			}
		} else {
			return false;
		}
	}

	@Override
	public boolean containsAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(final Collection<? extends ProcessInstanceMonitoringTreeTableElement> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(final Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	public static ProcessInstanceMonitoringTreeTableExpansion get() {
		ProcessInstanceMonitoringTreeTableExpansion expansion = Session.get().getMetaData(
				ProcessInstanceMonitoringTreeTableExpansion.KEY);
		if (expansion == null) {
			expansion = new ProcessInstanceMonitoringTreeTableExpansion();
			Session.get().setMetaData(ProcessInstanceMonitoringTreeTableExpansion.KEY, expansion);
		}
		return expansion;
	}
}
