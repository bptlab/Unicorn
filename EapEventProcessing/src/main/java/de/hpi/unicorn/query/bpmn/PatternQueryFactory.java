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

/**
 * A factory to generate the pattern queries for a BPMN process to monitor and
 * analyze its execution.
 * 
 * @author micha
 */
public class PatternQueryFactory extends AbstractPatternQueryFactory {

	/**
	 * Constructor to create queries with a query factory. This query factory
	 * only delegates to a concrete query factory, which corresponds to the
	 * current elements pattern type.
	 * 
	 * @param patternQueryGenerator
	 */
	public PatternQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			PatternQuery query = null;

			// Falls alle indirekten Children keine Monitoringspoints haben,
			// braucht man auch keine Query
			// AggregationRule für zusammengesetzte Queries
			// if(hasIndirectChildrenMonitoringPoints(component)){
			if (component.hasMonitoringPointsWithEventType()) {
				switch (component.getType()) {
				case AND:
					query = new AndQueryFactory(this.patternQueryGenerator).generateQuery(component,
							catchingMonitorableElement, parentQuery);
					break;
				case LOOP:
					query = new LoopQueryFactory(this.patternQueryGenerator).generateQuery(component,
							catchingMonitorableElement, parentQuery);
					break;
				case SEQUENCE:
					query = new SequenceQueryFactory(this.patternQueryGenerator).generateQuery(component,
							catchingMonitorableElement, parentQuery);
					break;
				case XOR:
					query = new OrQueryFactory(this.patternQueryGenerator).generateQuery(component,
							catchingMonitorableElement, parentQuery);
					break;
				case SUBPROCESS:
					// TODO: Kann SubProcess noch ein externes Catching-Event
					// haben?
					query = new SubProcessQueryFactory(this.patternQueryGenerator).generateQuery(component,
							catchingMonitorableElement, parentQuery);
					break;
				default:
					throw new RuntimeException("No supported pattern!");
					// break;
				}
				// }
			}
			return query;
		} else {
			throw new QueryGenerationException("Input element should be a component for a LOOP-query!");
		}
	}

}
