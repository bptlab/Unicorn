/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * 
 * Container object for pairs of attributes. Used for event correlation.
 * Attributes must be from the same type, but may have different names and may
 * belong to different event types. Related to a process.
 * 
 */
@Entity
@Table(name = "CorrelationRule")
public class CorrelationRule extends Persistable {

	private static final long serialVersionUID = -1261406813387858839L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private final int ID;

	@ManyToOne
	@JoinColumn(name = "ProcessID")
	private CorrelationProcess process;

	@ManyToOne
	@JoinColumn(name = "firstAttributeID")
	private TypeTreeNode firstAttribute;

	@ManyToOne
	@JoinColumn(name = "secondAttributeID")
	private TypeTreeNode secondAttribute;

	@Transient
	private EapEventType eventTypeOfFirstAttribute;

	@Transient
	private EapEventType eventTypeOfSecondAttribute;

	public CorrelationRule() {
		this.ID = 0;
	}

	/**
	 * 
	 * Constructor. Checks for validity of the rule and throws an exception if
	 * required.
	 * 
	 * @param firstAttribute
	 * @param secondAttribute
	 * @throws RuntimeException
	 */
	public CorrelationRule(final TypeTreeNode firstAttribute, final TypeTreeNode secondAttribute)
			throws RuntimeException {
		this();
		if (firstAttribute == null && secondAttribute == null) {
			throw new RuntimeException("Correlation rule attributes must not be null.");
		} else if (firstAttribute.getType() != secondAttribute.getType()) {
			throw new RuntimeException("Types of correlation rule attributes are not equal.");
		}
		this.firstAttribute = firstAttribute;
		this.secondAttribute = secondAttribute;
	}

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public void setProcess(final CorrelationProcess process) {
		this.process = process;
	}

	public TypeTreeNode getFirstAttribute() {
		return this.firstAttribute;
	}

	public void setFirstAttribute(final TypeTreeNode firstAttribute) {
		this.firstAttribute = firstAttribute;
	}

	public TypeTreeNode getSecondAttribute() {
		return this.secondAttribute;
	}

	public void setSecondAttribute(final TypeTreeNode secondAttribute) {
		this.secondAttribute = secondAttribute;
	}

	public EapEventType getEventTypeOfFirstAttribute() {
		return this.eventTypeOfFirstAttribute;
	}

	public void setEventTypeOfFirstAttribute(final EapEventType eventTypeOfFirstAttribute) {
		this.eventTypeOfFirstAttribute = eventTypeOfFirstAttribute;
	}

	public EapEventType getEventTypeOfSecondAttribute() {
		return this.eventTypeOfSecondAttribute;
	}

	public void setEventTypeOfSecondAttribute(final EapEventType eventTypeOfSecondAttribute) {
		this.eventTypeOfSecondAttribute = eventTypeOfSecondAttribute;
	}

	public static List<CorrelationRule> findAll() {
		final Query query = Persistor.getEntityManager().createQuery("select t from CorrelationRule t",
				CorrelationRule.class);
		return query.getResultList();
	}

	@Override
	public CorrelationRule save() {
		this.firstAttribute.addToCorrelationRulesFirst(this);
		this.secondAttribute.addToCorrelationRulesSecond(this);
		return (CorrelationRule) super.save();
	}

	@Override
	public String toString() {
		return this.firstAttribute.getQualifiedAttributeName() + "=" + this.secondAttribute.getQualifiedAttributeName();
	}

	@Override
	public int getID() {
		return this.ID;
	}
}
