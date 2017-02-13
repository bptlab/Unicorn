/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryListener;
import de.hpi.unicorn.query.PatternQueryType;
import de.hpi.unicorn.query.QueryTypeEnum;

/**
 * This factory generates queries for loop components. <br>
 * 
 * @author micha
 */
public class LoopQueryFactory extends AbstractPatternQueryFactory {

	/**
	 * Constructor to create loop queries with a query factory.
	 * 
	 * @param patternQueryGenerator
	 */
	public LoopQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			// Operator: UNTIL
			// Sequence-Polygone darin wie immer
			// Until-Bedingung: beobachtbares Element nach der
			// Loop-Bond-Component
			final List<AbstractBPMNElement> untilConditionElements = this.getSuccessingMonitorableElement(component);
			// QUERY bauen Elemente der Component (ein Polygon?) UNTIL
			// (conditionElement1 OR conditionElement2 OR ...)
			final PatternQuery query = new PatternQuery(this.generateQueryName("LOOP"), null, QueryTypeEnum.LIVE,
					PatternQueryType.LOOP, this.orderElements(component));

			final String queryString = this.generateLoopQueryString(component, untilConditionElements,
					catchingMonitorableElement, query);

			this.addQueryRelationship(parentQuery, query);
			query.setEsperQuery(queryString);

			this.registerQuery(query);
			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}

			final PatternQueryListener listener = query.getListener();

			final Set<EapEventType> loopBreakEventTypes = new HashSet<EapEventType>();
			for (final AbstractBPMNElement conditionElement : untilConditionElements) {
				loopBreakEventTypes.add(conditionElement.getMonitoringPoints().get(0).getEventType());
			}
			listener.setLoopBreakEventTypes(new ArrayList<EapEventType>(loopBreakEventTypes));

			return query;
			// Element nochmal zu Esper schicken?
		} else {
			throw new QueryGenerationException("Input element should be a component for a LOOP-query!");
		}
	}

	private String generateLoopQueryString(final Component component,
			final List<AbstractBPMNElement> untilConditionElements,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		// EVERY (([1]S0=SmallSequence) UNTIL (EVERY U0=SecondEvent))
		int elementsWithMonitoringPoints = 0;
		final StringBuilder sequencePatternQueryString = new StringBuilder();
		// TODO: Liefert auch noch Gateways am Rand, die nicht betrachtet werden
		// sollten
		final List<AbstractBPMNElement> orderedChildren = this.orderElements(component);
		for (final AbstractBPMNElement element : orderedChildren) {
			// Falls Element Component rekursiv tiefer aufrufen
			final StringBuilder queryPart = new StringBuilder();
			if (element instanceof Component) {
				final PatternQuery subQuery = new PatternQueryFactory(this.patternQueryGenerator).generateQuery(
						element, catchingMonitorableElement, parentQuery);
				queryPart.append("[1] S" + elementsWithMonitoringPoints + "=");
				queryPart.append(subQuery.getTitle());
			} else {
				continue;
			}
			if (elementsWithMonitoringPoints == 0) { // Erstes Element
				sequencePatternQueryString.append("SELECT * FROM PATTERN [(EVERY ((");
				sequencePatternQueryString.append(queryPart);
			} else {
				sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " " + queryPart);
			}
			elementsWithMonitoringPoints++;
		}
		sequencePatternQueryString.append(") " + EsperPatternOperators.LOOP.operator + " (");
		// Eines der UntilConditionElemente sollte als Abbruch ausgelöst werden
		int untilConditionElementsCounter = 0;
		for (final AbstractBPMNElement element : untilConditionElements) {
			final StringBuilder queryPart = new StringBuilder();

			queryPart.append("U" + untilConditionElementsCounter + "=");
			queryPart.append(element.getMonitoringPoints().get(0).getEventType().getTypeName());

			if (untilConditionElementsCounter == 0) { // Erstes Element
				sequencePatternQueryString.append(queryPart);
			} else {
				sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " " + queryPart);
			}
			untilConditionElementsCounter++;
		}
		if (catchingMonitorableElement == null) {
			sequencePatternQueryString.append(")))]");
		} else {
			sequencePatternQueryString.append(") " + EsperPatternOperators.XOR.operator + " EVERY C1=");
			sequencePatternQueryString.append(catchingMonitorableElement.getMonitoringPoints().get(0).getEventType()
					.getTypeName());
			sequencePatternQueryString.append("))]");
		}

		// gleiche ProcessInstanceID-Bedingung anhängen
		// TODO: Funktioniert noch nicht! Es kann nicht auf Pattern-Elemente vor
		// dem Until zugegriffen werden :(
		// sequencePatternQueryString.append(" WHERE de.hpi.unicorn.esper.EapUtils.isIntersectionNotEmpty({");
		// for(int j = 0; j < elementsWithMonitoringPoints; j++){
		// sequencePatternQueryString.append("S" + j + ".ProcessInstances,");
		// }
		// for(int j = 0; j < untilConditionElementsCounter; j++){
		// if(j != untilConditionElementsCounter - 1){ //letztes Element -->
		// kein Komma
		// sequencePatternQueryString.append("U" + j + ".ProcessInstances,");
		// } else {
		// sequencePatternQueryString.append("U" + j + ".ProcessInstances");
		// }
		// }
		// sequencePatternQueryString.append("})");

		return sequencePatternQueryString.toString();
	}

	private List<AbstractBPMNElement> getSuccessingMonitorableElement(final Component component) {
		final Set<AbstractBPMNElement> successingMonitorableElements = new HashSet<AbstractBPMNElement>();
		final Set<AbstractBPMNElement> visitedElements = new HashSet<AbstractBPMNElement>();
		this.traverseSuccessingMonitorableElements(component.getExitPoint(), visitedElements,
				successingMonitorableElements);
		return new ArrayList<AbstractBPMNElement>(successingMonitorableElements);
	}

}
