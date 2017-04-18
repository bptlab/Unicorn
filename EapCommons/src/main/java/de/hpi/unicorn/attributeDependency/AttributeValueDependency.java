/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.attributeDependency;


import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

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

import java.util.List;

/**
 * This class represents the dependent attribute values for one event type.
 * You can specify that one particular attribute value determines particular attribute values of another attribute.
 * Which attribute that is, is specified in @AttributeDependency.
 */
@Entity
@Table(name = "AttributeValueDependency",
        uniqueConstraints = @UniqueConstraint(columnNames = {"DependencyRule", "BaseAttributeValue"}))
public class AttributeValueDependency extends Persistable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int ID;

    @ManyToOne
    @JoinColumn(name = "DependencyRule")
    private AttributeDependency dependencyRule;

    @Column(name = "BaseAttributeValue")
    private String baseAttributeValue;

    @Column(name = "DependentAttributeValues")
    private String dependentAttributeValues;

    /**
     * Basic constructor for AttributeValueDependency (no ID given, defaults to 0).
     */
    public AttributeValueDependency() {
        this.ID = 0;
    }

    /**
     * Default constructor for AttributeValueDependency.
     *
     * @param dependencyRule a AttributeDependency this value dependency should be defined in
     * @param baseAttributeValue contains the value triggering this dependency
     * @param dependentAttributeValues contains the values that should be used within this rule for the dependent attribute value
     */
    public AttributeValueDependency(AttributeDependency dependencyRule, String baseAttributeValue, String dependentAttributeValues) {
        this();
        this.dependencyRule = dependencyRule;
        this.baseAttributeValue = baseAttributeValue;
        this.dependentAttributeValues = dependentAttributeValues;
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
     * Getter for the value triggering this dependency rule.
     *
     * @return a string containing the value of the base attribute
     */
    public String getBaseAttributeValue() { return this.baseAttributeValue; }

    /**
     * Getter for the values that can be used within this rule for the dependent attribute.
     *
     * @return a string containing the dependent attribute value in this rule
     */
    public String getDependentAttributeValues() { return this.dependentAttributeValues; }

    /**
     * Setter for the values that can be used within this rule for the dependent attribute.
     *
     * @param dependentAttributeValues new values for the dependent attribute
     */
    public void setDependentAttributeValues(String dependentAttributeValues) {
        this.dependentAttributeValues = dependentAttributeValues;
    }

    /**
     * Getter for the AttributeDependency-rule this value dependency is defined on.
     *
     * @return an AttributeDependency
     */
    public AttributeDependency getDependencyRule() { return this.dependencyRule; }

    /**
     * Static method to return all AttributeValueDependencies defined on a given AttributeDependency.
     *
     * @param attributeDependency the AttributeValueDependencies should be returned for
     * @return a list with AttributeValueDependencies
     */
    public static List<AttributeValueDependency> getAttributeValueDependenciesFor(AttributeDependency attributeDependency) {
        final Query query = Persistor.getEntityManager().createQuery("SELECT a FROM AttributeValueDependency a WHERE a.dependencyRule = "
                + ":depRule", AttributeValueDependency.class).setParameter("depRule", attributeDependency);
        return query.getResultList();
    }

    /**
     * Find the value dependency with the given dependency rule and base value.
     * As this combination is a primary key, there is just one value dependency returned.
     *
     * @param attributeDependency AttributeDependency of searched value dependency
     * @param baseValue base value of searched value dependency
     * @return value dependency that matches the search criteria
     */
    public static AttributeValueDependency getAttributeValueDependencyFor(AttributeDependency attributeDependency, String baseValue) {
        List<AttributeValueDependency> dependencyValues = AttributeValueDependency.getAttributeValueDependenciesFor(attributeDependency);
        for (AttributeValueDependency value : dependencyValues) {
            if (baseValue.equals(value.getBaseAttributeValue())) {
                return value;
            }
        }
        return null;
    }

}
