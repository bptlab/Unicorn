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

import java.util.List;


/**
 * The AttributeDependencyManager allows to request dependencies and their values for a given event type.
 * It is used to reduce the database-access for basic requests.
 *
 */
public class AttributeDependencyManager {
	private EapEventType eventType;
	private List<AttributeDependency> attributeDependencies;

	public AttributeDependencyManager(EapEventType eventType) {
		this.eventType = eventType;
		this.attributeDependencies = AttributeDependency.getAttributeDependenciesWithEventType(eventType);
	}

	/**
	 * Checks whether a dependency was configured so that this attribute is dependent of another.
	 *
	 * @param attribute Attribute to be checked to be a dependent attribute
	 */
	public boolean isDependentAttributeInDependency(TypeTreeNode attribute) {
		for(AttributeDependency attributeDependency : attributeDependencies) {
			if(attributeDependency.getDependentAttribute().equals(attribute)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Checks whether a dependency was configured so that this attribute defines the value of another attribute.
	 *
	 * @param attribute Attribute to be checked to be a base attribute
	 */
	public boolean isBaseAttributeInDependency(TypeTreeNode attribute) {
		for(AttributeDependency attributeDependency : attributeDependencies) {
			if(attributeDependency.getBaseAttribute().equals(attribute)) {
				return true;
			}
		}
		return false;
	}
}
