/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.attributeDependency;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;
import org.apache.log4j.Logger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import java.util.Map;
import java.util.List;

/**
 * This class represents the dependencies between attributes of one event type.
 * You can specify that one attribute determines the values of another attribute.
 * See @AttributeValueDependency for the actual dependent values.
 *
 *
 * If you are placing many requests towards the static methods of this class, please consider using the @AttributeDependencyManager. It will "cache"
 * the results for you and reduce the amount of direct database requests.
 *
 */
@Entity
@Table(name = "AttributeDependency",
        uniqueConstraints = @UniqueConstraint(columnNames = {"EventType", "BaseAttribute", "DependentAttribute"}))
public class AttributeDependency extends Persistable {

    private static final String EVENTTYPE_LITERAL = "eventType";
    private static final String BASEATTRIBUTE_LITERAL = "baseAttribute";
    private static final String DEPENDENTATTRIBUTE_LITERAL = "dependentAttribute";
    private static final long serialVersionUID = 1L;
    private static final String SELECT_A_ATTRIBUTEDEPENDENCY = "SELECT a FROM AttributeDependency a";
    private static final String WHERE_MATCH_EVENTTYPE = " WHERE a.eventType = :eventType";


    private static final Logger logger = Logger.getLogger(AttributeDependency.class);

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;

    @ManyToOne
    @JoinColumn(name = "EventType")
    private EapEventType eventType;

    @ManyToOne
    @JoinColumn(name = "BaseAttribute")
    private TypeTreeNode baseAttribute;

    @ManyToOne
    @JoinColumn(name = "DependentAttribute")
    private TypeTreeNode dependentAttribute;

    /**
     * Basic constructor for AttributeDependency (no ID given, defaults to 0).
     */
    public AttributeDependency() {
        this.ID = 0;
    }

    /**
     * Default constructor for AttributeDependency.
     *
     * @param eventType the dependency is defined on
     * @param baseAttribute whose input will define the value of the
     * @param dependentAttribute which will get a value defined in the dependency if a rule matches
     */
    public AttributeDependency(EapEventType eventType, TypeTreeNode baseAttribute, TypeTreeNode dependentAttribute) {
        this();
        this.eventType = eventType;
        this.baseAttribute = baseAttribute;
        this.dependentAttribute = dependentAttribute;
    }

    /**
     * Getter for ID of dependency.
     *
     * @return an integer
     */
    @Override
    public int getID() {
        return this.ID;
    }

    /**
     * Getter for base attribute.
     *
     * @return a TypeTreeNode / attribute
     */
    public TypeTreeNode getBaseAttribute() { return this.baseAttribute; }

    /**
     * Getter for dependent attribute.
     *
     * @return a TypeTreeNode / attribute
     */
    public TypeTreeNode getDependentAttribute() { return this.dependentAttribute; }

    /**
     * Getter for event type on which the dependency is defined on.
     *
     * @return an EventType
     */
    public EapEventType getEventType() { return eventType; }

    /**
     * Add multiple value dependencies to this dependency.
     * If there already are some value dependencies, a merge is tried.
     *
     * @param values a map containing base values associated with dependent values
     * @return a bool if save/merge was successful
     */
    public boolean addDependencyValues(Map<String, String> values) {
        List<AttributeValueDependency> attributeValueDependencies = AttributeValueDependency.getAttributeValueDependenciesFor(this);
        try {
            for (Map.Entry entry : values.entrySet()) {
                // Check if value dependency already exists that should be updated instead of creating a new one
                boolean updatedValueDependency = false;
                for (AttributeValueDependency attributeValueDependency : attributeValueDependencies) {
                    if (attributeValueDependency.getBaseAttributeValue().equals(entry.getKey().toString())) {
                        attributeValueDependency.setDependentAttributeValues(entry.getValue().toString());
                        attributeValueDependency.merge();
                        updatedValueDependency = true;
                        break;
                    }
                }
                if (!updatedValueDependency) {
                    AttributeValueDependency value = new AttributeValueDependency(this, entry.getKey().toString(), entry.getValue().toString());
                    if (value.save() == null) {
                        return false;
                    }
                }
            }
        } catch (Exception e) {
            logger.debug("Error while merging new dependencyValues into dependency.", e);
            return false;
        }
        return true;
    }

    /**
     * Static method to retrieve all dependencies defined on a given EventType.
     *
     * @param eventType the dependencies should be returned for
     * @return a list with AttributeDependencies
     */
    public static List<AttributeDependency> getAttributeDependenciesWithEventType(EapEventType eventType) {
        final Query query = Persistor.getEntityManager().createQuery(SELECT_A_ATTRIBUTEDEPENDENCY
                + WHERE_MATCH_EVENTTYPE,
                AttributeDependency.class).setParameter(EVENTTYPE_LITERAL, eventType);
        return query.getResultList();
    }

    /**
     * Static method to retrieve all dependencies having the given attribute as base attribute.
     *
     * @param baseAttribute the dependencies should be returned for
     * @return a list with AttributeDependencies
     */
    public static List<AttributeDependency> getAttributeDependenciesForAttribute(TypeTreeNode baseAttribute) {
        final Query query = Persistor.getEntityManager().createQuery(SELECT_A_ATTRIBUTEDEPENDENCY
                        + WHERE_MATCH_EVENTTYPE
                        + " AND a.baseAttribute = :baseAttribute",
                AttributeDependency.class).setParameter(EVENTTYPE_LITERAL, baseAttribute.getEventType()).setParameter("baseAttribute", baseAttribute);
        return query.getResultList();
    }

    /**
     * Tries to fetch an existing AttributeDependency specified by EventType, base and dependent attribute, if it already exists.
     * If there is no dependency defined for this constellation, null is returned.
     *
     * @param eventType the dependency should be defined on
     * @param baseAttribute the dependency should be defined on
     * @param dependentAttribute the dependency should be defined on
     * @return an attributeDependency or null
     */
    public static AttributeDependency getAttributeDependencyIfExists(EapEventType eventType, TypeTreeNode baseAttribute, TypeTreeNode
            dependentAttribute) {
        final Query query = Persistor.getEntityManager().createQuery(SELECT_A_ATTRIBUTEDEPENDENCY
                        + WHERE_MATCH_EVENTTYPE
                        + " AND a.baseAttribute = :baseAttribute"
                        + " AND a.dependentAttribute = :dependentAttribute",
                AttributeDependency.class)
                .setParameter(EVENTTYPE_LITERAL, eventType)
                .setParameter(BASEATTRIBUTE_LITERAL, baseAttribute)
                .setParameter(DEPENDENTATTRIBUTE_LITERAL, dependentAttribute);
        try {
            return (AttributeDependency) query.getResultList().get(0);
        }
        catch (Exception e) {
            logger.debug("No fitting dependency found.", e);
            return null;
        }
    }
}
