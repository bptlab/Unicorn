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

    public EapEventType getEventType() { return eventType; }

    public boolean addDependencyValues(Map<String, String> values) {
        List<AttributeValueDependency> attributeValueDependencies = AttributeValueDependency.getAttributeValueDependenciesFor(this);
        try {
            for (Map.Entry entry : values.entrySet()) {
                // Check if value dependency already exists that should be updated instead of creating a new one
                boolean updatedValueDependency = false;
                for(AttributeValueDependency attributeValueDependency : attributeValueDependencies) {
                    if(attributeValueDependency.getBaseAttributeValue().equals(entry.getKey().toString())) {
                        attributeValueDependency.setDependentAttributeValues(entry.getValue().toString());
                        attributeValueDependency.merge();
                        updatedValueDependency = true;
                        break;
                    }
                }
                if(!updatedValueDependency) {
                    AttributeValueDependency value = new AttributeValueDependency(this, entry.getKey().toString(), entry.getValue().toString());
                    value.save();
                }
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

    public static AttributeDependency getAttributeDependencyIfExists(EapEventType eventType, TypeTreeNode baseAttribute, TypeTreeNode
            dependentAttribute) {
        final Query query = Persistor.getEntityManager().createQuery("SELECT a FROM AttributeDependency a WHERE a.eventType = :eventType AND " +
                        "a.baseAttribute = :baseAttribute AND " +
                        "a.dependentAttribute = :dependentAttribute",
                AttributeDependency.class)
                .setParameter("eventType", eventType)
                .setParameter("baseAttribute", baseAttribute)
                .setParameter("dependentAttribute", dependentAttribute);
        try {
            return (AttributeDependency) query.getResultList().get(0);
        }
        catch (Exception e){
            return null;
        }
    }
}
