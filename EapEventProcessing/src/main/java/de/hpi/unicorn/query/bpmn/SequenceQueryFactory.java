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
 * This query factory creates queries for components with sequential elements.
 * 
 * @author micha
 */
public class SequenceQueryFactory extends AbstractPatternQueryFactory {

	/**
	 * Constructor to create sequential queries with a query factory.
	 * 
	 * @param patternQueryGenerator
	 */
	public SequenceQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			// Operator: ->
			final PatternQuery query = new PatternQuery(this.generateQueryName("Sequence"), null, QueryTypeEnum.LIVE,
					PatternQueryType.SEQUENCE, this.orderElements(component));

			final String queryString = this.generateQueryString(component, EsperPatternOperators.SEQUENCE,
					catchingMonitorableElement, query);
			query.setEsperQuery(queryString);
			this.addQueryRelationship(parentQuery, query);

			this.registerQuery(query);
			if (query != null && query.getListener() != null) {
				query.getListener().setCatchingElement(catchingMonitorableElement);
			}

			return query;
		} else {
			throw new QueryGenerationException("Input element should be a component for a sequential query!");
		}
	}

}
