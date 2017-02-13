/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator.model;

import java.util.ArrayList;
import java.util.List;

import de.hpi.unicorn.bpmn.decomposition.ANDComponent;
import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.LoopComponent;
import de.hpi.unicorn.bpmn.decomposition.SequenceComponent;
import de.hpi.unicorn.bpmn.decomposition.XORComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNEndEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.utils.Tuple;

/**
 * This class converts a tree from the simple simulation page to a BPMN model
 * for the simulator.
 * 
 * @author micha
 */
public class SimulationTreeTableToModelConverter {

	EventTree<Object> tree;
	BPMNProcess process = new BPMNProcess();
	EventTree<AbstractBPMNElement> bpmnTree = new EventTree<AbstractBPMNElement>();
	int IDCounter = 0;

	public BPMNProcess convertTreeToModel(final EventTree<Object> tree) {
		this.tree = tree;
		final BPMNStartEvent startEvent = new BPMNStartEvent(this.createID(), "Start", null);
		this.process.addBPMNElement(startEvent);
		final BPMNEndEvent endEvent = new BPMNEndEvent(this.createID(), "End", null);
		this.process.addBPMNElement(endEvent);
		this.convertTreeToBPMNTree();
		// Annahme, dass Tree immer mit einer Component (Sequence, XOR, AND oder
		// LOOP) beginnt
		final Tuple<AbstractBPMNElement, AbstractBPMNElement> subStartAndEnd = this.createSubBranch(this.bpmnTree
				.getRootElements().get(0));
		AbstractBPMNElement.connectElements(startEvent, subStartAndEnd.x);
		AbstractBPMNElement.connectElements(subStartAndEnd.y, endEvent);
		return this.process;
	}

	/**
	 * Tries to create the whole subbrach recursively starting with the given
	 * branchStartElement.
	 * 
	 * @param branchStartElement
	 * @return a list with the start and end element of the created branch
	 */
	private Tuple<AbstractBPMNElement, AbstractBPMNElement> createSubBranch(final AbstractBPMNElement branchStartElement) {
		if (branchStartElement instanceof Component) {
			if (branchStartElement instanceof SequenceComponent) {
				return this.createSequenceSubBranch((SequenceComponent) branchStartElement);
			} else if (branchStartElement instanceof ANDComponent) {
				return this.createAndSubBranch((ANDComponent) branchStartElement);
			} else if (branchStartElement instanceof XORComponent) {
				return this.createXORSubBranch((XORComponent) branchStartElement);
			} else if (branchStartElement instanceof LoopComponent) {
				return this.createLoopSubBranch((LoopComponent) branchStartElement);
			}
		}
		final BPMNTask task = (BPMNTask) branchStartElement;
		this.process.addBPMNElement(task);
		return (new Tuple<AbstractBPMNElement, AbstractBPMNElement>(task, task));
	}

	private Tuple<AbstractBPMNElement, AbstractBPMNElement> createSequenceSubBranch(
			final SequenceComponent branchStartElement) {
		final List<AbstractBPMNElement> children = this.bpmnTree.getChildren(branchStartElement);
		AbstractBPMNElement firstElement = null;
		AbstractBPMNElement predecessor = null;
		for (int i = 0; i < children.size(); i++) {
			final Tuple<AbstractBPMNElement, AbstractBPMNElement> subStartAndEnd = this
					.createSubBranch(children.get(i));
			if (i == 0) { // erstes Element
				firstElement = (subStartAndEnd.x);
			} else { // Elemente dazwischen
				AbstractBPMNElement.connectElements(predecessor, subStartAndEnd.x);
			}
			predecessor = subStartAndEnd.y;
		}
		// letztes Element der letzten Subkomponente ist gleichzeitig letztes
		// Element der Sequenz
		return new Tuple<AbstractBPMNElement, AbstractBPMNElement>(firstElement, predecessor);
	}

	private Tuple<AbstractBPMNElement, AbstractBPMNElement> createAndSubBranch(final ANDComponent branchStartElement) {
		String ID = this.createID();
		final BPMNAndGateway startGateway = new BPMNAndGateway(ID, "AND" + ID, null);
		this.process.addBPMNElement(startGateway);
		ID = this.createID();
		final BPMNAndGateway endGateway = new BPMNAndGateway(ID, "AND" + ID, null);
		this.process.addBPMNElement(endGateway);
		// alle Unterelemente erstellen und mit Gateways verbinden
		for (final AbstractBPMNElement child : this.bpmnTree.getChildren(branchStartElement)) {
			final Tuple<AbstractBPMNElement, AbstractBPMNElement> subStartAndEnd = this.createSubBranch(child);
			AbstractBPMNElement.connectElements(startGateway, subStartAndEnd.x);
			AbstractBPMNElement.connectElements(subStartAndEnd.y, endGateway);
		}
		return new Tuple<AbstractBPMNElement, AbstractBPMNElement>(startGateway, endGateway);
	}

	private Tuple<AbstractBPMNElement, AbstractBPMNElement> createXORSubBranch(final XORComponent branchStartElement) {
		String ID = this.createID();
		final BPMNXORGateway startGateway = new BPMNXORGateway(ID, "XOR" + ID, null);
		this.process.addBPMNElement(startGateway);
		ID = this.createID();
		final BPMNXORGateway endGateway = new BPMNXORGateway(ID, "XOR" + ID, null);
		this.process.addBPMNElement(endGateway);
		for (final AbstractBPMNElement child : this.bpmnTree.getChildren(branchStartElement)) {
			final Tuple<AbstractBPMNElement, AbstractBPMNElement> subStartAndEnd = this.createSubBranch(child);
			AbstractBPMNElement.connectElements(startGateway, subStartAndEnd.x);
			AbstractBPMNElement.connectElements(subStartAndEnd.y, endGateway);
		}
		return new Tuple<AbstractBPMNElement, AbstractBPMNElement>(startGateway, endGateway);
	}

	private Tuple<AbstractBPMNElement, AbstractBPMNElement> createLoopSubBranch(final LoopComponent branchStartElement) {
		final List<AbstractBPMNElement> children = this.bpmnTree.getChildren(branchStartElement);
		String ID = this.createID();
		final BPMNXORGateway startGateway = new BPMNXORGateway(ID, "XOR" + ID, null);
		this.process.addBPMNElement(startGateway);
		ID = this.createID();
		final BPMNXORGateway endGateway = new BPMNXORGateway(ID, "XOR" + ID, null);
		this.process.addBPMNElement(endGateway);
		// Schleife einbauen
		AbstractBPMNElement.connectElements(endGateway, startGateway);
		AbstractBPMNElement predecessor = startGateway;
		for (int i = 0; i < children.size(); i++) {
			final Tuple<AbstractBPMNElement, AbstractBPMNElement> subStartAndEnd = this
					.createSubBranch(children.get(i));
			AbstractBPMNElement.connectElements(predecessor, subStartAndEnd.x);
			if (i == children.size() - 1) { // letztes Element
				AbstractBPMNElement.connectElements(subStartAndEnd.y, endGateway);
			}
			predecessor = subStartAndEnd.y;
		}
		return new Tuple<AbstractBPMNElement, AbstractBPMNElement>(startGateway, endGateway);
	}

	/**
	 * Converts the tree of eventTypes and IPattern objects to BPMNTasks and
	 * gateway components.
	 */
	private void convertTreeToBPMNTree() {
		for (final Object treeElement : this.tree.getRootElements()) {
			this.convertAndAddElementToBPMNTree(treeElement, null);
		}
	}

	private void convertAndAddElementToBPMNTree(final Object treeElement, final AbstractBPMNElement bpmnParent) {
		if (treeElement instanceof EapEventType) {
			final String ID = Integer.toString(++this.IDCounter);
			final MonitoringPoint monitoringPoint = new MonitoringPoint((EapEventType) treeElement,
					MonitoringPointStateTransition.terminate, "");
			final ArrayList<MonitoringPoint> monitoringPoints = new ArrayList<MonitoringPoint>();
			monitoringPoints.add(monitoringPoint);
			final BPMNTask task = new BPMNTask(ID, "Task " + ID, monitoringPoints);
			this.bpmnTree.addChild(bpmnParent, task);
		} else if (treeElement instanceof Component) {
			if (this.tree.hasChildren(treeElement)) {
				AbstractBPMNElement bpmnElement = null;
				if (treeElement instanceof ANDComponent) {
					bpmnElement = (ANDComponent) treeElement;
				} else if (treeElement instanceof XORComponent) {
					bpmnElement = (XORComponent) treeElement;
				} else if (treeElement instanceof LoopComponent) {
					bpmnElement = (LoopComponent) treeElement;
				} else if (treeElement instanceof SequenceComponent) {
					bpmnElement = (SequenceComponent) treeElement;
				}
				this.bpmnTree.addChild(bpmnParent, bpmnElement);
				for (final Object child : this.tree.getChildren(treeElement)) {
					this.convertAndAddElementToBPMNTree(child, bpmnElement);
				}
			}
		}
	}

	private String createID() {
		this.IDCounter++;
		return Integer.toString(this.IDCounter);
	}

}
