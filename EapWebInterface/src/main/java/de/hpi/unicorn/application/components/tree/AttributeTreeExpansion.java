/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;

import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * handles the expansion state of a tree component
 */
public class AttributeTreeExpansion implements Set<TypeTreeNode>, Serializable {

	private static final long serialVersionUID = 1L;

	private static MetaDataKey<AttributeTreeExpansion> KEY = new MetaDataKey<AttributeTreeExpansion>() {
	};

	private final Set<String> IDs = new HashSet<String>();
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
		if (object instanceof TypeTreeNode) {
			final TypeTreeNode node = (TypeTreeNode) object;
			if (this.inverse) {
				return !this.IDs.contains(node.getIdentifier());
			} else {
				return this.IDs.contains(node.getIdentifier());
			}
		} else {
			return false;
		}
	}

	@Override
	public Iterator<TypeTreeNode> iterator() {
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
	public boolean add(final TypeTreeNode node) {
		if (this.inverse) {
			return this.IDs.remove(node.getIdentifier());
		} else {
			return this.IDs.add(node.getIdentifier());
		}
	}

	@Override
	public boolean remove(final Object object) {
		if (object instanceof TypeTreeNode) {
			final TypeTreeNode node = (TypeTreeNode) object;
			if (this.inverse) {
				return this.IDs.add(node.getIdentifier());
			} else {
				return this.IDs.remove(node.getIdentifier());
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
	public boolean addAll(final Collection<? extends TypeTreeNode> c) {
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

	public static AttributeTreeExpansion get() {
		AttributeTreeExpansion expansion = Session.get().getMetaData(AttributeTreeExpansion.KEY);
		if (expansion == null) {
			expansion = new AttributeTreeExpansion();
			Session.get().setMetaData(AttributeTreeExpansion.KEY, expansion);
		}
		return expansion;
	}
}
