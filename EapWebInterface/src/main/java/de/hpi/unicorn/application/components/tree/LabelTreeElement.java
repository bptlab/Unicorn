/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.tree;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LabelTreeElement<T> implements Serializable {

	private static final long serialVersionUID = 8541013668634625224L;

	private int ID;
	private T value;
	private LabelTreeElement<T> parent;
	private List<LabelTreeElement<T>> children = new ArrayList<LabelTreeElement<T>>();

	public LabelTreeElement(final int ID, final T value) {
		this.ID = ID;
		this.value = value;
	}

	public LabelTreeElement(final LabelTreeElement<T> parent, final int ID, final T value) {
		this(ID, value);
		this.parent = parent;
		if (this.parent != null) {
			this.parent.addChild(this);
		}
	}

	public int getID() {
		return this.ID;
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(final T value) {
		this.value = value;
	}

	public LabelTreeElement<T> getParent() {
		return this.parent;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public void setParent(final LabelTreeElement<T> parent) {
		this.parent = parent;
		if (parent != null && !parent.getChildren().contains(this)) {
			parent.addChild(this);
		}
	}

	public List<LabelTreeElement<T>> getChildren() {
		return this.children;
	}

	public List<T> getChildValues() {
		final List<T> childValues = new ArrayList<T>();
		for (final LabelTreeElement<T> child : this.children) {
			childValues.add(child.getValue());
		}
		return childValues;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public void setChildren(final List<LabelTreeElement<T>> children) {
		this.children = children;
	}

	/**
	 * 
	 * @return returns recursive the path to this element as XPath
	 */
	public String getXPath() {
		if (this.parent == null) {
			return "/" + this.value.toString().replaceAll(" ", "");
		}
		return this.parent.getXPath() + "/" + this.value.toString();

	}

	/**
	 * root level is 0
	 * 
	 * @return level of element as int
	 */
	public int getLevel() {
		if (this.parent == null) {
			return 0;
		} else {
			return this.parent.getLevel() + 1;
		}
	}

	private boolean addChild(final LabelTreeElement<T> childTreeElement) {
		return this.children.add(childTreeElement);
	}

	public boolean removeChild(final LabelTreeElement<T> nestedTreeElement) {
		return this.children.remove(nestedTreeElement);
	}

	public void removeChildren() {
		this.children.clear();
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
