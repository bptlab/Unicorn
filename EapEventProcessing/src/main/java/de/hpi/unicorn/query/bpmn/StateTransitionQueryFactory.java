/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import java.util.Arrays;
import java.util.List;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;
import de.hpi.unicorn.query.QueryTypeEnum;

/**
 * This query factory creates queries for concrete BPMN elements, which
 * represents the monitorable state transitions of the current element.
 * 
 * @author micha
 */
public class StateTransitionQueryFactory extends AbstractPatternQueryFactory {

	public StateTransitionQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	/**
	 * This method should generate a query for a single BPMN element, this query
	 * makes it possible to monitor the life cycle of a BPMN element. It is
	 * possible to observe the single state transitions of an activity when it
	 * is executed with multiple monitoring points.
	 * 
	 * @param element
	 * @param catchingMonitorableElement
	 * @param resendElement
	 * @return
	 */
	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery) {
		PatternQuery query = null;
		final String queryString = this.generateQueryString(element, catchingMonitorableElement);
		// Only update in case it already exists
		if (this.patternQueryGenerator.getQueryForElement(element) != null) {
			query = this.patternQueryGenerator.getQueryForElement(element);
			this.addQueryRelationship(parentQuery, query);

			this.updateQuery(query);

			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}
		} else {
			query = new PatternQuery(this.generateQueryName("StateTransition"), queryString, QueryTypeEnum.LIVE,
					PatternQueryType.STATETRANSITION, Arrays.asList(element));
			this.addQueryRelationship(parentQuery, query);

			this.registerQuery(query);

			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}
		}

		return query;
	}

	/**
	 * @param element
	 * @param catchingMonitorableElement
	 * @return
	 */
	private String generateQueryString(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement) {
		// not all monitoring points of an element must be present
		// In general Query-Cycle: (Enable->Begin->Terminate) OR Skip, Disrupt
		final MonitoringPoint initializeMonitoringPoint = element
				.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.initialize);
		final MonitoringPoint enableMonitoringPoint = element
				.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.enable);
		final MonitoringPoint beginMonitoringPoint = element
				.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.begin);
		final MonitoringPoint terminateMonitoringPoint = element
				.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.terminate);
		final MonitoringPoint skipMonitoringPoint = element
				.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.skip);
		element.getMonitoringPointByStateTransitionType(MonitoringPointStateTransition.disrupt);

		final List<MonitoringPoint> regularMonitoringPoints = Arrays.asList(initializeMonitoringPoint,
				enableMonitoringPoint, beginMonitoringPoint, terminateMonitoringPoint);

		int elementsWithMonitoringPoints = 0;
		final StringBuilder sequencePatternQueryString = new StringBuilder();
		sequencePatternQueryString.append("SELECT * FROM PATTERN [(");

		for (final MonitoringPoint monitoringPoint : regularMonitoringPoints) {
			if (monitoringPoint != null && monitoringPoint.getEventType() != null) {
				if (elementsWithMonitoringPoints > 0) {
					sequencePatternQueryString.append(" " + EsperPatternOperators.SEQUENCE.operator + " ");
				}
				sequencePatternQueryString.append("EVERY S" + elementsWithMonitoringPoints + "=");
				sequencePatternQueryString.append(monitoringPoint.getEventType().getTypeName());
				elementsWithMonitoringPoints++;
			}
		}

		sequencePatternQueryString.append(") ");

		final boolean elementHasSkipMonitoringPoint = skipMonitoringPoint != null
				&& skipMonitoringPoint.getEventType() != null;

		if (elementHasSkipMonitoringPoint) {
			if (elementsWithMonitoringPoints > 0) {
				sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " ");
			}
			sequencePatternQueryString.append("EVERY S" + elementsWithMonitoringPoints + "=");
			sequencePatternQueryString.append(skipMonitoringPoint.getEventType().getTypeName());
			elementsWithMonitoringPoints++;
		}

		if (catchingMonitorableElement == null) {
			sequencePatternQueryString.append("]");
		} else {
			sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " EVERY C1=");
			sequencePatternQueryString.append(catchingMonitorableElement.getMonitoringPoints().get(0).getEventType()
					.getTypeName());
			sequencePatternQueryString.append("]");
		}

		if (!elementHasSkipMonitoringPoint) {
			// gleiche ProcessInstanceID-Bedingung anhängen, falls
			// Skip-Monitoring-Point nicht hinzugefügt,
			// bei OR-Operator in Pattern ist die Bedingung nicht möglich
			sequencePatternQueryString.append(" WHERE de.hpi.unicorn.esper.EapUtils.isIntersectionNotEmpty({");
			for (int j = 0; j < elementsWithMonitoringPoints; j++) {
				if (j == elementsWithMonitoringPoints - 1) { // letztes Element
																// --> kein
																// Komma
					sequencePatternQueryString.append("S" + j + ".ProcessInstances");
				} else {
					sequencePatternQueryString.append("S" + j + ".ProcessInstances,");
				}
			}
			sequencePatternQueryString.append("})");
		}

		final String queryString = sequencePatternQueryString.toString();
		return queryString;
	}

}
