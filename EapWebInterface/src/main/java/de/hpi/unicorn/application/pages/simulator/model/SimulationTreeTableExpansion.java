/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator.model;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

/**
 * handles the expansion state of a tree component
 */
public class SimulationTreeTableExpansion<T> implements Set<SimulationTreeTableElement<T>>, Serializable {

	private static final long serialVersionUID = 1L;

	private static MetaDataKey<SimulationTreeTableExpansion> KEY = new MetaDataKey<SimulationTreeTableExpansion>() {
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
		if (object instanceof SimulationTreeTableElement) {
			final SimulationTreeTableElement<T> node = (SimulationTreeTableElement<T>) object;
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
	public Iterator<SimulationTreeTableElement<T>> iterator() {
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
	public boolean add(final SimulationTreeTableElement<T> node) {
		if (this.inverse) {
			return this.IDs.remove(node.getID());
		} else {
			return this.IDs.add(node.getID());
		}
	}

	@Override
	public boolean remove(final Object object) {
		if (object instanceof SimulationTreeTableElement) {
			final SimulationTreeTableElement node = (SimulationTreeTableElement) object;
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
	public boolean addAll(final Collection<? extends SimulationTreeTableElement<T>> c) {
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

	public static SimulationTreeTableExpansion get() {
		SimulationTreeTableExpansion expansion = Session.get().getMetaData(SimulationTreeTableExpansion.KEY);
		if (expansion == null) {
			expansion = new SimulationTreeTableExpansion();
			Session.get().setMetaData(SimulationTreeTableExpansion.KEY, expansion);
		}
		return expansion;
	}
}
