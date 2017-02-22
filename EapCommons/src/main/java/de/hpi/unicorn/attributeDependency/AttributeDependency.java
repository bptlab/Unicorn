/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.attributeDependency;

import javax.persistence.*;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;
import de.hpi.unicorn.persistence.Persistor;

import java.util.Map;
import java.util.List;

/**
 * This class represents the dependencies between attributes of one event type.
 * You can specify that one attribute determines the values of another attribute.
 * See @AttributeValueDependency for the actual dependent values.
 */
@Entity
@Table(name = "AttributeDependency")
public class AttributeDependency extends Persistable {

    private static final long serialVersionUID = 1L;

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

    public AttributeDependency() {
        this.ID = 0;
    }

    public AttributeDependency(EapEventType eventType, TypeTreeNode baseAttribute, TypeTreeNode dependentAttribute) {
        this();
        this.eventType = eventType;
        this.baseAttribute = baseAttribute;
        this.dependentAttribute = dependentAttribute;
    }

    @Override
    public int getID() {
        return this.ID;
    }

    public TypeTreeNode getBaseAttribute() { return this.baseAttribute; }

    public TypeTreeNode getDependentAttribute() { return this.dependentAttribute; }


    public boolean addDependencyValues(Map<String, String> values) {
        try {
            for (Map.Entry entry : values.entrySet()) {
                AttributeValueDependency value = new AttributeValueDependency(this, entry.getKey().toString(), entry.getValue().toString());
                value.save();
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public static List<AttributeDependency> getAttributeDependenciesWithEventType(EapEventType eventType) {
        final Query query = Persistor.getEntityManager().createQuery("SELECT a FROM AttributeDependency a WHERE a.eventType = :eventType",
                AttributeDependency.class).setParameter("eventType", eventType);
        return query.getResultList();
    }

    public static List<AttributeDependency> getAttributeDependenciesForAttribute(TypeTreeNode baseAttribute) {
        final Query query = Persistor.getEntityManager().createQuery("SELECT a FROM AttributeDependency a WHERE a.eventType = :eventType AND a" +
                        ".baseAttribute = :baseAttribute",
                AttributeDependency.class).setParameter("eventType", baseAttribute.getEventType()).setParameter("baseAttribute", baseAttribute);
        return query.getResultList();
    }
}
