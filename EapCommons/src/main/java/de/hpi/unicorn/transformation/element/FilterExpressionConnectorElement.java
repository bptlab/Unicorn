/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

import java.io.Serializable;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * Representation of a tree node for filter expression connectors.
 * 
 * @see FilterExpressionConnectorEnum
 */
@Entity
@DiscriminatorValue("FEC")
public class FilterExpressionConnectorElement extends EventTreeElement<Serializable> implements Serializable {

	private static final long serialVersionUID = 1L;

	public FilterExpressionConnectorElement() {
		super();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression connector
	 */
	public FilterExpressionConnectorElement(final int id, final FilterExpressionConnectorEnum content) {
		super(id, content);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression connector
	 */
	public FilterExpressionConnectorElement(final EventTreeElement<Serializable> parent, final int id,
			final FilterExpressionConnectorEnum content) {
		super(parent, id, content);
	}

}
