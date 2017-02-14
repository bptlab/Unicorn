/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.correlation;

import javax.persistence.*;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistable;

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
    @JoinColumn(name = "LeadingAttribute")
    private TypeTreeNode leadingAttribute;

    @ManyToOne
    @JoinColumn(name = "DependentAttribute")
    private TypeTreeNode dependentAttribute;

    public AttributeDependency() {
    }

    @Override
    public int getID() {
        return this.ID;
    }

}
