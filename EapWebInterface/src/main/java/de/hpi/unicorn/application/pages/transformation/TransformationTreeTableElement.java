/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * representation of a tree node
 * 
 * @param <T>
 *            type of content to be stored
 */
public class TransformationTreeTableElement<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private int ID;
	private T content;
	private TransformationTreeTableElement<T> parent;
	private final ArrayList<TransformationTreeTableElement<T>> children = new ArrayList<TransformationTreeTableElement<T>>();
	private int probability;

	/**
	 * creates a root node
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public TransformationTreeTableElement(final int ID, final T content, final int probability) {
		this.ID = ID;
		this.content = content;
		this.probability = probability;
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public TransformationTreeTableElement(final TransformationTreeTableElement<T> parent, final int ID,
			final T content, final int probability) {
		this(ID, content, probability);
		this.parent = parent;
		this.parent.getChildren().add(this);
	}

	public Integer getID() {
		return this.ID;
	}

	public void setID(final int ID) {
		this.ID = ID;
	}

	public T getContent() {
		return this.content;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public TransformationTreeTableElement<T> getParent() {
		return this.parent;
	}

	public ArrayList<TransformationTreeTableElement<T>> getChildren() {
		return this.children;
	}

	@Override
	public String toString() {
		if (this.content == null) {
			return new String();
		}
		return this.content.toString();
	}

	public int getProbability() {
		return this.probability;
	}

	public void setProbability(final int probability) {
		this.probability = probability;
	}

	public void remove() {
		if (this.parent != null) {
			this.parent.getChildren().remove(this);
		}
		// Müssen Kinder noch explizit entfernt werden?
	}

	public void setParent(final TransformationTreeTableElement<T> parent) {
		this.parent = parent;
	}
}
