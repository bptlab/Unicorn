/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation;

import java.io.Serializable;
import java.util.Map;

import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

/**
 * Provides methods to parse queries in EPL language from a transformation rule.
 */
public abstract class TransformationRuleParser {

	/**
	 * Parses an EPL query from the given parameters.
	 * 
	 * @param attributeIdentifiersAndExpressions
	 *            pairs of attribute identifiers and expressions - determines
	 *            what values are stored in the transformed events
	 * @param patternTree
	 *            pattern that is used to listen for events, built up from the
	 *            provided elements
	 * @return EPL query
	 */
	public abstract String parseRule(TransformationPatternTree patternTree,
			Map<String, String> attributeIdentifiersAndExpressions,
			Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets);

	/**
	 * Parses the pattern part of an EPL query.
	 * 
	 * @param element
	 *            root element of the pattern tree
	 * @return pattern part of EPL query
	 */
	protected abstract String buildPatternString(EventTreeElement<Serializable> element);

	/**
	 * Parses the value selection part of an EPL query.
	 * 
	 * @param attributeIdentifiersAndExpressions
	 *            pairs of attribute identifiers and expressions - determines
	 *            what values are stored in the transformed events
	 * @param attributeIdentifiersAndExpressionSets
	 *            pairs of attribute identifiers and sets of expressions
	 *            determining the fetch of external knowledge
	 * @return value selection part of EPL query
	 */
	protected abstract String buildValueSelectionString(Map<String, String> attributeIdentifiersAndExpressions,
			Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets);

	/**
	 * Parses the external knowledge information for the value selection part of
	 * an EPL query.
	 * 
	 * @param externalKnowledge
	 *            sets of expressions determining the fetch of external
	 *            knowledge
	 * @return external knowledge information part of value selection part of
	 *         EPL query
	 */
	protected abstract String buildValueSelectionStringFromExternalKnowledge(
			ExternalKnowledgeExpressionSet externalKnowledge);
}
