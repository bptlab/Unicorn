/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * Representation of a tree node for pattern operators. A range element is
 * required for REPEAT and UNTIL pattern operators. Distinct attributes are
 * required for the EVERY-DISTINCT pattern operator.
 * 
 * @see PatternOperatorEnum
 */
@Entity
@DiscriminatorValue("PO")
public class PatternOperatorElement extends EventTreeElement<Serializable> implements Serializable {

	private static final long serialVersionUID = -9184705192436364035L;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "RangeElement_ID")
	private RangeElement rangeElement;

	@ElementCollection
	@CollectionTable(name = "DistinctAttributes", joinColumns = @JoinColumn(name = "PatternOperatorElement_ID"))
	@Column(name = "attribute")
	private List<String> distinctAttributes;

	public PatternOperatorElement() {
		super();
		this.rangeElement = new RangeElement();
		this.distinctAttributes = new ArrayList<String>();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the identifier
	 * @param content
	 *            pattern operator
	 */
	public PatternOperatorElement(final int id, final PatternOperatorEnum content) {
		super(id, content);
		this.rangeElement = new RangeElement();
		this.distinctAttributes = new ArrayList<String>();
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            pattern operator
	 */
	public PatternOperatorElement(final EventTreeElement<Serializable> parent, final int id,
			final PatternOperatorEnum content) {
		super(parent, id, content);
		this.rangeElement = new RangeElement();
		this.distinctAttributes = new ArrayList<String>();
	}

	public RangeElement getRangeElement() {
		return this.rangeElement;
	}

	public void setRangeElement(final RangeElement rangeElement) {
		this.rangeElement = rangeElement;
	}

	public List<String> getDistinctAttributes() {
		return this.distinctAttributes;
	}

	public void setDistinctAttributes(final List<String> distinctAttributes) {
		this.distinctAttributes = distinctAttributes;
	}

}
