/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPStatement;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.FilterExpressionOperatorEnum;
import de.hpi.unicorn.transformation.element.PatternOperatorElement;
import de.hpi.unicorn.transformation.element.PatternOperatorEnum;
import de.hpi.unicorn.transformation.element.RangeElement;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpression;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

/**
 * Handles the transformation of events. Checks transformations rules for
 * validity and provides means to initially register all transformation rules
 * located in the database
 * 
 * BachelorSeminar: - registers TransformationRules at StreamProcessingAdapter [
 * initializeWithTransformationRulesFromDB(), register() ] - activate and
 * deactivate TransformationRules [ startStatement(), stopStatement() ] - checks
 * transformations rules for validity [ checkForValidity() ] - removes
 * TransformationRules from StreamProcessingAdapter [ removeFromEsper() ]
 * 
 * NECESSARY METHODS: -
 */
public class TransformationRuleLogic implements Serializable {

	private static final long serialVersionUID = -6756132263243847948L;
	private static TransformationRuleLogic instance = null;

	public TransformationRuleLogic() {
		// initializeWithTransformationRulesFromDB();
	}

	/**
	 * Registers transformation rules that are stored in the database at the
	 * stream processing adapter.
	 */
	public void initializeWithTransformationRulesFromDB() {
		for (final TransformationRule transformationRule : TransformationRule.findAll()) {
			/*
			 * NOTE: one statement for each transformation rule
			 * 
			 * other option would be: if the transformation rule is equal an
			 * existing one, a second listener (probably transforming to another
			 * event type but with the same attributes) would be added to the
			 * existing statement
			 */
			/*
			 * MH 2014-10-21: Only add a TransformationRule from the database if
			 * it not already registered with Esper
			 */
			final EPStatement statement = StreamProcessingAdapter.getInstance().getStatement(
					TransformationRuleLogic.generateStatementName(transformationRule));
			if (statement == null) {
				Broker.getInstance().register(transformationRule);
				System.out.println("Registered transformation rule '" + transformationRule.getTitle()
						+ "' for event type '" + transformationRule.getEventType().getTypeName() + "' from database.");
			} else {
				System.out.println("Transformation rule " + transformationRule.getTitle()
						+ " already registered in Esper.");
			}
		}
	}

	public static TransformationRuleLogic getInstance() {
		if (TransformationRuleLogic.instance == null) {
			TransformationRuleLogic.instance = new TransformationRuleLogic();
		}
		return TransformationRuleLogic.instance;
	}

	public static boolean instanceIsCleared() {
		return (TransformationRuleLogic.instance == null);
	}

	public static void clearInstance() {
		TransformationRuleLogic.instance = null;
	}

	/**
	 * This method generates a name for an EP statement from the title of a
	 * given {@link TransformationRule}
	 * 
	 * @param transformationRule
	 *            the {@link TransformationRule}
	 * @return the name of the statement
	 */
	public static String generateStatementName(final TransformationRule transformationRule) {// TODO:
																								// necessary
																								// method
		return ("transformation_" + transformationRule.getEventType().getTypeName() + "_" + transformationRule
				.getTitle()).toLowerCase();
	}

	/**
	 * Checks components of a transformation rule for validity.
	 * 
	 * @param selectedEventType
	 *            event type of the resulting events
	 * @param transformationRuleName
	 *            name of transformation rule
	 * @param attributeIdentifiersAndExpressions
	 *            map of attributes and desired values of the resulting events
	 * @param attributeIdentifiersWithExternalKnowledge
	 *            map of attributes using external knowledge and their sets of
	 *            external knowledge expressions
	 * @param patternTree
	 *            pattern determining when a new event is created
	 * @throws RuntimeException
	 *             exception message may be used to output errors on the user
	 *             interface
	 */
	public void checkForValidity(final EapEventType selectedEventType, final String transformationRuleName,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge,
			final TransformationPatternTree patternTree) throws RuntimeException {// TODO:
		// necessary
		// method
		if (patternTree.isEmpty()) {
			throw new RuntimeException("Pattern builder: Please provide a pattern!");
		}
		for (final PatternOperatorElement element : patternTree.getPatternOperatorElements()) {
			if (element.getValue() == PatternOperatorEnum.UNTIL) {
				final RangeElement rangeElement = element.getRangeElement();
				if ((rangeElement.getLeftEndpoint() < 0 && rangeElement.getRightEndpoint() < 0)
						|| !(rangeElement.getLeftEndpoint() < rangeElement.getRightEndpoint())) {
					throw new RuntimeException(
							"Pattern builder: Please check the range specification(s) of the UNTIL pattern operator(s)!");
				}
			} else if (element.getValue() == PatternOperatorEnum.EVERY_DISTINCT) {
				if (element.getDistinctAttributes().isEmpty()) {
					throw new RuntimeException("Pattern builder: Please provide distinct attributes!");
				}
			} else if (element.getValue() == PatternOperatorEnum.REPEAT) {
				final RangeElement rangeElement = element.getRangeElement();
				if (!(rangeElement.getLeftEndpoint() > 0)) {
					throw new RuntimeException(
							"Pattern builder: Please check the provided number of event occurences for the REPEAT pattern operator(s)!");
				}
			}
		}
		for (final EventTypeElement element : patternTree.getEventTypeElements()) {
			if (!element.hasAlias()) {
				throw new RuntimeException("Pattern builder: An alias is required for each event type!");
			}
		}
		for (final FilterExpressionElement element : patternTree.getFilterExpressionElements()) {
			if ((element.getValue() == FilterExpressionOperatorEnum.IN || element.getValue() == FilterExpressionOperatorEnum.NOT_IN)) {
				if (element.isRightHandSideRangeBased()) {
					final RangeElement rangeElement = element.getRightHandSideRangeOfValues();
					if (!(rangeElement.getLeftEndpoint() < rangeElement.getRightEndpoint())) {
						throw new RuntimeException(
								"Pattern builder: Please check the range specifications in your filter expressions!");
					}
				} else {
					if (element.getRightHandSideListOfValues().isEmpty()) {
						throw new RuntimeException(
								"Pattern builder: The list of values in your filter expressions is empty!");
					}
				}
			} else {
				if (element.getLeftHandSideExpression() == null || element.getLeftHandSideExpression().isEmpty()
						|| element.getRightHandSideExpression() == null
						|| element.getRightHandSideExpression().isEmpty()) {
					throw new RuntimeException("Pattern builder: Please check your filter expressions for completion!");
				}
			}
		}
		if (selectedEventType == null) {
			throw new RuntimeException("Attribute selection: Please select the event type for the transformed events!");
		}
		final Iterator<String> iterator = attributeIdentifiersAndExpressions.keySet().iterator();
		while (iterator.hasNext()) {
			final String attributeIdentifier = iterator.next();
			if (attributeIdentifiersWithExternalKnowledge.get(attributeIdentifier) != null) {
				for (final ExternalKnowledgeExpression expression : attributeIdentifiersWithExternalKnowledge.get(
						attributeIdentifier).getExternalKnowledgeExpressions()) {
					if (expression.getCriteriaAttributesAndValues().isEmpty()
							|| expression.getDesiredAttribute() == null || expression.getEventType() == null) {
						throw new RuntimeException(
								"Attribute selection: Incomplete information provided for retrieval of external knowledge - please check!");
					}
				}
			} else if (attributeIdentifiersAndExpressions.get(attributeIdentifier) == null
					|| attributeIdentifiersAndExpressions.get(attributeIdentifier).isEmpty()) {
				throw new RuntimeException("Attribute selection: Please provide values for all attributes!");
			}
		}
		if (transformationRuleName == null || transformationRuleName.isEmpty()) {
			throw new RuntimeException("Please provide a name for your transformation rule!");
		}
	}

	/**
	 * Collects all necessary parameters for a transformation rule and creates
	 * it.
	 * 
	 * @param selectedEventType
	 *            event type of the resulting events
	 * @param transformationRuleName
	 *            name of transformation rule
	 * @param attributeIdentifiersAndExpressions
	 *            map of attributes and desired values of the resulting events
	 * @param attributeIdentifiersWithExternalKnowledge
	 *            map of attributes using external knowledge and their sets of
	 *            external knowledge expressions
	 * @param patternTree
	 *            pattern determining when a new event is created
	 * @return newly created transformation rule
	 */
	public TransformationRule createTransformationRule(final EapEventType selectedEventType,
			final String transformationRuleName, final TransformationPatternTree patternTree,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge) {
		return new TransformationRule(selectedEventType, transformationRuleName, patternTree,
				attributeIdentifiersAndExpressions, attributeIdentifiersWithExternalKnowledge);
	}

	public static void checkForValidity(final TransformationRule transformationRule) {
		final String esperQuery = transformationRule.getEsperQuery().toLowerCase();
		final String selectPart = transformationRule.getEsperQuery().substring(0, esperQuery.indexOf("from"));
		final String[] selectPartSplitted = selectPart.split(",");
		final ArrayList<String> attributesFromRule = new ArrayList<String>();
		final ArrayList<String> attributesOfTargetEventType = transformationRule.getEventType()
				.getAttributeExpressions();
		for (final String element : selectPartSplitted) {
			final String[] assignmentSplitted = element.split("\\s");
			attributesFromRule.add(assignmentSplitted[assignmentSplitted.length - 1]);
		}
		for (final String attribute : attributesFromRule) {
			if (!attributesOfTargetEventType.contains(attribute)) {
				throw new EPException("Error in transformation rule: The attribute '" + attribute
						+ "' does not exist in target event type '" + transformationRule.getEventType().getTypeName()
						+ "'.");
			}
		}

	}
}
