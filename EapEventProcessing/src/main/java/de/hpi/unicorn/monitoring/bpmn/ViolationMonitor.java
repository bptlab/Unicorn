/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.monitoring.bpmn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.PatternQueryType;

/**
 * The ViolationMonitor tries to reveal violations while the execution of
 * process instances. For instance order, exclusiveness or cooccurence
 * violations.
 * 
 * @author micha
 */
public class ViolationMonitor {

	private ProcessInstanceMonitor processInstanceMonitor;
	private final EventTree<AbstractBPMNElement> processDecompositionTree;

	/**
	 * Creates a new ViolationMonitor for the given
	 * {@link ProcessInstanceMonitor} to monitor execution violations.
	 * 
	 * @param processInstanceMonitor
	 */
	public ViolationMonitor(final ProcessInstanceMonitor processInstanceMonitor) {
		this.processInstanceMonitor = processInstanceMonitor;
		this.processDecompositionTree = this.processInstanceMonitor.getProcessInstance().getProcess()
				.getProcessDecompositionTree();
	}

	/**
	 * Searches for order, exclusiveness and cooccurence violations, during the
	 * execution of the specified process instance.
	 */
	public void searchForViolations() {
		this.searchForOrderViolations();
		this.searchForExclusivenessViolations();
		this.searchForOccurenceViolations();
		this.searchForLoopViolations();
	}

	/**
	 * This method searches for order violations in sequential components. So if
	 * the elements in the component are triggered in the false order, the
	 * sequential components has an order violation
	 */
	private void searchForOrderViolations() {
		// TODO: OrderViolation: Elemente in einer Sequenz, Reihenfolge der
		// Elemente ermitteln, falls Elemente alle getriggert, aber in falscher
		// Reihenfolge
		for (final QueryMonitor queryMonitor : this.processInstanceMonitor
				.getQueryMonitorsWithQueryType(PatternQueryType.SEQUENCE)) {
			if (queryMonitor.isRunning()) {
				List<QueryMonitor> subQueryMonitors = this.processInstanceMonitor.getSubQueryMonitors(queryMonitor);
				if (subQueryMonitors != null && !subQueryMonitors.contains(null) && subQueryMonitors.size() >= 1) {
					subQueryMonitors = this.orderQueryMonitorsSequential(queryMonitor);
					boolean allSubQueriesTerminated = true;
					boolean timeViolation = false;

					final int queryExecution = subQueryMonitors.get(0).getExecutionCount();
					Date queryEndTime = subQueryMonitors.get(0).getEndTime();

					for (final QueryMonitor subQueryMonitor : subQueryMonitors) {
						// TODO: Sollte Status terminate gefragt werden, also
						// auch skipped oder nur finished?
						// Alle terminated und gleiche Anzahl von Executions
						if (!subQueryMonitor.isTerminated() || subQueryMonitor.getExecutionCount() != queryExecution) {
							allSubQueriesTerminated = false;
							break;
						}
						if (queryEndTime.after(subQueryMonitor.getEndTime())) {
							timeViolation = true;
						}
						queryEndTime = subQueryMonitor.getEndTime();
					}
					// Alle Subqueries sind durchgelaufen
					if (allSubQueriesTerminated && timeViolation) {
						queryMonitor.addViolationStatus(ViolationStatus.Order);
						queryMonitor.setQueryStatus(QueryStatus.Finished);
						// TODO: sollte adaptQueries hier nochmal gerufen
						// werden?
					}
				}
			}
		}
	}

	/**
	 * Tries to order {@link QueryMonitor}s which belong to a sequence.
	 * 
	 * @param sequentialQueryMonitors
	 * @return
	 */
	private List<QueryMonitor> orderQueryMonitorsSequential(final QueryMonitor sequentialQueryMonitor) {
		final List<QueryMonitor> subQueryMonitors = this.processInstanceMonitor
				.getSubQueryMonitors(sequentialQueryMonitor);
		final List<AbstractBPMNElement> sequentialParents = this.processDecompositionTree
				.getParents(sequentialQueryMonitor.getQuery().getMonitoredElements());
		if (sequentialParents.size() == 1 && sequentialParents.get(0) instanceof Component) {
			final List<QueryMonitor> orderedQueryMonitors = new ArrayList<QueryMonitor>();
			final Component sequentialComponent = (Component) sequentialParents.get(0);
			// Hier sollte jeweils nur ein Element zurückkommen
			orderedQueryMonitors.addAll(this.processInstanceMonitor.getQueryMonitorsWithMonitoredElements(Arrays
					.asList(sequentialComponent.getSourceElement())));
			orderedQueryMonitors.addAll(this.processInstanceMonitor.getQueryMonitorsWithMonitoredElements(Arrays
					.asList(sequentialComponent.getSinkElement())));
			for (final QueryMonitor queryMonitor : subQueryMonitors) {
				if (!orderedQueryMonitors.contains(queryMonitor)) {
					QueryMonitor orderQueryMonitor;
					if (this.searchPredecessor(queryMonitor, orderedQueryMonitors) != null) {
						orderQueryMonitor = this.searchPredecessor(queryMonitor, orderedQueryMonitors);
						orderedQueryMonitors.add(orderedQueryMonitors.indexOf(orderQueryMonitor) + 1, queryMonitor);

					} else if (this.searchSuccessor(queryMonitor, orderedQueryMonitors) != null) {
						orderQueryMonitor = this.searchSuccessor(queryMonitor, orderedQueryMonitors);
						orderedQueryMonitors.add(orderedQueryMonitors.indexOf(orderQueryMonitor), queryMonitor);

					} else {
						orderedQueryMonitors.add(queryMonitor);
					}
				}
			}
			return orderedQueryMonitors;
		} else {
			System.err.println("Elements could not be ordered!");
			return subQueryMonitors;
		}
	}

	/**
	 * Searches for an QueryMonitor from the list of orderedQueryMonitors, that
	 * could be the predecessor of the specified QueryMonitor.
	 * 
	 * @param queryMonitor
	 * @param orderedQueryMonitors
	 * @return
	 */
	private QueryMonitor searchPredecessor(final QueryMonitor queryMonitor,
			final List<QueryMonitor> orderedQueryMonitors) {
		Set<AbstractBPMNElement> predecessors;
		for (final QueryMonitor orderedQueryMonitor : orderedQueryMonitors) {
			predecessors = new HashSet<AbstractBPMNElement>();
			if (queryMonitor.getQuery().getPatternQueryType().equals(PatternQueryType.STATETRANSITION)) {
				for (final AbstractBPMNElement monitoredElement : queryMonitor.getQuery().getMonitoredElements()) {
					predecessors.addAll(monitoredElement.getPredecessors());
				}
				// Wenn es keine StateTransition ist, beobacht die Query eine
				// Component, also bekommt man den Vorgänger als EntryPoint der
				// Component
			} else {
				for (final AbstractBPMNElement parent : this.processDecompositionTree.getParents(queryMonitor
						.getQuery().getMonitoredElements())) {
					if (parent instanceof Component) {
						final Component parentComponent = (Component) parent;
						predecessors.add(parentComponent.getEntryPoint());
					}
				}
			}
			predecessors.retainAll(orderedQueryMonitor.getQuery().getMonitoredElements());
			if (!predecessors.isEmpty()) {
				return orderedQueryMonitor;
			}
		}
		return null;
	}

	/**
	 * Searches for an QueryMonitor from the list of orderedQueryMonitors, that
	 * could be the successor of the specified QueryMonitor.
	 * 
	 * @param queryMonitor
	 * @param orderedQueryMonitors
	 * @return
	 */
	private QueryMonitor searchSuccessor(final QueryMonitor queryMonitor, final List<QueryMonitor> orderedQueryMonitors) {
		Set<AbstractBPMNElement> successors;
		for (final QueryMonitor orderedQueryMonitor : orderedQueryMonitors) {
			successors = new HashSet<AbstractBPMNElement>();
			if (queryMonitor.getQuery().getPatternQueryType().equals(PatternQueryType.STATETRANSITION)) {
				for (final AbstractBPMNElement monitoredElement : queryMonitor.getQuery().getMonitoredElements()) {
					successors.addAll(monitoredElement.getSuccessors());
				}
				// Wenn es keine StateTransition ist, beobacht die Query eine
				// Component, also bekommt man den Nachfolger als ExitPoint der
				// Component
			} else {
				for (final AbstractBPMNElement parent : this.processDecompositionTree.getParents(queryMonitor
						.getQuery().getMonitoredElements())) {
					if (parent instanceof Component) {
						final Component parentComponent = (Component) parent;
						successors.add(parentComponent.getExitPoint());
					}
				}
			}
			successors.retainAll(orderedQueryMonitor.getQuery().getMonitoredElements());
			if (!successors.isEmpty()) {
				return orderedQueryMonitor;
			}
		}
		return null;
	}

	/**
	 * The method searches for exclusiveness violations between several pathes
	 * in a XOR component. If a second execution path is monitored for a XOR
	 * component, it will be treated as a exclusiveness-violation.
	 */
	private void searchForExclusivenessViolations() {
		for (final QueryMonitor queryMonitor : this.processInstanceMonitor
				.getQueryMonitorsWithQueryType(PatternQueryType.XOR)) {
			final List<QueryMonitor> subQueryMonitors = this.processInstanceMonitor.getSubQueryMonitors(queryMonitor);
			if (!subQueryMonitors.contains(null) && !queryMonitor.isInLoop()) {
				int subQueriesFinished = 0;
				for (final QueryMonitor subQueryMonitor : subQueryMonitors) {
					if (subQueryMonitor.isFinished()) {
						subQueriesFinished++;
					}
				}
				if (subQueriesFinished > 1) {
					for (final QueryMonitor subQueryMonitor : subQueryMonitors) {
						subQueryMonitor.addViolationStatus(ViolationStatus.Exclusiveness);
					}
				}
			}
		}
	}

	/**
	 * This method searches for cooccurence-Violations. If a process instance
	 * reaches the last monitorable element and some elements are still running,
	 * these elements are missing and are marked with this violation status.
	 */
	private void searchForOccurenceViolations() {
		// Misssing kann sich nur auf StateTransitions beziehen?
		// Ohne Schleife: Falls Element noch Running ist --> Missing
		AbstractBPMNElement endEvent = null;
		for (final AbstractBPMNElement element : this.processDecompositionTree.getLeafElements()) {
			if (element instanceof BPMNEndEvent) {
				endEvent = element;
			}
		}
		if (endEvent != null && this.processDecompositionTree != null) {
			final AbstractBPMNElement lastMonitorableElement = this.getNearestMonitorablePredecessor(endEvent,
					this.processDecompositionTree.getLeafElements());
			final Set<QueryMonitor> lastElementQueryMonitors = this.processInstanceMonitor
					.getQueryMonitorsWithMonitoredElements(Arrays.asList(lastMonitorableElement));
			if (!lastElementQueryMonitors.isEmpty()) {
				final QueryMonitor lastElementQueryMonitor = lastElementQueryMonitors.iterator().next();
				if (lastElementQueryMonitor.isTerminated()) {
					// Ohne Schleife
					for (final QueryMonitor runningQueryMonitor : this.processInstanceMonitor
							.getQueryMonitorsWithStatus(QueryStatus.Started)) {
						// StateTransitions
						if (runningQueryMonitor.getQuery().getPatternQueryType()
								.equals(PatternQueryType.STATETRANSITION)) {
							runningQueryMonitor.addViolationStatus(ViolationStatus.Missing);
							runningQueryMonitor.setQueryStatus(QueryStatus.Aborted);
							this.abortParentQueries(runningQueryMonitor);
						}
					}
				}
			}
		}
	}

	/**
	 * Searches for cooccurence and exclusiveness violations in loop components.
	 * A more accurate distinction is not possible for the monitoring of loop
	 * components.
	 */
	private void searchForLoopViolations() {
		// LoopComponents untersuchen: Wenn ExecutionCount unterschiedlich in
		// einer LoopComponent, dann LoopViolation für LoopComponent
		// außer SubComponent ist wieder eine Loop
		for (final QueryMonitor queryMonitor : this.processInstanceMonitor
				.getQueryMonitorsWithQueryType(PatternQueryType.LOOP)) {
			final int loopExecutionCount = queryMonitor.getExecutionCount();
			final List<QueryMonitor> subQueryMonitors = this.processInstanceMonitor.getSubQueryMonitors(queryMonitor);
			if (!subQueryMonitors.contains(null)) {
				for (final QueryMonitor subQueryMonitor : subQueryMonitors) {
					if (!subQueryMonitor.getQuery().getPatternQueryType().equals(PatternQueryType.LOOP)) {
						if (subQueryMonitor.getExecutionCount() != loopExecutionCount) {
							queryMonitor.addViolationStatus(ViolationStatus.Loop);
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * Aborts recursevily all parent queries for the specified query, that are
	 * running.
	 * 
	 * @param queryMonitor
	 */
	private void abortParentQueries(final QueryMonitor queryMonitor) {
		final PatternQuery parentQuery = queryMonitor.getQuery().getParentQuery();
		if (parentQuery != null) {
			final QueryMonitor parentQueryMonitor = this.processInstanceMonitor.getQueryMonitorForQuery(parentQuery);
			if (parentQueryMonitor != null) {
				if (parentQueryMonitor.isRunning()) {
					parentQueryMonitor.setQueryStatus(QueryStatus.Aborted);
				}
				this.abortParentQueries(parentQueryMonitor);
			}
		}
	}

	private AbstractBPMNElement getNearestMonitorablePredecessor(final AbstractBPMNElement sourceElement,
			final Set<AbstractBPMNElement> considerableElements) {
		if (sourceElement.hasMonitoringPointsWithEventType() && !(sourceElement instanceof Component)) {
			return sourceElement;
		}
		// Map aufbauen mit möglichen Elementen und Abstand/Rekursionstiefe zum
		// Startelement
		final Map<AbstractBPMNElement, Integer> elements = new HashMap<AbstractBPMNElement, Integer>();
		final Set<AbstractBPMNElement> visitedElements = new HashSet<AbstractBPMNElement>();
		visitedElements.add(sourceElement);
		this.getMonitorablePredecessors(sourceElement.getPredecessors(), elements, considerableElements, 1,
				visitedElements);
		// Nähstes Element ermitteln
		int minDepth = Integer.MAX_VALUE;
		AbstractBPMNElement nearestElement = null;
		for (final AbstractBPMNElement element : elements.keySet()) {
			if (elements.get(element) < minDepth) {
				minDepth = elements.get(element);
				nearestElement = element;
			}
		}
		return nearestElement;
	}

	private void getMonitorablePredecessors(final Set<AbstractBPMNElement> predecessors,
			final Map<AbstractBPMNElement, Integer> elements, final Set<AbstractBPMNElement> considerableElements,
			int depth, final Set<AbstractBPMNElement> visitedElements) {
		for (final AbstractBPMNElement predecessor : predecessors) {
			if (considerableElements.contains(predecessor) && !visitedElements.contains(predecessor)) {
				visitedElements.add(predecessor);
				if (predecessor.hasMonitoringPointsWithEventType() && !(predecessor instanceof Component)) {
					elements.put(predecessor, depth);
				}
				this.getMonitorablePredecessors(predecessor.getPredecessors(), elements, considerableElements, ++depth,
						visitedElements);
			}
		}
	}

	public ProcessInstanceMonitor getProcessInstanceMonitor() {
		return this.processInstanceMonitor;
	}

	public void setProcessInstanceMonitor(final ProcessInstanceMonitor processInstanceMonitor) {
		this.processInstanceMonitor = processInstanceMonitor;
	}

}
