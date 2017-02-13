/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * Handles the expansion state of a tree component.
 */
public class BPMNTreeTableExpansion implements Set<BPMNTreeTableElement>, Serializable {

	private static final long serialVersionUID = 1L;

	private static MetaDataKey<BPMNTreeTableExpansion> KEY = new MetaDataKey<BPMNTreeTableExpansion>() {
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
		if (object instanceof BPMNTreeTableElement) {
			final BPMNTreeTableElement node = (BPMNTreeTableElement) object;
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
	public Iterator<BPMNTreeTableElement> iterator() {
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
	public boolean add(final BPMNTreeTableElement node) {
		if (this.inverse) {
			return this.IDs.remove(node.getID());
		} else {
			return this.IDs.add(node.getID());
		}
	}

	@Override
	public boolean remove(final Object object) {
		if (object instanceof BPMNTreeTableElement) {
			final BPMNTreeTableElement node = (BPMNTreeTableElement) object;
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
	public boolean addAll(final Collection<? extends BPMNTreeTableElement> c) {
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

	public static BPMNTreeTableExpansion get() {
		BPMNTreeTableExpansion expansion = Session.get().getMetaData(BPMNTreeTableExpansion.KEY);
		if (expansion == null) {
			expansion = new BPMNTreeTableExpansion();
			Session.get().setMetaData(BPMNTreeTableExpansion.KEY, expansion);
		}
		return expansion;
	}
}
