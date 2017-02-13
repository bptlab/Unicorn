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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.BondComponent;
import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.SubProcessComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.AttachableElement;
import de.hpi.unicorn.bpmn.element.BPMNBoundaryEvent;
import de.hpi.unicorn.bpmn.element.BPMNEventType;
import de.hpi.unicorn.bpmn.element.BPMNIntermediateEvent;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.query.PatternQuery;

/**
 * This class defines the abstract factory methods to generate the
 * {@link PatternQuery}s. The queries are created in the concrete subclasses.
 * 
 * @author micha
 */
public abstract class AbstractPatternQueryFactory {

	protected EventTree<AbstractBPMNElement> processDecompositionTree;
	protected PatternQueryGenerator patternQueryGenerator;
	private static List<String> queryNames = new ArrayList<String>();

	/**
	 * Constructor to create queries with a query factory.
	 * 
	 * @param patternQueryGenerator
	 */
	public AbstractPatternQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		this.patternQueryGenerator = patternQueryGenerator;
		this.processDecompositionTree = patternQueryGenerator.getProcessDecompositionTree();
	}

	/**
	 * This method generates a {@link PatternQuery} for the element. The type of
	 * the query is depending on the concrete PatternQueryFactory. If the
	 * element is a component, then the children of the element are used for the
	 * query. The catchingMonitorableElement can specify an attached element for
	 * this query. If the catchingMonitorableElement is monitored, the query
	 * finishes immediately. The parentQuery is the query, that contains this
	 * new created one.
	 * 
	 * @param element
	 * @param catchingMonitorableElement
	 * @param parentQuery
	 * @return
	 * @throws QueryGenerationException
	 */
	protected abstract PatternQuery generateQuery(AbstractBPMNElement element,
			AbstractBPMNElement catchingMonitorableElement, PatternQuery parentQuery) throws QueryGenerationException;

	/**
	 * Registers the query at Esper and for the {@link BPMNQueryMonitor}.
	 * 
	 * @param query
	 */
	protected void registerQuery(final PatternQuery query) {
		// add this temp event type as it is used to reference results of child
		// queries
		this.addPatternEventTypeToEsper(query.getTitle());

		BPMNQueryMonitor.getInstance().addQueryForProcess(query,
				CorrelationProcess.findByBPMNProcess(this.patternQueryGenerator.getRPSTTree().getProcess()));

		query.setListener(query.addToEsper(StreamProcessingAdapter.getInstance()));
	}

	/**
	 * This method is indicated to update a query that is already registered at
	 * Esper.
	 * 
	 * @param query
	 */
	protected void updateQuery(final PatternQuery query) {
		query.updateForEsper(StreamProcessingAdapter.getInstance());
	}

	/**
	 * @param name
	 * @return
	 */
	public EapEventType addPatternEventTypeToEsper(String name) {
		name = name.replaceAll(" ", "");
		final EapEventType patternEventType = new EapEventType(name, new AttributeTypeTree(), "Timestamp");
		// event type must be saved to reference the BPMN query later
		Broker.getEventAdministrator().importEventType(patternEventType);
		return patternEventType;
	}

	/**
	 * This method generates the actual query, that is needed for Esper and can
	 * be stored as the queryString in the {@link PatternQuery}. The concrete
	 * generated string depends on the patternOperator, which is used as an
	 * infix operator while query creation.
	 * 
	 * @param component
	 * @param patternOperator
	 * @param catchingMonitorableElement
	 * @param parentQuery
	 * @return
	 * @throws QueryGenerationException
	 */
	protected String generateQueryString(final Component component, final EsperPatternOperators patternOperator,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		int elementsWithMonitoringPoints = 0;
		final StringBuilder sequencePatternQueryString = new StringBuilder();
		// TODO: Liefert auch noch Gateways am Rand, die nicht betrachtet werden
		// sollten
		// TODO: Timer-Events
		final List<AbstractBPMNElement> orderedChildren = this.orderElements(component);
		for (final AbstractBPMNElement element : orderedChildren) {
			// Falls Element Component rekursiv tiefer aufrufen
			final StringBuilder queryPart = new StringBuilder();
			if (element instanceof Component && element.hasMonitoringPoints()
					&& element.hasMonitoringPointsWithEventType()) {
				final PatternQuery subQuery = new PatternQueryFactory(this.patternQueryGenerator).generateQuery(
						element, catchingMonitorableElement, parentQuery);
				this.addQueryRelationship(parentQuery, subQuery);

				queryPart.append("EVERY S" + elementsWithMonitoringPoints + "=");
				queryPart.append(subQuery.getTitle());
			}
			// Element hat Attached Timer
			else if (element instanceof AttachableElement
					&& element.hasMonitoringPointsWithEventType()
					&& ((AttachableElement) element).hasAttachedIntermediateEvent()
					&& ((AttachableElement) element).getAttachedIntermediateEvent().getIntermediateEventType() == BPMNEventType.Timer
					&& orderedChildren.contains(((AttachableElement) element).getAttachedIntermediateEvent())) {
				// Püfen, ob Boundary Event auch im Polygon ist, sonst wird der
				// Timer hier nicht abgefragt
				final AttachableElement attachableElement = (AttachableElement) element;
				final BPMNBoundaryEvent boundaryEvent = attachableElement.getAttachedIntermediateEvent();
				// Timer-Query bauen
				System.err.println("Timer Query");
				final PatternQuery subQuery = new TimerQueryFactory(this.patternQueryGenerator).generateQuery(element,
						catchingMonitorableElement, parentQuery);
				this.addQueryRelationship(parentQuery, subQuery);

				queryPart.append("EVERY S" + elementsWithMonitoringPoints + "=");
				queryPart.append(subQuery.getTitle());
				// Timer-Element und Boundary-Timer aus den orderedChildren
				// entfernen (ModifyException?)
				orderedChildren.removeAll(Arrays.asList(attachableElement, boundaryEvent));
			}
			// Element ist IntermediateTimer
			else if (element instanceof BPMNIntermediateEvent
					&& ((BPMNIntermediateEvent) element).getIntermediateEventType().equals(BPMNEventType.Timer)) {
				// Timer-Query bauen
				System.err.println("Timer Query");
				final PatternQuery subQuery = new TimerQueryFactory(this.patternQueryGenerator).generateQuery(element,
						catchingMonitorableElement, parentQuery);
				this.addQueryRelationship(parentQuery, subQuery);

				queryPart.append("EVERY S" + elementsWithMonitoringPoints + "=");
				queryPart.append(subQuery.getTitle());
			}
			// Normales Element: Activity Lifecycle Query
			else if (element.hasMonitoringPointsWithEventType()) {
				final PatternQuery subQuery = new StateTransitionQueryFactory(this.patternQueryGenerator)
						.generateQuery(element, catchingMonitorableElement, parentQuery);

				// System.out.println(subQuery.getTitle() + ": " +
				// subQuery.getEsperQuery());

				queryPart.append("EVERY S" + elementsWithMonitoringPoints + "=");
				queryPart.append(subQuery.getTitle());
			} else {
				continue;
			}
			if (elementsWithMonitoringPoints == 0) { // Erstes Element
				sequencePatternQueryString.append("SELECT * FROM PATTERN [(");
				sequencePatternQueryString.append(queryPart);
			} else {
				sequencePatternQueryString.append(" " + patternOperator.operator + " " + queryPart);
			}
			elementsWithMonitoringPoints++;
		}
		if (catchingMonitorableElement == null) {
			sequencePatternQueryString.append(")]");
		} else {
			sequencePatternQueryString.append(") " + EsperPatternOperators.XOR.operator + " EVERY C1=");
			sequencePatternQueryString.append(catchingMonitorableElement.getMonitoringPoints().get(0).getEventType()
					.getTypeName());
			sequencePatternQueryString.append("]");
		}

		if (patternOperator != EsperPatternOperators.XOR) {
			// gleiche ProcessInstanceID-Bedingung anhängen
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
		return sequencePatternQueryString.toString();
	}

	/**
	 * Orders the elements in a list in their sequential order in a process.
	 * Elements have to be consecutive.
	 * 
	 * @param component
	 * @return
	 */
	protected List<AbstractBPMNElement> orderElements(final Component component) {
		final List<AbstractBPMNElement> componentChildren = new ArrayList<AbstractBPMNElement>(
				this.processDecompositionTree.getChildren(component));
		final List<AbstractBPMNElement> orderedElements = new ArrayList<AbstractBPMNElement>();

		final int componentChildrenAmount = componentChildren.size();

		// Bonds brauchen nicht sortiert werden
		if (component instanceof BondComponent) {
			return this.processDecompositionTree.getChildren(component);
		}

		// Startelement einfügen
		final AbstractBPMNElement startElement = component.getSourceElement();
		if (startElement == null) {
			throw new RuntimeException("No source element for component!");
		}
		orderedElements.add(startElement);
		componentChildren.remove(startElement);

		for (int i = 0; i < componentChildrenAmount; i++) {
			final AbstractBPMNElement lastOrderedElement = orderedElements.get(orderedElements.size() - 1);
			// Set<AbstractBPMNElement> successors =
			// lastOrderedElement.getSuccessors();
			final Set<AbstractBPMNElement> successors = this.getSuccessorsWithComponents(lastOrderedElement,
					componentChildren);
			final List<AbstractBPMNElement> componentChildrenCopy = new ArrayList<AbstractBPMNElement>(
					componentChildren);
			componentChildrenCopy.retainAll(successors);
			if (componentChildrenCopy.size() > 0) {
				orderedElements.add(componentChildrenCopy.get(0));
			}
		}

		if (componentChildrenAmount != orderedElements.size()) {
			throw new RuntimeException("Elements could not be ordered!");
		}
		return orderedElements;
	}

	private Set<AbstractBPMNElement> getSuccessorsWithComponents(final AbstractBPMNElement predecessor,
			final List<AbstractBPMNElement> elements) {
		Set<AbstractBPMNElement> successors;
		if (predecessor instanceof Component) {
			final Component component = (Component) predecessor;
			if (predecessor instanceof SubProcessComponent) {
				final SubProcessComponent subProcessComponent = (SubProcessComponent) predecessor;
				successors = new HashSet<AbstractBPMNElement>(subProcessComponent.getSubProcess().getSuccessors());
			} else {
				successors = new HashSet<AbstractBPMNElement>(component.getSinkElement().getSuccessors());
				// Falls Component eine Schleife ist, könnten hier auch Elemente
				// aus der Component als Nachfolger auftauchen
				successors.removeAll(component.getChildren());
			}
			// Falls Component eine Schleife ist, könnten hier auch Elemente aus
			// der Component als Nachfolger auftauchen
			successors.removeAll(component.getChildren());

		} else {
			successors = new HashSet<AbstractBPMNElement>(predecessor.getSuccessors());
		}
		for (final AbstractBPMNElement element : elements) {
			if (element instanceof Component) {
				final Component component = (Component) element;
				if (successors.contains(component.getSourceElement())) {
					successors.add(element);
				}
			}
		}
		return successors;
	}

	/**
	 * @param element
	 * @param visitedElements
	 * @param successingMonitorableElements
	 */
	protected void traverseSuccessingMonitorableElements(final AbstractBPMNElement element,
			final Set<AbstractBPMNElement> visitedElements, final Set<AbstractBPMNElement> successingMonitorableElements) {
		if (!visitedElements.contains(element)) {
			visitedElements.add(element);
			if (element.hasMonitoringPoints()) {
				successingMonitorableElements.add(element);
			} else {
				for (final AbstractBPMNElement child : element.getSuccessors()) {
					this.traverseSuccessingMonitorableElements(child, visitedElements, successingMonitorableElements);
				}
			}
		}

	}

	/**
	 * Connects the two queries as parent and child.
	 * 
	 * @param parent
	 * @param child
	 */
	protected void addQueryRelationship(final PatternQuery parent, final PatternQuery child) {
		child.setParentQuery(parent);
		if (parent != null) {
			parent.addChildQueries(child);
		}
	}

	/**
	 * This method generates a unique name for each query. So that query names
	 * can be used as event types and do not intersect.
	 * 
	 * @param prefix
	 * @return
	 */
	protected String generateQueryName(final String prefix) {
		String queryName;
		do {
			queryName = prefix + new Date().getTime();
		} while (AbstractPatternQueryFactory.queryNames.contains(queryName));
		AbstractPatternQueryFactory.queryNames.add(queryName);
		return queryName;
	}

}
