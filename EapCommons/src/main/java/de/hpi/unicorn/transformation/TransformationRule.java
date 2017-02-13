/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

/**
 * 
 * Contains all objects that are required for event transformation.
 * Transformation rules are unique by the event type of the transformed events
 * and the user-defined title of the transformation rule.
 * 
 */
@Entity
@Table(name = "TransformationRule", uniqueConstraints = { @UniqueConstraint(columnNames = { "eventType", "title" }) })
public class TransformationRule extends Persistable {

	private static final long serialVersionUID = 6601905480228417174L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "transformationRuleID")
	private int ID;

	@JoinColumn(name = "EventType")
	@ManyToOne
	private EapEventType eventType;

	@ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
	@JoinTable(name = "TransformationRuleEventTypes", joinColumns = { @JoinColumn(name = "Id") })
	@JoinColumn(name = "EventTypes")
	private List<EapEventType> eventTypesOfIncomingEvents;

	@Column(name = "Title")
	private String title;

	@Column(name = "Query", length = 10000)
	private String esperQuery;

	@Column(name = "SparqlQuery", length = 2000)
	private String sparqlQuery;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "PatternTreeID")
	private TransformationPatternTree patternTree;

	@ElementCollection
	@MapKeyColumn(name = "attributeIdentifier")
	@CollectionTable(name = "AttributeIdentifiersAndExpressions", joinColumns = @JoinColumn(name = "AttributeIdentifiersAndExpressionsID"))
	private Map<String, String> attributeIdentifiersAndExpressions;

	@OneToMany(mappedBy = "transformationRule", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	@MapKey(name = "attributeExpression")
	private Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExternalKnowledgeExpressionSets;

	public TransformationRule() {
		this.ID = 0;
		this.title = "";
		this.eventType = null;
		this.eventTypesOfIncomingEvents = new ArrayList<EapEventType>();
		this.esperQuery = "";
		this.sparqlQuery = null;
		this.patternTree = null;
		this.attributeIdentifiersAndExpressions = new HashMap<String, String>();
		this.attributeIdentifiersAndExternalKnowledgeExpressionSets = new HashMap<String, ExternalKnowledgeExpressionSet>();
	}

	/**
	 * Constructor for transformation rules created in the basic rule editor.
	 * DEPRECATED - use TransformationRule(EapEventType eventType,
	 * List<EapEventType> incomingEventTypes, String title, String query)
	 * 
	 * @param eventType
	 *            event type of the transformed events
	 * @param title
	 *            name of the transformation rule
	 * @param query
	 *            Esper query that is used to listen for incoming events and to
	 *            create the transformed events
	 */
	@Deprecated
	public TransformationRule(final EapEventType eventType, final String title, final String query) {
		this();
		this.eventType = eventType;
		this.title = title;
		this.setQuery(query);
	}

	/**
	 * Constructor for transformation rules created in the basic rule editor.
	 * 
	 * @param eventType
	 *            event type of the transformed events
	 * @param eventTypesOfIncomingEvents
	 *            event types of incoming normalized events
	 * @param title
	 *            name of the transformation rule
	 * @param query
	 *            Esper query that is used to listen for incoming events and to
	 *            create the transformed events
	 */
	public TransformationRule(final EapEventType eventType, final List<EapEventType> eventTypesOfIncomingEvents,
			final String title, final String query) {
		this();
		this.eventType = eventType;
		this.eventTypesOfIncomingEvents = eventTypesOfIncomingEvents;
		this.title = title;
		this.setQuery(query);
	}

	/**
	 * Constructor for transformation rules created in the advanced rule editor.
	 * 
	 * @param eventType
	 *            event type of the transformed events
	 * @param title
	 *            name of the transformation rule
	 * @param patternTree
	 *            pattern that is used to listen for events, built up from the
	 *            provided elements
	 * @param attributeIdentifiersAndExpressions
	 *            pairs of attribute identifiers and expressions - determines
	 *            what values are stored in the transformed events
	 * @param attributeIdentifiersAndExpressionSets
	 *            pairs of attribute identifiers and sets of expressions
	 *            determining the fetch of external knowledge
	 */
	public TransformationRule(final EapEventType eventType, final String title,
			final TransformationPatternTree patternTree, final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets) {
		this(eventType, title, null);
		for (final String key : attributeIdentifiersAndExpressionSets.keySet()) {
			attributeIdentifiersAndExpressions.remove(key);
		}
		this.patternTree = patternTree;
		this.attributeIdentifiersAndExpressions = attributeIdentifiersAndExpressions;
		this.attributeIdentifiersAndExternalKnowledgeExpressionSets = attributeIdentifiersAndExpressionSets;
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

	public List<EapEventType> getEventTypesOfIncomingEvents() {
		return this.eventTypesOfIncomingEvents;
	}

	public void setEventTypesOfIncomingEvents(final List<EapEventType> eventTypesOfIncomingEvents) {
		this.eventTypesOfIncomingEvents = eventTypesOfIncomingEvents;
	}

	public String getTitle() {
		return this.title;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public String getQuery() {
		final StringBuffer sb = new StringBuffer();
		sb.append(this.esperQuery);
		if (this.sparqlQuery != null) {
			sb.append(" WHERE { " + this.sparqlQuery + " }");
		}
		return sb.toString();
	}

	public void setQuery(final String query) {
		if (query != null) {
			final int beginOfSparqlQuery = query.toUpperCase().indexOf("WHERE {");
			final int endOfSparqlQuery = query.indexOf("}");
			if (beginOfSparqlQuery < 0) {
				this.esperQuery = query;
				this.sparqlQuery = null;
			} else {
				this.esperQuery = query.substring(0, beginOfSparqlQuery);
				this.sparqlQuery = query.substring(beginOfSparqlQuery + 7, endOfSparqlQuery);
			}
		}
	}

	public String getEsperQuery() {
		if (this.esperQuery == null || this.esperQuery.isEmpty()) {
			this.esperQuery = EsperTransformationRuleParser.getInstance().parseRule(this.patternTree,
					this.attributeIdentifiersAndExpressions,
					this.attributeIdentifiersAndExternalKnowledgeExpressionSets);
		}
		return this.esperQuery;
	}

	public void setEsperQuery(final String query) {
		this.esperQuery = query;
	}

	public boolean hasSparqlQuery() {
		if (this.sparqlQuery == null || this.sparqlQuery.isEmpty()) {
			return false;
		}
		return true;
	}

	public String getSparqlQuery() {
		return this.sparqlQuery;
	}

	public void setSparqlQuery(final String sparqlQuery) {
		this.sparqlQuery = sparqlQuery;
	}

	public TransformationPatternTree getPatternTree() {
		return this.patternTree;
	}

	public void setPatternTree(final TransformationPatternTree patternTree) {
		this.patternTree = patternTree;
	}

	public Map<String, String> getAttributeIdentifiersAndExpressions() {
		return this.attributeIdentifiersAndExpressions;
	}

	public void setAttributeIdentifiersAndExpressions(final Map<String, String> attributeIdentifiersAndExpressions) {
		this.attributeIdentifiersAndExpressions = attributeIdentifiersAndExpressions;
	}

	public Map<String, ExternalKnowledgeExpressionSet> getAttributeIdentifiersWithExternalKnowledge() {
		return this.attributeIdentifiersAndExternalKnowledgeExpressionSets;
	}

	public void setAttributeIdentifiersAndExpressionSets(
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets) {
		this.attributeIdentifiersAndExternalKnowledgeExpressionSets = attributeIdentifiersAndExpressionSets;
	}

	@Override
	public TransformationRule save() {
		for (final String attributeIdentifer : this.attributeIdentifiersAndExternalKnowledgeExpressionSets.keySet()) {
			if (this.attributeIdentifiersAndExternalKnowledgeExpressionSets.get(attributeIdentifer)
					.getTransformationRule() == null) {
				this.attributeIdentifiersAndExternalKnowledgeExpressionSets.get(attributeIdentifer)
						.setTransformationRule(this);
			}
		}
		return (TransformationRule) super.save();
	}

	@Override
	public TransformationRule remove() {
		return (TransformationRule) super.remove();
	}

	public static List<TransformationRule> findAll() {
		final Query q = Persistor.getEntityManager().createQuery("SELECT t FROM TransformationRule t",
				TransformationRule.class);
		return q.getResultList();
	}

	public static List<TransformationRule> findByEventType(final EapEventType eventType) {
		final EntityManager em = Persistor.getEntityManager();
		em.clear();
		/*
		 * the ID of the eventType is stored in the database, not the whole
		 * EapEventType
		 */
		final Query query = em.createNativeQuery(
				"SELECT * FROM TransformationRule WHERE EventType = " + eventType.getID(), TransformationRule.class);
		try {
			return query.getResultList();
		} catch (final Exception e) {
			return null;
		}
	}

	public static TransformationRule findByEventTypeAndTitle(final String eventTypeName, final String title) {
		final EntityManager em = Persistor.getEntityManager();
		/*
		 * the ID of the eventType is stored in the database, not the whole
		 * EapEventType
		 */
		em.clear();
		final Query query = em.createNativeQuery(
				"SELECT * FROM TransformationRule WHERE EventType = (SELECT ID FROM EventType WHERE TypeName = '"
						+ eventTypeName + "') AND Title = '" + title + "'", TransformationRule.class);
		try {
			return (TransformationRule) query.getResultList().get(0);
		} catch (final Exception e) {
			return null;
		}
	}

	/**
	 * Finds all transformation rules whose queries are evaluated against the
	 * event.
	 * 
	 * @param event
	 *            event in question
	 * @return transformation rules whose queries are evaluated against the
	 *         event
	 */
	public static List<TransformationRule> getTransformationRulesForEvent(final EapEvent event) {
		final List<TransformationRule> transformationRules = TransformationRule.findAll();
		final List<TransformationRule> relevantTransformationRules = new ArrayList<TransformationRule>();
		for (final TransformationRule rule : transformationRules) {
			for (final EapEventType eventType : rule.getEventTypesOfIncomingEvents()) {
				if (eventType.equals(event.getEventType())) {
					relevantTransformationRules.add(rule);
					break;
				}
			}
		}
		return relevantTransformationRules;
	}
}
