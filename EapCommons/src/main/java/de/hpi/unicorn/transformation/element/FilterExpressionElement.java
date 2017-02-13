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
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * Representation of a tree node for filter expression elements. A filter
 * expressions consists of two expressions that a connected by a filter
 * expression operator One of the two expressions may be range- or
 * enumeration-based.
 * 
 * @see FilterExpressionOperatorEnum
 */
@Entity
@DiscriminatorValue("FE")
public class FilterExpressionElement extends EventTreeElement<Serializable> implements Serializable {

	private static final long serialVersionUID = -7712173256600654317L;

	@Column(name = "LeftHandSideExpression")
	private String leftHandSideExpression;

	@Column(name = "RightHandSideExpression")
	private String rightHandSideExpression;

	@ElementCollection
	private List<String> rightHandSideListOfValues;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "RangeElement_ID")
	private RangeElement rightHandSideRangeOfValues;

	@Column(name = "RightHandSideRangeBased")
	private boolean rightHandSideRangeBased;

	public FilterExpressionElement() {
		super();
		this.init();
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression element
	 */
	public FilterExpressionElement(final int id, final FilterExpressionOperatorEnum content) {
		super(id, content);
		this.init();
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression element
	 */
	public FilterExpressionElement(final EventTreeElement<Serializable> parent, final int id,
			final FilterExpressionOperatorEnum content) {
		super(parent, id, content);
		this.init();
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression element
	 * @param leftHandSideExpression
	 *            expression (reference to the attribute of the parent event
	 *            type)
	 * @param rightHandSideExpression
	 *            expression (e.g. value, calculation, ...)
	 */
	public FilterExpressionElement(final EventTreeElement<Serializable> parent, final int id,
			final FilterExpressionOperatorEnum content, final String leftHandSideExpression,
			final String rightHandSideExpression) {
		super(parent, id, content);
		this.init();
		this.leftHandSideExpression = leftHandSideExpression;
		this.rightHandSideExpression = rightHandSideExpression;
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression element
	 * @param leftHandSideExpression
	 *            expression (reference to the attribute of the parent event
	 *            type)
	 * @param rightHandSideListOfValues
	 *            enumeration of values as list
	 */
	public FilterExpressionElement(final EventTreeElement<Serializable> parent, final int id,
			final FilterExpressionOperatorEnum content, final String leftHandSideExpression,
			final ArrayList<String> rightHandSideListOfValues) {
		super(parent, id, content);
		assert ((content == FilterExpressionOperatorEnum.IN) || (content == FilterExpressionOperatorEnum.NOT_IN));
		this.leftHandSideExpression = leftHandSideExpression;
		this.rightHandSideListOfValues = rightHandSideListOfValues;
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            the identifier
	 * @param content
	 *            filter expression element
	 * @param leftHandSideExpression
	 *            expression (reference to the attribute of the parent event
	 *            type)
	 * @param rightHandSideRangeOfValues
	 *            range of values
	 */
	public FilterExpressionElement(final EventTreeElement<Serializable> parent, final int id,
			final FilterExpressionOperatorEnum content, final String leftHandSideExpression,
			final RangeElement rightHandSideRangeOfValues) {
		super(parent, id, content);
		assert ((content == FilterExpressionOperatorEnum.IN) || (content == FilterExpressionOperatorEnum.NOT_IN));
		this.leftHandSideExpression = leftHandSideExpression;
		this.rightHandSideRangeOfValues = rightHandSideRangeOfValues;
	}

	private void init() {
		this.leftHandSideExpression = new String();
		this.rightHandSideExpression = new String();
		this.rightHandSideListOfValues = new ArrayList<String>();
		this.rightHandSideRangeOfValues = new RangeElement();
		this.rightHandSideRangeBased = true;
	}

	public String getLeftHandSideExpression() {
		return this.leftHandSideExpression;
	}

	public void setLeftHandSideExpression(final String leftHandSideExpression) {
		this.leftHandSideExpression = leftHandSideExpression;
	}

	public String getRightHandSideExpression() {
		return this.rightHandSideExpression;
	}

	public void setRightHandSideExpression(final String rightHandSideExpression) {
		this.rightHandSideExpression = rightHandSideExpression;
	}

	public List<String> getRightHandSideListOfValues() {
		return this.rightHandSideListOfValues;
	}

	public void setRightHandSideListOfValues(final List<String> rightHandSideListOfValues) {
		this.rightHandSideListOfValues = rightHandSideListOfValues;
	}

	public RangeElement getRightHandSideRangeOfValues() {
		return this.rightHandSideRangeOfValues;
	}

	public void setRightHandSideRangeOfValues(final RangeElement rightHandSideRangeOfValues) {
		this.rightHandSideRangeOfValues = rightHandSideRangeOfValues;
	}

	public boolean isRightHandSideRangeBased() {
		return this.rightHandSideRangeBased;
	}

	public void setRightHandSideRangeBased(final boolean rightHandSideRangeBased) {
		this.rightHandSideRangeBased = rightHandSideRangeBased;
	}

}
