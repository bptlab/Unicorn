/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.algo.tree.tctree.TCType;

import de.hpi.unicorn.bpmn.DirectedBPMNEdge;
import de.hpi.unicorn.bpmn.MultiDirectedBPMNGraph;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNSubProcess;
import de.hpi.unicorn.event.collection.EventTree;

/**
 * This class constructs a RPST and a {@link EventTree} with the
 * {@link AbstractBPMNElement}s of the process derived from the RPST.
 * 
 * @author micha
 * 
 */
public class RPSTBuilder {

	private BPMNProcess process;

	/**
	 * Graph derived from the BPMNProcess.
	 */
	private final MultiDirectedBPMNGraph graph;

	/**
	 * The RPST tree.
	 */
	private final RPST<DirectedBPMNEdge, AbstractBPMNElement> rpst;

	/**
	 * Tree of the RPST nodes. RPST nodes are edges of the original process.
	 */
	private EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> rpstNodesTree;

	/**
	 * Tree, which contains the BPMNProcess elements in the decomposition
	 * hierarchy of the RPST.
	 */
	private EventTree<AbstractBPMNElement> processDecompositionTree;

	/**
	 * @param process
	 */
	public RPSTBuilder(final BPMNProcess process) {
		this.process = (BPMNProcess) process.clone();
		this.process = BPMNProcessPreprocessor.structureProcess(this.process);
		this.graph = this.convertBPMNToGraph(this.process);
		this.rpst = new RPST<DirectedBPMNEdge, AbstractBPMNElement>(this.graph);
		this.buildRPSTNodesTree();
		this.buildProcessDecompositionTree();
		PatternUtil.determinePatternForTreeComponents(this.processDecompositionTree);
	}

	/**
	 * Builds a {@link EventTree} from the components of the RPST for better
	 * handling.
	 */
	private void buildRPSTNodesTree() {
		final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rootNode = this.rpst.getRoot();

		this.rpstNodesTree = new EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>>();

		this.addElementToRPSTNodesTree(rootNode, null);
	}

	/**
	 * Determines if a node in the RPST has children.
	 * 
	 * @param node
	 * @param rpst
	 * @return
	 */
	private boolean hasChildren(final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> node,
			final RPST<DirectedBPMNEdge, AbstractBPMNElement> rpst) {
		return !rpst.getChildren(node).isEmpty();
	}

	private void addElementToRPSTNodesTree(final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> element,
			final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> parent) {
		this.rpstNodesTree.addChild(parent, element);
		if (this.hasChildren(element, this.rpst)) {
			for (final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> child : this.rpst.getChildren(element)) {
				this.addElementToRPSTNodesTree(child, element);
			}
		}
	}

	/**
	 * This method converts an node-oriented {@link BPMNProcess} to an
	 * edge-oriented {@link MultiDirectedBPMNGraph}.
	 * 
	 * @param process
	 * @return
	 */
	private MultiDirectedBPMNGraph convertBPMNToGraph(final BPMNProcess process) {
		final MultiDirectedBPMNGraph g = new MultiDirectedBPMNGraph();
		for (final AbstractBPMNElement element : process.getBPMNElementsWithOutSequenceFlows()) {
			for (final AbstractBPMNElement successor : element.getSuccessors()) {
				g.addEdge(element, successor);
			}
		}
		return g;
	}

	/**
	 * Builds the processDecompositionTree from the RPST.
	 */
	private void buildProcessDecompositionTree() {
		this.processDecompositionTree = new EventTree<AbstractBPMNElement>();
		this.addElementsToProcessDecompositionTree(this.rpstNodesTree, this.rpstNodesTree.getRootElements(), null, null);
		// Children für Components im ProcessDecompositionTree setzen
		for (final AbstractBPMNElement rootElement : this.processDecompositionTree.getRootElements()) {
			this.setChildrenForProcessDecompositionTreeElements(rootElement);
		}

		this.determineEntryAndExitPoints();
	}

	private void setChildrenForProcessDecompositionTreeElements(final AbstractBPMNElement element) {
		if (element instanceof Component && this.processDecompositionTree.hasChildren(element)) {
			final Component component = (Component) element;
			component.addChildren(this.processDecompositionTree.getChildren(component));
			for (final AbstractBPMNElement child : this.processDecompositionTree.getChildren(component)) {
				this.setChildrenForProcessDecompositionTreeElements(child);
			}
		}
	}

	/**
	 * Sets the entry and exit point for every element of the tree, that is a
	 * component.
	 */
	private void determineEntryAndExitPoints() {
		for (final AbstractBPMNElement element : this.processDecompositionTree.getElements()) {
			if (element instanceof Component) {
				final Component component = (Component) element;

				component.setEntryPoint(this.determineEntryPoint(component));
				component.setExitPoint(this.determineExitPoint(component));
			}
		}
	}

	private AbstractBPMNElement determineEntryPoint(final Component component) {
		final AbstractBPMNElement sourceElement = component.getSourceElement();
		// TODO: Testen: Kann noch falsch sein bei Schleifen
		AbstractBPMNElement entryPoint = null;
		if (!sourceElement.getPredecessors().isEmpty()) {
			// Direkte Vorgänger des SourceElement
			final Set<AbstractBPMNElement> predecessors = new HashSet<AbstractBPMNElement>(
					sourceElement.getPredecessors());

			// Alle Childs des aktuellen Parent
			final Set<AbstractBPMNElement> parentIndirectChildElements = this.processDecompositionTree
					.getIndirectChildren(this.processDecompositionTree.getParent(component));

			// Alle Childs der aktuellen Component
			final Set<AbstractBPMNElement> componentChildren = this.processDecompositionTree
					.getIndirectChildren(component);

			// Sinnvolle Entrypoints sind Vorgänger der Component, die nicht
			// Kinder der Component sind
			parentIndirectChildElements.removeAll(componentChildren);
			predecessors.retainAll(parentIndirectChildElements);

			if (!predecessors.isEmpty()) {
				entryPoint = predecessors.iterator().next();
			}

		}
		return entryPoint;
	}

	private AbstractBPMNElement determineExitPoint(final Component component) {
		// TODO: Testen: Kann noch falsch sein bei Schleifen
		final AbstractBPMNElement sinkElement = component.getSinkElement();
		AbstractBPMNElement exitPoint = null;
		if (!sinkElement.getSuccessors().isEmpty()) {
			// Direkte Nachfolger des SinkElements
			final Set<AbstractBPMNElement> successors = new HashSet<AbstractBPMNElement>(sinkElement.getSuccessors());

			// Alle Childs des aktuellen Parent
			final Set<AbstractBPMNElement> parentIndirectChildElements = this.processDecompositionTree
					.getIndirectChildren(this.processDecompositionTree.getParent(component));

			// Alle Childs der aktuellen Component
			final Set<AbstractBPMNElement> componentChildren = this.processDecompositionTree
					.getIndirectChildren(component);

			// Sinnvolle Exitpoints sind Nachfolger der Component, die nicht
			// Kinder der Component sind
			parentIndirectChildElements.removeAll(componentChildren);
			successors.retainAll(parentIndirectChildElements);

			if (!successors.isEmpty()) {
				exitPoint = successors.iterator().next();
			}
		}
		return exitPoint;
	}

	/**
	 * Adds the elements of the RPSTNodesTree to the ProcessDecompositionTree
	 * with the specified parent elements.
	 * 
	 * @param rpstNodesTree
	 * @param rpstNodesTreeElements
	 * @param rpstNodesTreeParentElement
	 * @param processDecompositionTreeParent
	 */
	private void addElementsToProcessDecompositionTree(
			final EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> rpstNodesTree,
			final Collection<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> rpstNodesTreeElements,
			final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeParentElement,
			final AbstractBPMNElement processDecompositionTreeParent) {
		final Set<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> trivialElements = new HashSet<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>>();

		for (final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeElement : rpstNodesTreeElements) {
			if (rpstNodesTreeElement.getType() == TCType.TRIVIAL) {
				trivialElements.add(rpstNodesTreeElement);
				continue;
			}

			AbstractBPMNElement createdElement = null;

			final AbstractBPMNElement sourceElement = rpstNodesTreeElement.getEntry();

			final AbstractBPMNElement sinkElement = rpstNodesTreeElement.getExit();

			if (rpstNodesTreeElement.getType() == TCType.POLYGON) {
				createdElement = new PolygonComponent(null, sourceElement, null, sinkElement);
				createdElement.setName(rpstNodesTreeElement.getName());
			} else if (rpstNodesTreeElement.getType() == TCType.BOND) {
				createdElement = new BondComponent(null, sourceElement, null, sinkElement);
				createdElement.setName(rpstNodesTreeElement.getName());
			}
			// TODO: consider TCType.RIGID
			// else if(rpstNodesTreeElement.getType() == TCType.RIGID){
			// throw new
			// RuntimeException("RIGIDs sind noch nicht durchdacht ;)");
			// }

			this.processDecompositionTree.addChild(processDecompositionTreeParent, createdElement);

			if (rpstNodesTree.hasChildren(rpstNodesTreeElement)) {
				this.addElementsToProcessDecompositionTree(rpstNodesTree,
						rpstNodesTree.getChildren(rpstNodesTreeElement), rpstNodesTreeElement, createdElement);
			}
		}

		if (!trivialElements.isEmpty()) {
			this.addTrivialElementsToProcessDecompositionTree(trivialElements, rpstNodesTreeParentElement,
					processDecompositionTreeParent);
		}
	}

	private void addTrivialElementsToProcessDecompositionTree(
			final Set<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> trivialElements,
			final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeParentElement,
			final AbstractBPMNElement processDecompositionTreeParent) {
		final Map<AbstractBPMNElement, Integer> elementsMap = new HashMap<AbstractBPMNElement, Integer>();
		elementsMap.put(rpstNodesTreeParentElement.getEntry(), 1);
		elementsMap.put(rpstNodesTreeParentElement.getExit(), 1);
		for (final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeElement : trivialElements) {
			for (final DirectedBPMNEdge directedBPMNEdge : rpstNodesTreeElement.getFragment()) {
				for (final AbstractBPMNElement element : directedBPMNEdge.getVertices()) {
					if (!elementsMap.containsKey(element)) {
						elementsMap.put(element, 1);
					} else {
						elementsMap.put(element, elementsMap.get(element) + 1);
					}
				}
			}
		}
		for (final AbstractBPMNElement element : elementsMap.keySet()) {
			if (elementsMap.get(element) > 1) {
				if (!(element instanceof BPMNSubProcess)) {
					this.processDecompositionTree.addChild(processDecompositionTreeParent, element);
				} else {
					// TODO: in eigene Methode auslagern
					// SubProcess zerlegt in den RPST
					final BPMNSubProcess subProcess = (BPMNSubProcess) element;
					final RPSTBuilder subProcessRPST = new RPSTBuilder(subProcess);

					// Sollte möglich sein, da im PreProcessingStep beim RPST
					// bauen alle Start- und Endevents vereinigt werden
					final AbstractBPMNElement sourceElement = subProcessRPST.getProcess().getStartEvent();
					final AbstractBPMNElement sinkElement = subProcessRPST.getProcess().getEndEvent();

					AbstractBPMNElement entryPoint = this.getPredecessorFromEdges(subProcess, trivialElements);
					if (entryPoint == null) { /*
											 * dann ist Element erstes Element
											 * der Component und hat nur einen
											 * Vorgänger
											 */
						if (subProcess.getPredecessors().size() > 1) {
							throw new RuntimeException("Fehler in der Logik!");
						} else {
							entryPoint = (!subProcess.getPredecessors().isEmpty()) ? subProcess.getPredecessors()
									.iterator().next() : null;
						}
					}
					AbstractBPMNElement exitPoint = this.getSuccessorFromEdges(subProcess, trivialElements);
					if (exitPoint == null) { /*
											 * dann ist Element letztes Element
											 * der Component und hat nur einen
											 * Nachfolger
											 */
						if (subProcess.getSuccessors().size() > 1) {
							throw new RuntimeException("Fehler in der Logik!");
						} else {
							exitPoint = (!subProcess.getSuccessors().isEmpty()) ? subProcess.getSuccessors().iterator()
									.next() : null;
						}
					}
					// TODO: Vielleicht keine eigene SubProcess-Component,
					// sondern eher Component-Eigenschaft SubProcess hinzufügen?
					final SubProcessComponent subProcessComponent = new SubProcessComponent(entryPoint, sourceElement,
							exitPoint, sinkElement);
					subProcessComponent.setSubProcess(subProcess);
					// RPST des SubProcess unter der SubProcessComponent
					// einhängen
					if (processDecompositionTreeParent instanceof Component) {
						final Component parentComponent = (Component) processDecompositionTreeParent;
						if (parentComponent.getSourceElement().equals(subProcess)) {
							parentComponent.setSourceElement(subProcessComponent);
						} else if (parentComponent.getSinkElement().equals(subProcess)) {
							parentComponent.setSinkElement(subProcessComponent);
						}
					}
					this.processDecompositionTree.addChild(processDecompositionTreeParent, subProcessComponent);
					this.addElementsToProcessDecompositionTree(subProcessRPST.getRPSTNodesTree(), subProcessRPST
							.getRPSTNodesTree().getRootElements(), null, subProcessComponent);
				}

			}
		}
	}

	private AbstractBPMNElement getPredecessorFromEdges(final AbstractBPMNElement element,
			final Set<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> trivialElements) {
		for (final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeElement : trivialElements) {
			for (final DirectedBPMNEdge directedBPMNEdge : rpstNodesTreeElement.getFragment()) {
				if (directedBPMNEdge.getTarget().equals(element)) {
					return directedBPMNEdge.getSource();
				}
			}
		}
		return null;
	}

	private AbstractBPMNElement getSuccessorFromEdges(final AbstractBPMNElement element,
			final Set<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> trivialElements) {
		for (final IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rpstNodesTreeElement : trivialElements) {
			for (final DirectedBPMNEdge directedBPMNEdge : rpstNodesTreeElement.getFragment()) {
				if (directedBPMNEdge.getSource().equals(element)) {
					return directedBPMNEdge.getTarget();
				}
			}
		}
		return null;
	}

	public BPMNProcess getProcess() {
		return this.process;
	}

	public MultiDirectedBPMNGraph getGraph() {
		return this.graph;
	}

	public RPST<DirectedBPMNEdge, AbstractBPMNElement> getRpst() {
		return this.rpst;
	}

	public EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> getRPSTNodesTree() {
		return this.rpstNodesTree;
	}

	public EventTree<AbstractBPMNElement> getProcessDecompositionTree() {
		return this.processDecompositionTree;
	}

}
