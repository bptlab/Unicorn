/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.attributeDependency;

import javax.management.Attribute;
import javax.persistence.*;

import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

import java.util.List;

/**
 * This class represents the dependent attribute values for one event type.
 * You can specify that one particular attribute value determines particular attribute values of another attribute.
 * Which attribute that is, is specified in @AttributeDependency.
 */
@Entity
@Table(name = "AttributeValueDependency")
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

    public AttributeValueDependency() {
        this.ID = 0;
    }

    public AttributeValueDependency(AttributeDependency dependencyRule, String baseAttributeValue, String dependentAttributeValues) {
        this();
        this.dependencyRule = dependencyRule;
        this.baseAttributeValue = baseAttributeValue;
        this.dependentAttributeValues = dependentAttributeValues;
    }

    public static List<AttributeValueDependency> getAttributeValueDependenciesForAttributeDependency(AttributeDependency attributeDependency) {
        final Query query = Persistor.getEntityManager().createQuery("SELECT a FROM AttributeValueDependency a WHERE a.dependencyRule = " +
                ":depRule", AttributeValueDependency.class).setParameter("depRule",attributeDependency);
        return query.getResultList();
    }

    @Override
    public int getID() {
        return this.ID;
    }

    public String getBaseAttributeValue() { return this.baseAttributeValue; }

    public String getDependentAttributeValues() { return this.dependentAttributeValues; }

}
