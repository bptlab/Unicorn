/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * Representation of a tree node for event types. Each event type element holds
 * an alias that is unique for each pattern tree.
 */
@Entity
@DiscriminatorValue("ET")
public class EventTypeElement extends EventTreeElement<Serializable> implements Serializable {

	private static final long serialVersionUID = -2184890213473132784L;

	@Column(name = "Alias")
	private String alias;

	public EventTypeElement() {
		super();
		this.alias = new String();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the identifier
	 * @param contentevent
	 *            type
	 */
	public EventTypeElement(final int id, final EapEventType content) {
		super(id, content);
		this.alias = new String();
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            event type
	 */
	public EventTypeElement(final EventTreeElement<Serializable> parent, final int id, final EapEventType content) {
		super(parent, id, content);
		this.alias = new String();
	}

	public boolean hasAlias() {
		return this.alias != null && !this.alias.isEmpty();
	}

	public String getAlias() {
		return this.alias;
	}

	public void setAlias(final String alias) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		return this.value.toString();
	}

}
