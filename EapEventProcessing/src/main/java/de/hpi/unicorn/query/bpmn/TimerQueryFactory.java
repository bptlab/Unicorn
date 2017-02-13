/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AttachableElement;
import de.hpi.unicorn.bpmn.element.BPMNEventType;
import de.hpi.unicorn.bpmn.element.BPMNIntermediateEvent;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;
import de.hpi.unicorn.query.QueryTypeEnum;

/**
 * This class creates Esper-Queries to observe BPMN timer-events, like attached
 * timer events or intermediate timer events.
 * 
 * @author micha
 */
public class TimerQueryFactory extends AbstractPatternQueryFactory {

	public TimerQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	/**
	 * This method should generate a query for a single BPMN element that has an
	 * attached timer, this query makes it possible to fire an event after a
	 * specified time interval.
	 * 
	 * @param element
	 * @param catchingMonitorableElement
	 * @return
	 * @throws QueryGenerationException
	 */
	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof AttachableElement) {
			return this.generateAttachedTimerQuery(element, catchingMonitorableElement, parentQuery);
		} else if (element instanceof BPMNIntermediateEvent) {
			return this.generateInterMediateTimerQuery(element, catchingMonitorableElement, parentQuery);
		} else {
			throw new QueryGenerationException("Input element should be a attachable element for an TIMER-query!");
		}
	}

	private PatternQuery generateAttachedTimerQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery) {
		final StringBuilder sequencePatternQueryString = new StringBuilder();
		sequencePatternQueryString.append("SELECT * FROM PATTERN [(");

		final PatternQuery query = new PatternQuery(this.generateQueryName("Timer"), null, QueryTypeEnum.LIVE,
				PatternQueryType.TIMER, Arrays.asList(element));
		this.addQueryRelationship(parentQuery, query);
		// TODO: Hier die Timer-Query
		final PatternQuery subQuery = new StateTransitionQueryFactory(this.patternQueryGenerator).generateQuery(
				element, catchingMonitorableElement, query);
		sequencePatternQueryString.append("EVERY S0=" + subQuery.getTitle());

		final String timerEventTypeName = element.getMonitoringPoints().get(0).getEventType().getTypeName();
		final EapEventType boundaryTimerEventType = this.addPatternEventTypeToEsper(timerEventTypeName
				+ "BoundaryTimer");

		sequencePatternQueryString.append(" " + EsperPatternOperators.SEQUENCE.operator + " EVERY S1="
				+ boundaryTimerEventType.getTypeName());

		sequencePatternQueryString.append(") ");

		if (catchingMonitorableElement == null) {
			sequencePatternQueryString.append("]");
		} else {
			sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " EVERY C1=");
			sequencePatternQueryString.append(catchingMonitorableElement.getMonitoringPoints().get(0).getEventType()
					.getTypeName());
			sequencePatternQueryString.append("]");
		}

		sequencePatternQueryString
				.append(" WHERE de.hpi.unicorn.esper.EapUtils.isIntersectionNotEmpty({S0.ProcessInstances, S1.ProcessInstances})");

		final String queryString = sequencePatternQueryString.toString();
		query.setEsperQuery(queryString);
		// System.out.println(query.getTitle() + ": " + queryString);

		this.registerQuery(query);
		if (query != null && query.getListener() != null) {
			query.getListener().setCatchingElement(catchingMonitorableElement);
		}

		// Wenn, die SubQuery (StateTransition der Aktivität) gefeuert hat, soll
		// der Timer ausgelöst werden
		final EapEventType attachableElementEventType = element.getMonitoringPoints().get(0).getEventType();
		subQuery.getListener().setTimer(attachableElementEventType.getTypeName(), boundaryTimerEventType,
				((AttachableElement) element).getAttachedIntermediateEvent().getTimeDuration());

		return query;
	}

	private PatternQuery generateInterMediateTimerQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery) {
		final BPMNIntermediateEvent intermediateEvent = (BPMNIntermediateEvent) element;
		PatternQuery timerQuery = null;
		if (intermediateEvent.getIntermediateEventType().equals(BPMNEventType.Timer)) {
			final StringBuilder sequencePatternQueryString = new StringBuilder();

			// SELECT * FROM PATTERN [((EVERY VOR1 OR VOR2 ... ) -> Timer)]
			timerQuery = new PatternQuery(this.generateQueryName("Timer"), null, QueryTypeEnum.LIVE,
					PatternQueryType.TIMER, Arrays.asList(element));
			this.addQueryRelationship(parentQuery, timerQuery);

			// Nach Vorgängern mit MonitoringPoints suchen
			final Set<AbstractBPMNElement> monitorablePredecessors = new HashSet<AbstractBPMNElement>();
			final Set<AbstractBPMNElement> visitedpredecessors = new HashSet<AbstractBPMNElement>();
			this.getNearestMonitorablePredecessors(intermediateEvent, monitorablePredecessors, visitedpredecessors);

			final List<PatternQuery> predecessorQueries = new ArrayList<PatternQuery>(
					this.findStateTransitionQueriesForElements(monitorablePredecessors));

			if (!predecessorQueries.isEmpty()) {

				sequencePatternQueryString.append("SELECT * FROM PATTERN [(");

				// Für alle Vorgänger eine OR-Query erzeugen, sodass jeder der
				// Vorgänger den Timer triggern kann
				final PatternQuery predecessorOrQuery = this.createPredecessorORQuery(predecessorQueries);

				sequencePatternQueryString.append("EVERY S1=" + predecessorOrQuery.getTitle() + " ");

				final EapEventType interMediateTimerEventType = this.addPatternEventTypeToEsper(intermediateEvent
						.getName() + "Timer");
				sequencePatternQueryString.append(EsperPatternOperators.SEQUENCE.operator + " EVERY S2="
						+ interMediateTimerEventType.getTypeName() + ")");

				if (catchingMonitorableElement == null) {
					sequencePatternQueryString.append("]");
				} else {
					sequencePatternQueryString.append(" " + EsperPatternOperators.XOR.operator + " EVERY C1=");
					sequencePatternQueryString.append(catchingMonitorableElement.getMonitoringPoints().get(0)
							.getEventType().getTypeName());
					sequencePatternQueryString.append("]");
				}

				// Gleiche Prozessinstanzbedingung anhängen
				sequencePatternQueryString
						.append(" WHERE de.hpi.unicorn.esper.EapUtils.isIntersectionNotEmpty({S1.ProcessInstances, S2.ProcessInstances})");

				final String queryString = sequencePatternQueryString.toString();
				timerQuery.setEsperQuery(queryString);

				this.registerQuery(timerQuery);
				if (timerQuery != null && timerQuery.getListener() != null) {
					timerQuery.getListener().setCatchingElement(catchingMonitorableElement);
				}

				// Wenn die beobachtbaren Vorgängerqueries (StateTransition der
				// Elemente) gefeuert haben, soll der Timer ausgelöst werden
				for (final PatternQuery predecessorQuery : predecessorQueries) {
					final AbstractBPMNElement timerTrigger = predecessorQuery.getMonitoredElements().get(0);
					final EapEventType timmerTriggerEventType = timerTrigger.getMonitoringPoints().get(0)
							.getEventType();
					predecessorQuery.getListener().setTimer(timmerTriggerEventType.getTypeName(),
							interMediateTimerEventType, intermediateEvent.getTimeDuration());
				}
			}
		}
		return timerQuery;
	}

	private PatternQuery createPredecessorORQuery(final List<PatternQuery> predecessorQueries) {
		final PatternQuery predecessorOrQuery = new PatternQuery(this.generateQueryName("TimerPredecessor"), null,
				QueryTypeEnum.LIVE, PatternQueryType.XOR, new ArrayList<AbstractBPMNElement>());
		final StringBuilder sequencePatternQueryString = new StringBuilder();
		for (int i = 0; i < predecessorQueries.size(); i++) {
			final PatternQuery predecessorQuery = predecessorQueries.get(i);
			sequencePatternQueryString.append("SELECT * FROM PATTERN [(");
			if (i < predecessorQueries.size() - 1) {
				sequencePatternQueryString.append("EVERY S" + i + "=" + predecessorQuery.getTitle()
						+ EsperPatternOperators.XOR.operator);
			} else { // Letztes Element, daher kein OR-Operator mehr
				sequencePatternQueryString.append("EVERY S" + i + "=" + predecessorQuery.getTitle());
			}
		}
		sequencePatternQueryString.append(")]");

		predecessorOrQuery.setEsperQuery(sequencePatternQueryString.toString());

		this.addPatternEventTypeToEsper(predecessorOrQuery.getTitle());
		predecessorOrQuery.setListener(predecessorOrQuery.addToEsper(StreamProcessingAdapter.getInstance()));

		return predecessorOrQuery;
	}

	private Set<PatternQuery> findStateTransitionQueriesForElements(final Set<AbstractBPMNElement> elements) {
		final Set<PatternQuery> matchingQueries = new HashSet<PatternQuery>();
		for (final AbstractBPMNElement element : elements) {
			final PatternQuery query = this.patternQueryGenerator.getQueryForElement(element);
			if (query != null) {
				matchingQueries.add(query);
			}
		}
		return matchingQueries;
	}

	/**
	 * Searches for all monitorable predecessors of the specified element and
	 * adds them to monitorablePredecessors, but only the nearest on every path.
	 * 
	 * @param element
	 * @param monitorablePredecessors
	 * @param visitedpredecessors
	 */
	private void getNearestMonitorablePredecessors(final AbstractBPMNElement element,
			final Set<AbstractBPMNElement> monitorablePredecessors, final Set<AbstractBPMNElement> visitedpredecessors) {
		if (!visitedpredecessors.contains(element)) {
			visitedpredecessors.add(element);
			for (final AbstractBPMNElement predecessor : element.getPredecessors()) {
				if (predecessor.hasMonitoringPointsWithEventType()) {
					monitorablePredecessors.add(predecessor);
				} else {
					this.getNearestMonitorablePredecessors(predecessor, monitorablePredecessors, visitedpredecessors);
				}
			}
		}

	}

}
