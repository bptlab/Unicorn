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
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * The AttributeDependencyManager allows to request dependencies and their values for a given event type.
 * Use it to reduce the database-access for basic requests.
 *
 */
public class AttributeDependencyManager implements Serializable {
	private EapEventType eventType;
	private List<AttributeDependency> attributeDependencies;
	private Map<AttributeDependency, List<AttributeValueDependency>> attributeValueDependencies = new HashMap<>();

	public AttributeDependencyManager(EapEventType eventType) {
		this.eventType = eventType;
		this.attributeDependencies = AttributeDependency.getAttributeDependenciesWithEventType(eventType);
		for(AttributeDependency attributeDependency : attributeDependencies) {
			attributeValueDependencies.put(attributeDependency, AttributeValueDependency.getAttributeValueDependenciesFor
					(attributeDependency));
		}
	}

	/**
	 * Returns a List with all dependencies.
	 *
	 * @return a list with attribute dependencies
	 */
	public List<AttributeDependency> getAttributeDependencies() {
		return this.attributeDependencies;
	}

	/**
	 * Returns a List with all dependencies where the given attribute is set as base attribute.
	 *
	 * @param baseAttribute the attribute all dependencies should be returned for
	 * @return a list with attribute dependencies
	 */
	public List<AttributeDependency> getAttributeDependenciesForAttribute(final TypeTreeNode baseAttribute) {
		ArrayList<AttributeDependency> filteredDependenciesList = new ArrayList<>(attributeDependencies);
		CollectionUtils.filter(filteredDependenciesList, new Predicate() {
			@Override
			public boolean evaluate(Object attributeDependency) {
				return baseAttribute.equals(((AttributeDependency) attributeDependency).getBaseAttribute());
			}
		});
		return filteredDependenciesList;
	}

	/**
	 * Returns a list with all value dependencies for a given attribute dependency.
	 *
	 * @param attributeDependency the dependency all value dependencies should be returned for
	 * @return a list with the attribute value dependencies
	 */
	public List<AttributeValueDependency> getAttributeValueDependenciesForAttributeDependency(final AttributeDependency attributeDependency) {
		return attributeValueDependencies.get(attributeDependency);
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

	/**
	 * If a dependency entry for the given event-type, base attribute, dependent attribute triple already exist this entry is returned, otherwise a
	 * new dependency entry for this triple is created, saved and returned.
	 *
	 * @param eventType the dependency is for
	 * @param baseAttribute of the dependency
	 * @param dependentAttribute of the dependency
	 * @return the corresponding or new attribute dependency
	 */
	public static AttributeDependency getAttributeDependency(EapEventType eventType, TypeTreeNode baseAttribute, TypeTreeNode dependentAttribute) {
		AttributeDependency attributeDependency = AttributeDependency.getAttributeDependencyIfExists(eventType, baseAttribute, dependentAttribute);
		if(attributeDependency == null) {
			attributeDependency = new AttributeDependency(eventType, baseAttribute, dependentAttribute);
			attributeDependency.save();
		}
		return attributeDependency;
	}
}
