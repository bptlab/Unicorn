/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;
import de.hpi.unicorn.query.QueryTypeEnum;

/**
 * This query factory creates queries for components of type XOR.
 * 
 * @author micha
 */
public class OrQueryFactory extends AbstractPatternQueryFactory {

	/**
	 * Constructor to create XOR queries with a query factory.
	 * 
	 * @param patternQueryGenerator
	 */
	public OrQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			// TODO: Prüfen, ob auf allen Pfaden der Component Monitoring-Points
			// liegen, sonst kann keine richtige Abfrage erzeugt werden
			if (!this.allPathesContainMonitoringPoints(component)) {
				throw new QueryGenerationException("Query creation failed for: " + element
						+ " Reason: On all pathes of an exclusive component should be a monitorable element!");
			}
			// Component sollte mehrere Polygone beinhalten
			// Operator: OR
			final PatternQuery query = new PatternQuery(this.generateQueryName("XOR"), null, QueryTypeEnum.LIVE,
					PatternQueryType.XOR, this.orderElements(component));

			final String queryString = this.generateQueryString(component, EsperPatternOperators.XOR,
					catchingMonitorableElement, query);
			query.setEsperQuery(queryString);
			this.addQueryRelationship(parentQuery, query);

			this.registerQuery(query);
			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}

			return query;
		} else {
			throw new QueryGenerationException("Input element should be a component for an OR-query!");
		}
	}

	/**
	 * Proofs that at least one monitoring point is on all paths of the
	 * component.
	 * 
	 * @param component
	 * @return
	 */
	private boolean allPathesContainMonitoringPoints(final Component component) {
		boolean allChildsHaveMonitoringPoint = true;
		for (final AbstractBPMNElement child : this.patternQueryGenerator.getRPSTTree().getProcessDecompositionTree()
				.getChildren(component)) {
			if (!child.hasMonitoringPointsWithEventType()) {
				allChildsHaveMonitoringPoint = false;
			}
		}
		return allChildsHaveMonitoringPoint;
	}

}
