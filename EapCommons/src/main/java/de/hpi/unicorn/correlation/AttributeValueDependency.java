/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import javax.persistence.*;
import de.hpi.unicorn.persistence.Persistable;

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

    @Column(name = "LeadingAttributeValue")
    private String leadingAttributeValue;

    @Column(name = "DependentAttributeValues")
    private String dependentAttributeValues;

    public AttributeValueDependency() {
    }

    @Override
    public int getID() {
        return this.ID;
    }
}
