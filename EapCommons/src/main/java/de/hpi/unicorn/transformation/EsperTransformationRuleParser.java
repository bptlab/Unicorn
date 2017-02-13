/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.transformation;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.FilterExpressionConnectorElement;
import de.hpi.unicorn.transformation.element.FilterExpressionConnectorEnum;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.FilterExpressionOperatorEnum;
import de.hpi.unicorn.transformation.element.PatternOperatorElement;
import de.hpi.unicorn.transformation.element.PatternOperatorEnum;
import de.hpi.unicorn.transformation.element.RangeElement;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpression;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

/**
 * Provides methods to parse queries in Esper EPL language from a transformation
 * rule.
 */
public class EsperTransformationRuleParser extends TransformationRuleParser {

	private static EsperTransformationRuleParser instance = null;

	public static EsperTransformationRuleParser getInstance() {
		if (EsperTransformationRuleParser.instance == null) {
			EsperTransformationRuleParser.instance = new EsperTransformationRuleParser();
		}
		return EsperTransformationRuleParser.instance;
	}

	/**
	 * Parses an Esper EPL query from the given parameters.
	 * 
	 * @param attributeIdentifiersAndExpressions
	 *            pairs of attribute identifiers and expressions - determines
	 *            what values are stored in the transformed events
	 * @param patternTree
	 *            pattern that is used to listen for events, built up from the
	 *            provided elements
	 * @return Esper EPL query
	 */
	@Override
	public String parseRule(final TransformationPatternTree patternTree,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets) {
		assert (patternTree.getRoots().size() == 1);

		final StringBuffer query = new StringBuffer();

		// SELECT part
		query.append("SELECT");

		final String valueSelection = this.buildValueSelectionString(attributeIdentifiersAndExpressions,
				attributeIdentifiersAndExpressionSets);
		query.append(valueSelection);

		// FROM PATTERN part

		final EventTreeElement<Serializable> rootElement = patternTree.getRoots().get(0);
		final String pattern = this.buildPatternString(rootElement);

		query.append(" FROM Pattern [" + pattern + "]");

		return query.toString();
	}

	@Override
	protected String buildPatternString(final EventTreeElement<Serializable> element) {
		if (element instanceof PatternOperatorElement) {
			final PatternOperatorElement poElement = ((PatternOperatorElement) element);
			final PatternOperatorEnum poType = (PatternOperatorEnum) poElement.getValue();
			if (poElement.getChildren().size() == 2) {
				final String leftHandSideExpression = this.buildPatternString(poElement.getChildren().get(0));
				final String rightHandSideExpression = this.buildPatternString(poElement.getChildren().get(1));
				if (poType == PatternOperatorEnum.UNTIL) {
					final RangeElement rangeElement = poElement.getRangeElement();
					final StringBuffer sb = new StringBuffer();
					sb.append("(");
					if (rangeElement.getLeftEndpoint() >= 0 || rangeElement.getRightEndpoint() >= 0) {
						sb.append("[");
						sb.append(rangeElement.getLeftEndpoint() < 0 ? "" : rangeElement.getLeftEndpoint());
						sb.append(":");
						sb.append(rangeElement.getRightEndpoint() < 0 ? "" : rangeElement.getRightEndpoint());
						sb.append("] ");
					}
					sb.append(leftHandSideExpression + " UNTIL " + rightHandSideExpression);
					sb.append(")");
					return sb.toString();
				} else if (poType == PatternOperatorEnum.AND) {
					return "(" + leftHandSideExpression + " AND " + rightHandSideExpression + ")";
				} else if (poType == PatternOperatorEnum.OR) {
					return "(" + leftHandSideExpression + " OR " + rightHandSideExpression + ")";
				} else if (poType == PatternOperatorEnum.FOLLOWED_BY) {
					return "(" + leftHandSideExpression + " -> " + rightHandSideExpression + ")";
				}
			} else if (poElement.getChildren().size() == 1) {
				final String expression = this.buildPatternString(poElement.getChildren().get(0));
				if (poType == PatternOperatorEnum.EVERY) {
					return "(EVERY " + expression + ")";
				} else if (poType == PatternOperatorEnum.EVERY_DISTINCT) {
					final StringBuffer sb = new StringBuffer();
					sb.append("(EVERY-DISTINCT(");
					final Iterator<String> iteratorForDistinctAttributes = poElement.getDistinctAttributes().iterator();
					while (iteratorForDistinctAttributes.hasNext()) {
						final String distinctAttribute = iteratorForDistinctAttributes.next();
						sb.append(distinctAttribute);
						if (iteratorForDistinctAttributes.hasNext()) {
							sb.append(", ");
						}
					}
					sb.append(") " + expression + ")");
					return sb.toString();
				} else if (poType == PatternOperatorEnum.REPEAT) {
					final RangeElement rangeElement = poElement.getRangeElement();
					return "([" + rangeElement.getLeftEndpoint() + "] " + expression + ")";
				} else if (poType == PatternOperatorEnum.NOT) {
					return "(NOT " + expression + ")";
				}
			}
		} else if (element instanceof EventTypeElement) {
			final EventTypeElement etElement = ((EventTypeElement) element);
			final StringBuffer sb = new StringBuffer();
			if (etElement.hasAlias()) {
				sb.append("(" + etElement.getAlias() + "=" + ((EapEventType) etElement.getValue()).getTypeName());
			} else {
				sb.append("(" + ((EapEventType) etElement.getValue()).getTypeName());
			}
			if (etElement.hasChildren()) {
				sb.append("(");
				final Iterator<EventTreeElement<Serializable>> iterator = element.getChildren().iterator();
				while (iterator.hasNext()) {
					final EventTreeElement<Serializable> currentElement = iterator.next();
					sb.append(this.buildPatternString(currentElement));
					if (iterator.hasNext()) {
						sb.append(", ");
					}
				}
				sb.append(")");
			}
			sb.append(")");
			return sb.toString();
		} else if (element instanceof FilterExpressionConnectorElement) {
			final FilterExpressionConnectorElement fecElement = (FilterExpressionConnectorElement) element;
			final FilterExpressionConnectorEnum fecType = (FilterExpressionConnectorEnum) fecElement.getValue();
			if (fecType == FilterExpressionConnectorEnum.AND) {
				final String leftHandSideExpression = this.buildPatternString(fecElement.getChildren().get(0));
				final String rightHandSideExpression = this.buildPatternString(fecElement.getChildren().get(1));
				return "(" + leftHandSideExpression + " AND " + rightHandSideExpression + ")";
			} else if (fecType == FilterExpressionConnectorEnum.OR) {
				final String leftHandSideExpression = this.buildPatternString(fecElement.getChildren().get(0));
				final String rightHandSideExpression = this.buildPatternString(fecElement.getChildren().get(1));
				return "(" + leftHandSideExpression + " OR " + rightHandSideExpression + ")";
			} else if (fecType == FilterExpressionConnectorEnum.NOT) {
				final String expression = this.buildPatternString(fecElement.getChildren().get(0));
				return "NOT (" + expression + ")";
			}
		} else if (element instanceof FilterExpressionElement) {
			final FilterExpressionElement feElement = (FilterExpressionElement) element;
			final FilterExpressionOperatorEnum feType = (FilterExpressionOperatorEnum) feElement.getValue();
			final StringBuffer sb = new StringBuffer();
			sb.append("(");
			sb.append("(" + feElement.getLeftHandSideExpression() + ") " + feType.getValue() + " ");
			if (feType == FilterExpressionOperatorEnum.IN || feType == FilterExpressionOperatorEnum.NOT_IN) {
				if (feElement.isRightHandSideRangeBased()) {
					final RangeElement rangeElement = feElement.getRightHandSideRangeOfValues();
					sb.append(rangeElement.isLeftEndpointOpen() ? "(" : "[");
					sb.append(rangeElement.getLeftEndpoint() + ":" + rangeElement.getRightEndpoint());
					sb.append(rangeElement.isRightEndpointOpen() ? ")" : "]");
				} else {
					sb.append("(");
					final Iterator<String> iterator = feElement.getRightHandSideListOfValues().iterator();
					while (iterator.hasNext()) {
						sb.append(iterator.next());
						if (iterator.hasNext()) {
							sb.append(", ");
						}
					}
					sb.append(")");
				}
			} else {
				sb.append("(" + feElement.getRightHandSideExpression() + ")");
			}
			sb.append(")");
			return sb.toString();
		}
		return "";
	}

	@Override
	protected String buildValueSelectionString(final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersAndExpressionSets) {
		Iterator<String> iteratorForAttributeIdentifiers;
		final StringBuffer sb = new StringBuffer();
		if (attributeIdentifiersAndExpressions != null) {
			iteratorForAttributeIdentifiers = attributeIdentifiersAndExpressions.keySet().iterator();
			while (iteratorForAttributeIdentifiers.hasNext()) {
				final String attributeIdentifier = iteratorForAttributeIdentifiers.next();
				if (attributeIdentifiersAndExpressions.get(attributeIdentifier) != null
						&& !attributeIdentifiersAndExpressions.get(attributeIdentifier).isEmpty()) {
					sb.append(" (" + attributeIdentifiersAndExpressions.get(attributeIdentifier) + ") AS "
							+ attributeIdentifier);
					if (iteratorForAttributeIdentifiers.hasNext()) {
						sb.append(",");
					}
				}
			}
		}
		if (attributeIdentifiersAndExpressionSets != null) {
			iteratorForAttributeIdentifiers = attributeIdentifiersAndExpressionSets.keySet().iterator();
			while (iteratorForAttributeIdentifiers.hasNext()) {
				final String attributeIdentifier = iteratorForAttributeIdentifiers.next();
				if (attributeIdentifiersAndExpressionSets.get(attributeIdentifier) != null) {
					if (sb.length() > 0) {
						sb.append(",");
					}
					sb.append(" ("
							+ this.buildValueSelectionStringFromExternalKnowledge(attributeIdentifiersAndExpressionSets
									.get(attributeIdentifier)) + ") AS " + attributeIdentifier);
				}
			}
		}
		return sb.toString();
	}

	@Override
	protected String buildValueSelectionStringFromExternalKnowledge(
			final ExternalKnowledgeExpressionSet externalKnowledge) {
		String externalKnowledgeFetchMethodName = null;
		switch (externalKnowledge.getResultingType()) {
		case DATE:
			externalKnowledgeFetchMethodName = "dateValueFromEvent";
			break;
		case INTEGER:
			externalKnowledgeFetchMethodName = "integerValueFromEvent";
			break;
		case FLOAT:
			externalKnowledgeFetchMethodName = "doubleValueFromEvent";
			break;
		default:
			externalKnowledgeFetchMethodName = "stringValueFromEvent";
		}
		final StringBuffer sb = new StringBuffer();
		sb.append("coalesce(");
		for (final ExternalKnowledgeExpression expression : externalKnowledge.getExternalKnowledgeExpressions()) {
			sb.append(externalKnowledgeFetchMethodName + "(");
			sb.append("'" + expression.getEventType().getTypeName() + "', ");
			sb.append("'" + expression.getDesiredAttribute().getAttributeExpression() + "', {");
			final Iterator<String> iterator = expression.getCriteriaAttributesAndValues().keySet().iterator();
			while (iterator.hasNext()) {
				final String criteriaAttributeExpression = iterator.next();
				sb.append("'" + criteriaAttributeExpression + "', "
						+ expression.getCriteriaAttributesAndValues().get(criteriaAttributeExpression));
				if (iterator.hasNext()) {
					sb.append(", ");
				}
			}
			sb.append("}), ");
		}
		sb.append(externalKnowledge.getDefaultValue());
		sb.append(")");
		return sb.toString();
	}
}
