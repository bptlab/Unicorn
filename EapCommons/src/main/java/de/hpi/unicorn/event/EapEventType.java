/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

/**
 * Representation of an event type
 */
@Entity
@Table(name = "EventType")
public class EapEventType extends Persistable {

	private static final long serialVersionUID = 1L;

	@Id
	@Column(name = "ID")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int ID;

	@Column(name = "TypeName", unique = true)
	private String typeName;

	// hold the structure definition of the attributes + types in a tree
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "Attributes")
	private AttributeTypeTree attributes;

	// must be an attribute expression
	@Column(name = "TimestampName")
	private String timestampName;

	@Column(name = "XMLEvent")
	private boolean isXMLEvent = false;

	@Column(name = "SchemaName")
	private String schemaName;

	@Lob
	@Column(name = "xsdString", length = 40000)
	private String xsdString;

	private EapEventType() {
		this.typeName = "";
		this.attributes = new AttributeTypeTree();
		// TODO: should be AbstractXMLParser.GENERATED_TIMESTAMP_COLUMN_NAME
		this.timestampName = "ImportTime";
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName) throws RuntimeException {
		this();

		/*
		 * remove leading and trailing whitespace and replace each sequence of
		 * whitespace with an underscore
		 */
		final String strippedTypeName = typeName.trim().replaceAll(" +", "_");
		if (!this.isValidName(strippedTypeName)) {
			throw new RuntimeException(
					"Event type name is not valid. Only characters [a-z], [A-Z], [0-9], - and _ are allowed!");
		}
		this.typeName = strippedTypeName;
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @param attributeTree
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName, final AttributeTypeTree attributeTree) throws RuntimeException {
		this(typeName);
		this.attributes = attributeTree;
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @param attributeTree
	 * @param timestampName
	 *            must be an attribute expression (e.g. 'timestamp' for a
	 *            timestamp that is in root level, 'someroot.time' for a
	 *            timestamp that is in the second level etc.
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName, final AttributeTypeTree attributeTree, final String timestampName)
			throws RuntimeException {
		this(typeName, attributeTree);
		/*
		 * remove leading and trailing whitespace and replace each sequence of
		 * whitespace with an underscore
		 */
		if (timestampName != null) {
			final String strippedTimestampName = timestampName.trim().replaceAll(" +", "_");
			if (!this.isValidName(strippedTimestampName)) {
				throw new RuntimeException(
						"Timestamp name is not valid. Only characters [a-z], [A-Z], [0-9], - and _ are allowed!");
			}
			this.timestampName = strippedTimestampName;
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @param attributeTree
	 * @param timestampName
	 *            must be an attribute expression (e.g. 'timestamp' for a
	 *            timestamp that is in root level, 'someroot.time' for a
	 *            timestamp that is in the second level etc.
	 * @param schemaName
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName, final AttributeTypeTree attributeTree, final String timestampName,
			final String schemaName) throws RuntimeException {
		this(typeName, attributeTree, timestampName);
		this.isXMLEvent = true;
		this.schemaName = schemaName;
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @param attributes
	 *            list of root level attributes
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName, final List<TypeTreeNode> attributes) throws RuntimeException {
		this(typeName);
		for (final TypeTreeNode attribute : attributes) {
			this.attributes.addRoot(attribute);
		}
	}

	/**
	 * Constructor.
	 * 
	 * @param typeName
	 * @param attributes
	 *            list of root level attributes
	 * @param timestampName
	 *            must be an attribute expression (e.g. 'timestamp' for a
	 *            timestamp that is in root level, 'someroot.time' for a
	 *            timestamp that is in the second level etc.
	 * @throws RuntimeException
	 */
	public EapEventType(final String typeName, final List<TypeTreeNode> attributes, final String timestampName)
			throws RuntimeException {
		this(typeName, attributes);
		/*
		 * remove leading and trailing whitespace and replace each sequence of
		 * whitespace with an underscore
		 */
		if (timestampName != null) {
			final String strippedTimestampName = timestampName.trim().replaceAll(" +", "_");
			if (!this.isValidName(strippedTimestampName)) {
				throw new RuntimeException(
						"Timestamp name is not valid. Only characters [a-z], [A-Z], [0-9], - and _ are allowed!");
			}
			this.timestampName = strippedTimestampName;
		}
	}

	/**
	 * add attribute/type pair to the root attributes
	 */
	public void addValueType(final TypeTreeNode attribute) {
		this.attributes.addRoot(attribute);
	}

	/**
	 * add attribute/type pairs to the root attributes
	 */
	public void addValueTypes(final List<TypeTreeNode> rootAttributes) {
		for (final TypeTreeNode root : rootAttributes) {
			this.attributes.addRoot(root);
		}
	}

	/**
	 * checks if EapEventtyp contains all given attribute names
	 */
	public boolean containsValues(final List<String> attributeNames) {
		for (final String name : attributeNames) {
			if (!this.containsValue(name)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * checks if EapEventtyp contains the given attribute name
	 */
	public boolean containsValue(final String attributeName) {
		return this.attributes.contains(attributeName);
	}

	/**
	 * @param attributeExpression
	 * @return true if the first found attribute has children
	 */
	public boolean hasChildren(final String attributeExpression) {
		return this.attributes.hasChildren(attributeExpression);
	}

	/**
	 * checks if the Eventtyp is hierarchical
	 */
	public boolean isHierarchical() {
		return this.attributes.isHierarchical();
	}

	public boolean isValidName(final String string) {
		return string != null && string.matches("^[-a-zA-Z0-9._]+"); // a-z A-Z
																		// 0-9 _
																		// -
																		// sind
																		// erlaubt
	}

	/**
	 * @return list of root attribute names from the value type tree plus the
	 *         timestamp name
	 */
	public ArrayList<String> getNonHierarchicalAttributeExpressions() {
		final ArrayList<String> attributeNames = new ArrayList<String>();
		final List<TypeTreeNode> rootAttributes = this.attributes.getRoots();
		attributeNames.add(this.timestampName);
		for (final TypeTreeNode attribute : rootAttributes) {
			attributeNames.add(attribute.getName());
		}
		return attributeNames;
	}

	/**
	 * @return list of attribute expressions from the value type tree plus the
	 *         timestamp name
	 */
	public ArrayList<String> getAttributeExpressions() {
		final ArrayList<String> attributeExpressions = this.getAttributeExpressionsWithoutTimestampName();
		attributeExpressions.add(this.timestampName);
		return attributeExpressions;
	}

	/**
	 * @return list of attribute expressions from the value type tree
	 */
	public ArrayList<String> getAttributeExpressionsWithoutTimestampName() {
		final ArrayList<String> attributeExpressions = new ArrayList<String>();
		final List<TypeTreeNode> allAttributes = this.attributes.getAttributes();
		for (final TypeTreeNode attribute : allAttributes) {
			attributeExpressions.add(attribute.getAttributeExpression());
		}
		return attributeExpressions;
	}

	@Override
	public int getID() {
		return this.ID;
	}

	public void setID(final int iD) {
		this.ID = iD;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(final String name) {
		if (!this.isValidName(name)) {
			throw new RuntimeException("Event type name is invalid. Allowed characters: [a-z] [A-Z] [0-9] - _");
		}
		this.typeName = name;
	}

	/**
	 * @return timestamp name as attribute expression
	 */
	public String getTimestampName() {
		return this.timestampName;
	}

	public String getTimestampNameAsXPath() {
		if (this.timestampName == null) {
			return null;
		}
		return "/" + this.timestampName.replace(".", "/");
	}

	public void setTimestampName(final String timestampName) {
		if (timestampName != null) {
			final String strippedTimestampName = timestampName.trim().replaceAll(" +", "_");
			if (!this.isValidName(strippedTimestampName)) {
				throw new RuntimeException(
						"Timestamp name is not valid. Only characters [a-z], [A-Z], [0-9], - and _ are allowed!");
			}
			this.timestampName = strippedTimestampName;
		} else {
			this.timestampName = null;
		}
	}

	@JsonIgnore
	public List<TypeTreeNode> getRootLevelValueTypes() {
		return this.attributes.getRoots();
	}

	@JsonIgnore
	public List<TypeTreeNode> getValueTypes() {
		return this.attributes.getAttributes();
	}

	@JsonIgnore
	public AttributeTypeTree getValueTypeTree() {
		return this.attributes;
	}

	public void setValueTypeTree(final AttributeTypeTree attributes) {
		this.attributes = attributes;
	}

	public void setXMLEvent(final boolean isXMLEvent) {
		this.isXMLEvent = isXMLEvent;
	}

	public boolean isXMLEvent() {
		return this.isXMLEvent;
	}

	public String getXMLName() {
		return this.schemaName;
	}

	public void setXMLName(final String xmlName) {
		this.schemaName = xmlName;
	}

	public String getXsdString() {
		return this.xsdString;
	}

	public void setXsdString(final String xsdString) {
		this.xsdString = xsdString;
	}

	@Override
	public String toString() {
		final String processText = this.typeName + " (" + this.ID + ")";
		return processText;
	}

	public static EapEventType findByID(final int ID) {
		final List<EapEventType> eventTypes = EapEventType.findByAttribute("ID", Integer.toString(ID));
		if (!eventTypes.isEmpty()) {
			return eventTypes.get(0);
		} else {
			return null;
		}
	}

	/**
	 * return Eventtype which has the given structuredefinition
	 */
	public static EapEventType findBySchemaName(final String schemaName) {
		final List<EapEventType> eventTypes = EapEventType.findByAttribute("SchemaName", schemaName);
		if (!eventTypes.isEmpty()) {
			return eventTypes.get(0);
		} else {
			return null;
		}
	}

	public static List<EapEventType> findByIDGreaterThan(final int ID) {
		return EapEventType.findByAttributeGreaterThan("ID", Integer.toString(ID));
	}

	public static List<EapEventType> findByIDLessThan(final int ID) {
		return EapEventType.findByAttributeLessThan("ID", Integer.toString(ID));
	}

	public static List<EapEventType> findByAttribute(final String attributeName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"SELECT * FROM EventType WHERE " + attributeName + " = '" + value + "'", EapEventType.class);
		return query.getResultList();
	}

	private static List<EapEventType> findByAttributeGreaterThan(final String attributeName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM EventType " + "WHERE " + attributeName + " > '" + value + "'", EapEventType.class);
		return query.getResultList();
	}

	private static List<EapEventType> findByAttributeLessThan(final String attributeName, final String value) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM EventType " + "WHERE " + attributeName + " < '" + value + "'", EapEventType.class);
		return query.getResultList();
	}

	/**
	 * 
	 * @param typeName
	 *            name of the EventType
	 * @return
	 */
	public static EapEventType findByTypeName(final String typeName) {
		final Query query = Persistor.getEntityManager().createNativeQuery(
				"" + "SELECT * FROM EventType " + "WHERE TypeName = '" + typeName + "'", EapEventType.class);
		// Type names should be distinct!
		assert (query.getResultList().size() < 2);
		try {
			if (query.getResultList().size() > 0) {
				return (EapEventType) query.getResultList().get(0);
			} else {
				return null;
			}
		} catch (final Exception e) {
			System.err.println(e);
			return null;
		}
	}

	public List<String> getNonHierarchicalAttributeExpressionsWithoutTimestamp() {
		// String queryString = "" +
		// "SELECT DISTINCT MapKey FROM EventTransformationElement " +
		// "WHERE ID IN (SELECT treeRootElements_ID FROM TransformationMapTree_TransformationMapTreeRootElements "
		// + // hier nur auf flachen Events
		// "WHERE TransformationMapTree_TransformationMapID IN (SELECT MapTreeID FROM Event "
		// +
		// "WHERE EVENTTYPE_ID = '" + ID + "'))";
		// Query query =
		// Persistor.getEntityManager().createNativeQuery(queryString);
		// if (query.getResultList() == null) return new ArrayList<String>();
		// return query.getResultList();
		return this.attributes.getRootsAsExpression();
	}

	public List<Serializable> findAttributeValues(final String selectedConditionAttribute) {
		// String queryString = "" +
		// "SELECT DISTINCT MapValue FROM EventTransformationElement " +
		// "WHERE MapKey = '" + selectedConditionAttribute + "' AND " +
		// "ID IN (SELECT treeRootElements_ID FROM TransformationMapTree_TransformationMapTreeRootElements "
		// + // hier nur auf flachen Events
		// "WHERE TransformationMapTree_TransformationMapID IN (SELECT MapTreeID FROM Event "
		// +
		// "WHERE EVENTTYPE_ID = '" + ID + "'))";
		// Query query =
		// Persistor.getEntityManager().createNativeQuery(queryString);
		// EapEvent.
		// return query.getResultList();
		if (selectedConditionAttribute == null) {
			return new ArrayList<Serializable>();
		} else {
			final List<Serializable> result = EapEvent.findValuesByEventTypeAndAttributeExpression(this,
					selectedConditionAttribute);
			if (result == null) {
				return new ArrayList<Serializable>();
			}
			return result;
		}
	}

	public static List<EapEventType> findAll() {
		final Query query = Persistor.getEntityManager().createQuery("select t from EapEventType t");
		return query.getResultList();
	}

	/**
	 * returns Eventtypes which have a subset of the given attributes
	 */
	public static List<EapEventType> findMatchingEventTypesForNonHierarchicalAttributes(
			final List<String> attributeExpressions, final String importTimeName) {
		final List<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
		for (final EapEventType eventType : EapEventType.findAll()) {
			// prepare attributes without import time
			final ArrayList<String> attributes = eventType.getNonHierarchicalAttributeExpressions();
			attributes.remove(importTimeName);

			OUTERCHECK: if (attributeExpressions.containsAll(attributes)) {
				selectedEventTypes.add(eventType);
			} else {
				for (final String attribute : attributes) {
					if (attributeExpressions.contains(attribute)) {
						selectedEventTypes.add(eventType);
						break OUTERCHECK;
					}
				}
			}
		}
		return selectedEventTypes;
	}

	@Override
	public EapEventType save() {
		return (EapEventType) super.save();
	}

	public static boolean save(final List<EapEventType> eventTypes) {
		try {
			final EntityManager entityManager = Persistor.getEntityManager();
			entityManager.getTransaction().begin();
			for (final EapEventType eventType : eventTypes) {
				entityManager.persist(eventType);
			}
			entityManager.getTransaction().commit();
			return true;
		} catch (final Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Deletes this event type from the database.
	 * 
	 * @return
	 */
	@Override
	public EapEventType remove() {
		return (EapEventType) super.remove();
	}

	/**
	 * Deletes the specified eventtypes from the database.
	 * 
	 * @return
	 */
	public static boolean remove(final List<EapEventType> eventTypes) {
		boolean removed = true;
		for (final EapEventType eventType : eventTypes) {
			removed = (eventType.remove() != null);
		}
		return removed;
	}

	public static void removeAll() {
		final List<EapEventType> allEventTypes = EapEventType.findAll();
		EapEventType.remove(allEventTypes);
	}

	public static List<String> getAllTypeNames() {
		final ArrayList<String> eventTypeNames = new ArrayList<String>();
		for (final EapEventType eventType : EapEventType.findAll()) {
			eventTypeNames.add(eventType.getTypeName());
		}
		return eventTypeNames;
	}

	public ArrayList<String> getEventAttributes() {
		final Set<String> attributeSet = new HashSet<String>();
		for (final TypeTreeNode attribute : this.getValueTypes()) {
			attributeSet.add(attribute.getAttributeExpression());
		}
		return new ArrayList<String>(attributeSet);
	}
}