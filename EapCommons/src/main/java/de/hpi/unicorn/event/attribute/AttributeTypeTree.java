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
import javax.persistence.EntityTransaction;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * TreeStructure for Attribut/Datatype pairs of the Eventtyp
 */
@Entity
@Table(name = "AttributeTypeTree")
public class AttributeTypeTree extends Persistable {

	private static final long serialVersionUID = 4641263893746532464L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "AttributeTypeTreeID")
	protected int ID;

	@Column(name = "Auxiliary")
	private final String auxiliary = "Auxiliary";

	@OneToOne(mappedBy = "attributes")
	private EapEventType eventType;

	@OneToMany(mappedBy = "attributeTree", cascade = { CascadeType.PERSIST, CascadeType.REMOVE })
	private final List<TypeTreeNode> rootAttributes;

	public AttributeTypeTree() {
		this.ID = 0;
		this.rootAttributes = new ArrayList<TypeTreeNode>();
	}

	public AttributeTypeTree(final TypeTreeNode rootAttribute) {
		this();
		assert (rootAttribute != null);
		this.rootAttributes.add(rootAttribute);
	}

	public AttributeTypeTree(final List<TypeTreeNode> rootAttributes) {
		this();
		assert (rootAttributes != null);
		assert (!rootAttributes.isEmpty());
		this.rootAttributes.addAll(rootAttributes);
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public EapEventType getEventType() {
		return this.eventType;
	}

	public void setEventType(final EapEventType eventType) {
		this.eventType = eventType;
	}

	public boolean isHierarchical() {
		for (final TypeTreeNode root : this.rootAttributes) {
			if (root.hasChildren()) {
				return true;
			}
		}
		return false;
	}

	public boolean addRoot(final String rootAttributeName, final AttributeTypeEnum type) {
		final TypeTreeNode rootAttribute = new TypeTreeNode(rootAttributeName, type);
		rootAttribute.setAttributeTree(this);
		return this.rootAttributes.add(rootAttribute);
	}

	public boolean addRoot(final TypeTreeNode rootAttribute) {
		rootAttribute.setAttributeTree(this);
		return this.rootAttributes.add(rootAttribute);
	}

	public boolean addRoots(final List<TypeTreeNode> rootAttributes) {
		for (final TypeTreeNode node : rootAttributes) {
			node.setAttributeTree(this);
		}
		return rootAttributes.addAll(rootAttributes);
	}

	@JsonIgnore
	public ArrayList<TypeTreeNode> getRoots() {
		return new ArrayList<TypeTreeNode>(this.rootAttributes);
	}

	public List<String> getRootsAsExpression() {
		final List<String> xPaths = new ArrayList<String>();
		for (final TypeTreeNode attribute : this.rootAttributes) {
			xPaths.add(attribute.getAttributeExpression());
		}
		return xPaths;
	}

	public List<String> getRootsAsXPath() {
		final List<String> xPaths = new ArrayList<String>();
		for (final TypeTreeNode attribute : this.rootAttributes) {
			xPaths.add(attribute.getXPath());
		}
		return xPaths;
	}

	public List<TypeTreeNode> getAttributes() {
		final List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		for (final TypeTreeNode root : this.rootAttributes) {
			if (root.hasChildren()) {
				this.addAttributeToSet(root, attributes);
			}
			attributes.add(root);
		}
		return attributes;
	}

	/**
	 * returns attributes elements which have the given name
	 */
	public List<TypeTreeNode> getAttributesByName(final String attributeName) {
		final List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		for (final TypeTreeNode root : this.rootAttributes) {
			if (root.hasChildren()) {
				this.addAttributeToSet(root, attributes, attributeName);
			}
			if (root.getName().equals(attributeName)) {
				attributes.add(root);
			}
		}
		return attributes;
	}

	public List<String> getAttributesAsExpression() {
		final List<TypeTreeNode> attributes = this.getAttributes();
		final List<String> attributeExpressions = new ArrayList<String>();
		for (final TypeTreeNode attribute : attributes) {
			attributeExpressions.add(attribute.getAttributeExpression());
		}
		return attributeExpressions;
	}

	public List<String> getAttributesAsXPath() {
		final List<TypeTreeNode> attributes = this.getAttributes();
		final List<String> attributeXPaths = new ArrayList<String>();
		for (final TypeTreeNode attribute : attributes) {
			attributeXPaths.add(attribute.getXPath());
		}
		return attributeXPaths;
	}

	private void addAttributeToSet(final TypeTreeNode attribute, final List<TypeTreeNode> attributes,
			final String attributeName) {
		for (final TypeTreeNode child : attribute.getChildren()) {
			if (child.hasChildren()) {
				this.addAttributeToSet(child, attributes);
			}
			if (child.getName().equals(attributeName)) {
				attributes.add(child);
			}
		}
	}

	private void addAttributeToSet(final TypeTreeNode attribute, final List<TypeTreeNode> attributes) {
		for (final TypeTreeNode child : attribute.getChildren()) {
			if (child.hasChildren()) {
				this.addAttributeToSet(child, attributes);
			}
			attributes.add(child);
		}
	}

	public TypeTreeNode getAttributeByExpression(final String attributeExpression) {
		final List<TypeTreeNode> attributes = this.getAttributes();
		for (final TypeTreeNode attribute : attributes) {
			if (attribute.getAttributeExpression().equals(attributeExpression)) {
				return attribute;
			}
		}
		return null;
	}

	public TypeTreeNode getAttributeByXPath(final String xPath) {
		final List<TypeTreeNode> attributes = this.getAttributes();
		for (final TypeTreeNode attribute : attributes) {
			if (attribute.getXPath().equals(xPath)) {
				return attribute;
			}
		}
		return null;
	}

	public void retainAllAttributes(final Set<TypeTreeNode> attributes) {
		final ArrayList<TypeTreeNode> copiedAttributesList = new ArrayList<TypeTreeNode>(this.getAttributes());
		for (final TypeTreeNode attribute : copiedAttributesList) {
			if (!attributes.contains(attribute)) {
				attribute.removeAttribute();
				if (!attribute.hasParent()) {
					this.rootAttributes.remove(attribute);
				}
			}
		}
	}

	public void retainAllAttributes(final List<TypeTreeNode> attributes) {
		this.retainAllAttributes(new HashSet<TypeTreeNode>(attributes));
	}

	public boolean removeRoot(final TypeTreeNode rootAttribute) {
		return this.rootAttributes.remove(rootAttribute);
	}

	public boolean contains(final String attributeExpression) {
		final List<TypeTreeNode> attributes = this.getAttributes();
		for (final TypeTreeNode attribute : attributes) {
			if (attribute.getAttributeExpression().equals(attributeExpression)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasChildren(final String attributeExpression) {
		final TypeTreeNode attribute = this.getAttributeByExpression(attributeExpression);
		return attribute.hasChildren();
	}

	public List<TypeTreeNode> getLeafAttributes() {
		final List<TypeTreeNode> leafs = new ArrayList<TypeTreeNode>();
		for (final TypeTreeNode root : this.rootAttributes) {
			if (!root.hasChildren()) {
				leafs.add(root);
			} else {
				this.addAttributeToLeafs(root, leafs);
			}
		}
		return leafs;
	}

	private void addAttributeToLeafs(final TypeTreeNode attribute, final List<TypeTreeNode> leafs) {
		for (final TypeTreeNode child : attribute.getChildren()) {
			if (!child.hasChildren()) {
				leafs.add(child);
			} else {
				this.addAttributeToLeafs(child, leafs);
			}
		}
	}

	public static List<AttributeTypeTree> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM AttributeTypeTree t");
		return q.getResultList();
	}

	@Override
	public AttributeTypeTree save() {
		for (final TypeTreeNode attribute : this.rootAttributes) {
			attribute.setAttributeTree(this);
		}
		return (AttributeTypeTree) super.save();
	}

	@Override
	public AttributeTypeTree remove() {
		for (final TypeTreeNode attribute : this.rootAttributes) {
			attribute.setAttributeTree(null);
			attribute.remove();
		}
		return (AttributeTypeTree) super.remove();
	}

	/**
	 * the choochoo train comes and run over all AttributTrees and kills them
	 * _________ | _ | __ | | | |____\/_ | |_| | \_ | _ | _ _ | |/ \_|_/ \ / \ /
	 * \_ / \_/ \_ /
	 */
	public static void removeAll() {
		final List<AttributeTypeTree> attributeTrees = AttributeTypeTree.findAll();
		for (final AttributeTypeTree attributeTree : attributeTrees) {
			final List<TypeTreeNode> rootAttributes = attributeTree.getRoots();
			for (final TypeTreeNode attribute : rootAttributes) {
				attribute.setAttributeTree(null);
				attribute.remove();
			}
		}
		try {
			final EntityTransaction entr = Persistor.getEntityManager().getTransaction();
			entr.begin();
			final Query query = Persistor.getEntityManager().createQuery("DELETE FROM AttributeTypeTree");
			query.executeUpdate();
			entr.commit();
			// System.out.println(deleteRecords + " records are deleted.");
		} catch (final Exception ex) {
			System.out.println(ex.getMessage());
		}
	}

	@Override
	public String toString() {
		return this.printTreeLevel(this.rootAttributes, 0);
	}

	private String printTreeLevel(final List<TypeTreeNode> attributes, final int count) {
		String tree = "";
		for (final TypeTreeNode attribute : attributes) {
			for (int i = 0; i < count; i++) {
				tree += "\t";
			}
			tree += attribute + System.getProperty("line.separator");
			if (attribute.hasChildren()) {
				tree += this.printTreeLevel(attribute.getChildren(), count + 1);
			}
		}
		return tree;
	}
}