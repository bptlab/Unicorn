/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import java.util.HashSet;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.RPSTBuilder;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.query.PatternQuery;

/**
 * This class is intended as a controller to generate queries for Esper from a
 * BPMN process.
 * 
 * @author micha
 */
public class PatternQueryGenerator {

	private RPSTBuilder rpstTree;
	private EventTree<AbstractBPMNElement> processDecompositionTree;
	private Set<PatternQuery> queries = new HashSet<PatternQuery>();

	/**
	 * Constructor for the PatternQueryGenerator, which is intended as a
	 * controller to generate queries from a BPMN process.
	 * 
	 * @param rpstTree
	 */
	public PatternQueryGenerator(final RPSTBuilder rpstTree) {
		this.rpstTree = rpstTree;
		this.processDecompositionTree = this.rpstTree.getProcessDecompositionTree();
	}

	/**
	 * This method generates queries for the given BPMN process.
	 * 
	 * @throws QueryGenerationException
	 */
	public void generateQueries() throws QueryGenerationException {
		// StateTransitionQueries werden hier schon einmal erstellt, dass sie
		// zum Beispiel
		// für einen IntermediateTimer in der TimerQueryFactory, schon bekannt
		// sind und
		// berücksichtig werden können
		for (final AbstractBPMNElement treeElement : this.processDecompositionTree.getLeafElements()) {
			if (treeElement.hasMonitoringPointsWithEventType()) {
				final PatternQuery stateTransitionQuery = new StateTransitionQueryFactory(this).generateQuery(
						treeElement, null, null);
				this.queries.add(stateTransitionQuery);
			}
		}
		for (final AbstractBPMNElement rootElement : this.processDecompositionTree.getRootElements()) {
			if (rootElement instanceof Component) {
				new PatternQueryFactory(this).generateQuery(rootElement, null, null);
			} else {
				throw new RuntimeException("Queries can only be generated from components!");
			}
		}
	}

	public RPSTBuilder getRPSTTree() {
		return this.rpstTree;
	}

	public void setRpstTree(final RPSTBuilder rpstTree) {
		this.rpstTree = rpstTree;
	}

	public EventTree<AbstractBPMNElement> getProcessDecompositionTree() {
		return this.processDecompositionTree;
	}

	public void setProcessDecompositionTree(final EventTree<AbstractBPMNElement> processDecompositionTree) {
		this.processDecompositionTree = processDecompositionTree;
	}

	public Set<PatternQuery> getQueries() {
		return this.queries;
	}

	public void setQueries(final Set<PatternQuery> queries) {
		this.queries = queries;
	}

	public void addQuery(final PatternQuery query) {
		this.queries.add(query);
	}

	public void removeQuery(final PatternQuery query) {
		this.queries.remove(query);
	}

	/**
	 * This method searches for {@link PatternQuery}s, which contain only the
	 * given element.
	 * 
	 * @param element
	 * @return
	 */
	public PatternQuery getQueryForElement(final AbstractBPMNElement element) {
		for (final PatternQuery query : this.queries) {
			if (query.getMonitoredElements().contains(element) && query.getMonitoredElements().size() == 1) {
				return query;
			}
		}
		return null;
	}

}
