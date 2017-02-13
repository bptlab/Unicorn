/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.collection;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import de.hpi.unicorn.persistence.Persistable;

/**
 * @author micha
 * 
 * @param <T>
 */
@Entity
@Inheritance
@DiscriminatorColumn(name = "ElementType")
@Table(name = "EventTreeElement")
public class EventTreeElement<T> extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@Column(name = "Value")
	protected T value;

	@ManyToOne(cascade = CascadeType.ALL)
	private EventTreeElement<T> parent;

	@OneToMany(cascade = CascadeType.ALL)
	private List<EventTreeElement<T>> children = new ArrayList<EventTreeElement<T>>();

	public EventTreeElement() {
		this.ID = 0;
	}

	/**
	 * creates a root node
	 * 
	 * @param content
	 *            the content to be stored in the new node
	 */
	public EventTreeElement(final T value) {
		this();
		this.value = value;
	}

	/**
	 * creates a node and adds it to its parent
	 * 
	 * @param parent
	 * @param content
	 *            the content to be stored in the node
	 */
	public EventTreeElement(final EventTreeElement<T> parent, final T value) {
		this(value);
		this.parent = parent;
		if (this.parent != null) {
			this.parent.addChild(this);
		}
	}

	/**
	 * creates a root node
	 * 
	 * @param id
	 *            the identifier
	 * @param content
	 *            the content to be stored in the new node
	 */
	public EventTreeElement(final int id, final T value) {
		this(value);
		this.ID = id;
	}

	/**
	 * creates a root node
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            the content to be stored in the new node
	 */
	public EventTreeElement(final EventTreeElement<T> parent, final int id, final T value) {
		this(id, value);
		this.parent = parent;
		this.parent.addChild(this);
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public T getValue() {
		return this.value;
	}

	public void setValue(final T value) {
		this.value = value;
	}

	public EventTreeElement<T> getParent() {
		return this.parent;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public void setParent(final EventTreeElement<T> parent) {
		this.parent = parent;
		if (parent != null && !parent.getChildren().contains(this)) {
			parent.addChild(this);
		}
	}

	public List<EventTreeElement<T>> getChildren() {
		return this.children;
	}

	public List<T> getChildValues() {
		final List<T> childValues = new ArrayList<T>();
		for (final EventTreeElement<T> child : this.children) {
			childValues.add(child.getValue());
		}
		return childValues;
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	public void setChildren(final List<EventTreeElement<T>> children) {
		this.children = children;
	}

	/**
	 * may be used as an alternate identifier for this element since it is
	 * dependent on its parent(s)
	 * 
	 * @return XPath expression as String
	 */
	public String getXPath() {
		if (this.parent == null) {
			return "/" + this.value.toString().replaceAll(" ", "");
		} else {
			return this.parent.getXPath() + "/" + this.value.toString();
		}
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

	/**
	 * use removeElement()
	 * 
	 * @return
	 */
	@Override
	public Persistable remove() {
		this.removeElement();
		return super.remove();
	}

	public void removeElement() {
		for (final EventTreeElement<T> child : this.children) {
			child.setParent(null);
		}
		this.children.clear();
		if (this.hasParent()) {
			this.parent.removeChild(this);
		}
	}

	private boolean addChild(final EventTreeElement<T> childTreeElement) {
		return this.children.add(childTreeElement);
	}

	public boolean removeChild(final EventTreeElement<T> nestedTreeElement) {
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
