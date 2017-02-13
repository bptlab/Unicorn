/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event.attribute;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * Representation of an Attribute/Datatype element of the AttributeTypeTree
 */
@Entity
@Table(name = "TypeTreeNode")
public class TypeTreeNode extends Persistable {

	private static final long serialVersionUID = -3804228219409837851L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	protected int ID;

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	@JoinColumn(name = "attributeTree")
	private AttributeTypeTree attributeTree;

	@ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private TypeTreeNode parent;

	@OneToMany(cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private final List<TypeTreeNode> children = new ArrayList<TypeTreeNode>();

	@OneToMany(mappedBy = "firstAttribute")
	private final Set<CorrelationRule> correlationRulesFirst = new HashSet<CorrelationRule>();

	@OneToMany(mappedBy = "secondAttribute")
	private final Set<CorrelationRule> correlationRulesSecond = new HashSet<CorrelationRule>();

	@Column(name = "Name")
	protected String name;

	@Column(name = "AttributeType")
	@Enumerated(EnumType.STRING)
	private AttributeTypeEnum type = AttributeTypeEnum.STRING;

	/**
	 * for temporary use in TransformationRuleEditor only not persisted by JPA
	 */
	@Transient
	private boolean isTimestamp;

	public TypeTreeNode() {
		this.ID = 0;
		this.isTimestamp = false;
	}

	/**
	 * creates a root attribute without data type (use this constructor if you
	 * add child attributes to the root attribute)
	 * 
	 * @param name
	 *            allowed characters: a-z, A-Z, 0-9, _; whitespace(s) converted
	 *            to underscore
	 */
	public TypeTreeNode(final String name) throws RuntimeException {
		this();
		if (name.equals("ProcessInstances")) {
			throw new RuntimeException("The attribute name 'ProcessInstances' is reserved. Please choose another one.");
		}
		this.name = name.trim().replaceAll(" +", "_").replaceAll("[^a-zA-Z0-9_]+", "");
	}

	/**
	 * creates a root attribute with data type
	 * 
	 * @param name
	 * @param type
	 */
	public TypeTreeNode(final String name, final AttributeTypeEnum type) throws RuntimeException {
		this(name);
		this.type = type;
	}

	/**
	 * creates a child node without data type and adds it to the given parent
	 * (use this constructor if you add child attributes to the root attribute)
	 * 
	 * @param parent
	 * @param name
	 */
	public TypeTreeNode(final TypeTreeNode parent, final String name) throws RuntimeException {
		this.parent = parent;
		this.name = name;
		parent.setType(null);
		this.parent.addChild(this);
	}

	/**
	 * creates a child node with data type and adds it to the given parent
	 * 
	 * @param parent
	 * @param name
	 * @param type
	 */
	public TypeTreeNode(final TypeTreeNode parent, final String name, final AttributeTypeEnum type)
			throws RuntimeException {
		this(parent, name);
		this.type = type;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	/**
	 * Use getAttributeExpression() instead if you want to use it as a key for
	 * the values (HashMap) for the EapEvent.
	 * 
	 * @return the name of the attribute
	 */
	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public boolean hasParent() {
		return this.parent != null;
	}

	public TypeTreeNode getParent() {
		return this.parent;
	}

	public void setParent(final TypeTreeNode parent) {
		this.parent = parent;
		this.parent.setType(null);
		this.parent.addChild(this);
	}

	public boolean isRootElement() {
		return this.parent == null;
	}

	public AttributeTypeEnum getType() {
		return this.type;
	}

	public void setType(final AttributeTypeEnum attributeType) {
		this.type = attributeType;
	}

	public boolean isTimestamp() {
		return this.isTimestamp;
	}

	public void setTimestamp(final boolean isTimestamp) {
		this.isTimestamp = isTimestamp;
	}

	public ArrayList<TypeTreeNode> getChildren() {
		return new ArrayList<TypeTreeNode>(this.children);
	}

	public boolean hasChildren() {
		return !this.children.isEmpty();
	}

	/**
	 * adds child node to attribute element
	 */
	public boolean addChild(final TypeTreeNode attribute) {
		if (!attribute.hasParent()) {
			attribute.setParent(attribute);
		}
		return this.children.add(attribute);
	}

	public void removeAttribute() {
		this.children.clear();
		if (this.hasParent()) {
			this.parent.removeChild(this);
			if (!this.parent.hasChildren()) {
				// because leaf attributes without types are not allowed
				this.parent.setType(AttributeTypeEnum.STRING);
			}
		}
	}

	public void removeChild(final TypeTreeNode attribute) {
		this.children.remove(attribute);
	}

	public void removeAllChildren() {
		this.children.clear();
	}

	/**
	 * Generates an identifier composed of [level of this attribute in
	 * tree]-[name of parent attribute]-[name of this attribute] and returns it.
	 * Does not return the ID under which the attribute is stored in the
	 * database!
	 * 
	 * @return generated identifier as String
	 */
	public String getIdentifier() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.getLevelInTree(this, 0));
		if (this.parent != null) {
			sb.append("-" + this.parent.getName().trim());
		}
		sb.append(this.name.trim());
		return sb.toString();
	}

	/**
	 * @return level of this attribute in tree as an intenger
	 */
	private int getLevelInTree(final TypeTreeNode attribute, final int rootLevel) {
		if (!attribute.hasParent()) {
			return rootLevel;
		}
		return this.getLevelInTree(attribute.getParent(), rootLevel + 1);
	}

	/**
	 * 
	 * @return returns recursive the path to this element as XPath
	 */
	public String getXPath() {
		if (this.parent == null) {
			return "/" + this.name.toString().replaceAll(" ", "");
		}
		return this.parent.getXPath() + "/" + this.name.toString();

	}

	public String getAttributeExpression() {
		if (this.isRootElement()) {
			return this.name;
		}
		return this.parent.getAttributeExpression() + "." + this.name;
	}

	private TypeTreeNode getRootLevelParent() {
		if (this.parent == null) {
			return this;
		}
		return this.parent.getRootLevelParent();
	}

	@JsonIgnore
	public EapEventType getEventType() {
		final TypeTreeNode rootLevelParent = this.getRootLevelParent();
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM EventType " + "WHERE Attributes = '" + rootLevelParent.getAttributeTree().getID()
						+ "'", EapEventType.class);
		return (EapEventType) query.getSingleResult();
	}

	/**
	 * attribute identifier consisting of event type name plus attribute
	 * expression example: Order.orderId for first level attribute with name
	 * "orderId" of event type "Order"
	 * 
	 * @return qualified attribute name
	 */
	public String getQualifiedAttributeName() {
		return this.getEventType().getTypeName() + "." + this.getAttributeExpression();
	}

	@JsonIgnore
	public AttributeTypeTree getAttributeTree() {
		return this.attributeTree;
	}

	public void setAttributeTree(final AttributeTypeTree attributeTree) {
		this.attributeTree = attributeTree;
	}

	public boolean addToCorrelationRulesFirst(final CorrelationRule rule) {
		return this.correlationRulesFirst.add(rule);
	}

	public boolean addToCorrelationRulesSecond(final CorrelationRule rule) {
		return this.correlationRulesSecond.add(rule);
	}

	/**
	 * attributes are equal if their parent attributes and attribute names are
	 * equal NOTE: use equalsWithEventType(TypeTreeNode element) for including
	 * the event type
	 */
	@Override
	public boolean equals(final Object element) {
		if (element instanceof TypeTreeNode) {
			final TypeTreeNode attribute = (TypeTreeNode) element;
			/*
			 * attributes are equal irrespective of their types because it would
			 * be else possible to add attributes with the same name to a parent
			 * attribute
			 */
			return this.getAttributeExpression().equals(attribute.getAttributeExpression());
		} else {
			return false;
		}
	}

	/**
	 * equality by parent attributes and attribute names
	 */
	public boolean equalsWithEventType(final TypeTreeNode attribute) {
		boolean bool1 = false, bool2 = false;
		if (this.getRootLevelParent().getAttributeTree() == null) {
			bool1 = (attribute.getAttributeTree() == null) ? true : false;
		} else {
			bool1 = this.getRootLevelParent().getAttributeTree().equals(attribute.getAttributeTree());
		}
		bool2 = this.getAttributeExpression().equals(attribute.getAttributeExpression());
		return bool1 && bool2;
	}

	/**
	 * attributes have the same hashcode if their types and names are equal and
	 * if they belong to the same attribute tree (-> event type)
	 */
	@Override
	public int hashCode() {
		int hashCode = 0;
		if (this.type != null) {
			hashCode += this.type.hashCode();
		}
		if (this.getXPath() != null) {
			hashCode += this.getXPath().hashCode();
		}
		if (this.attributeTree != null) {
			hashCode += this.attributeTree.hashCode();
		}
		return hashCode;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.name);
		if (this.isTimestamp()) {
			sb.append(" : Timestamp");
		} else if (this.type != null) {
			sb.append(" : " + this.type);
		}
		return sb.toString();
	}
}