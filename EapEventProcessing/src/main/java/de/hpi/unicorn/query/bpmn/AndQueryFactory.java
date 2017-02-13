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
 * This query factory creates queries for components of type AND.
 * 
 * @author micha
 */
public class AndQueryFactory extends AbstractPatternQueryFactory {

	/**
	 * Constructor to create AND queries with a query factory.
	 * 
	 * @param patternQueryGenerator
	 */
	public AndQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			// Component sollte mehrere Polygone beinhalten
			// Operator: AND
			final PatternQuery query = new PatternQuery(this.generateQueryName("And"), null, QueryTypeEnum.LIVE,
					PatternQueryType.AND, this.orderElements(component));

			final String queryString = this.generateQueryString(component, EsperPatternOperators.AND,
					catchingMonitorableElement, query);
			query.setEsperQuery(queryString);
			this.addQueryRelationship(parentQuery, query);

			// System.out.println(query.getTitle() + ": " +
			// query.getEsperQuery());

			this.registerQuery(query);
			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}

			return query;
		} else {
			throw new QueryGenerationException("Input element should be a component for an AND-query!");
		}
	}

}
