/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation.element.externalknowledge;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.transformation.TransformationRule;

/**
 * Container object for external knowledge expressions. References the attribute
 * (using its attribute expression) of the event type for transformed events. It
 * also contains the type of the attribute and a default value. The default
 * value is used if no external knowledge expression can retrieve a value for
 * the specified attribute.
 * 
 */
@Entity
@Table(name = "ExternalKnowledgeExpressionSet")
public class ExternalKnowledgeExpressionSet extends Persistable {

	private static final long serialVersionUID = -7637140960882882120L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ExpressionSetID")
	private int ID;

	@Column(name = "attributeExpression")
	private String attributeExpression;

	@ManyToOne
	private TransformationRule transformationRule;

	@Column(name = "resultingType")
	@Enumerated(EnumType.STRING)
	private AttributeTypeEnum resultingType;

	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "ExpressionSetID", referencedColumnName = "ExpressionSetID")
	private List<ExternalKnowledgeExpression> externalKnowledgeExpressions;

	@Column(name = "defaultValue")
	private String defaultValue;

	public ExternalKnowledgeExpressionSet() {
		this.ID = 0;
		this.attributeExpression = null;
		this.resultingType = null;
		this.externalKnowledgeExpressions = new ArrayList<ExternalKnowledgeExpression>();
		this.defaultValue = null;
	}

	/**
	 * Constructor.
	 * 
	 * @param resultingType
	 *            the event type for the transformed events
	 * @param attributeExpression
	 *            the attribute expression of the attribute of the event type
	 *            for the transformed events
	 */
	public ExternalKnowledgeExpressionSet(final AttributeTypeEnum resultingType, final String attributeExpression) {
		this();
		this.resultingType = resultingType;
		this.attributeExpression = attributeExpression;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public AttributeTypeEnum getResultingType() {
		return this.resultingType;
	}

	public void setResultingType(final AttributeTypeEnum resultingType) {
		this.resultingType = resultingType;
	}

	public List<ExternalKnowledgeExpression> getExternalKnowledgeExpressions() {
		return this.externalKnowledgeExpressions;
	}

	public void setExternalKnowledgeExpressions(final List<ExternalKnowledgeExpression> externalKnowledgeExpressions) {
		this.externalKnowledgeExpressions = externalKnowledgeExpressions;
	}

	public String getAttributeExpression() {
		return this.attributeExpression;
	}

	public void setAttributeExpression(final String attributeExpression) {
		this.attributeExpression = attributeExpression;
	}

	public TransformationRule getTransformationRule() {
		return this.transformationRule;
	}

	public void setTransformationRule(final TransformationRule transformationRule) {
		this.transformationRule = transformationRule;
	}

	public boolean addExpression(final ExternalKnowledgeExpression expression) {
		return this.externalKnowledgeExpressions.add(expression);
	}

	public boolean removeExpression(final ExternalKnowledgeExpression expression) {
		return this.externalKnowledgeExpressions.remove(expression);
	}

	public String getDefaultValue() {
		return this.defaultValue;
	}

	public void setDefaultValue(final String defaultValue) {
		this.defaultValue = defaultValue;
	}

	@Override
	public ExternalKnowledgeExpressionSet save() {
		return (ExternalKnowledgeExpressionSet) super.save();
	}

	@Override
	public ExternalKnowledgeExpressionSet remove() {
		return (ExternalKnowledgeExpressionSet) super.remove();
	}
}
