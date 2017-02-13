/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNAndGateway;
import de.hpi.unicorn.bpmn.element.BPMNEventBasedGateway;
import de.hpi.unicorn.bpmn.element.BPMNEventBasedGatewayType;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.event.collection.EventTree;

/**
 * This class searches for bond components in the given tree and tries to assign
 * {@link IPattern} as the {@link BondComponent#setType(IPattern)}.
 * 
 * @author micha
 * 
 */
public class PatternUtil {

	/**
	 * Searches in the given tree for bond components and try to assign
	 * {@link IPattern} to the components.
	 * 
	 * @param processDecompositionTree
	 */
	public static void determinePatternForTreeComponents(final EventTree<AbstractBPMNElement> processDecompositionTree) {
		for (final AbstractBPMNElement element : processDecompositionTree.getElements()) {
			if (element instanceof BondComponent) {
				final BondComponent bond = (BondComponent) element;
				final List<AbstractBPMNElement> bondChildren = processDecompositionTree.getChildren(bond);
				PatternUtil.determinePatternForComponent(bond, bondChildren, processDecompositionTree);
			} else if (element instanceof PolygonComponent) {
				((PolygonComponent) element).setType(IPattern.SEQUENCE);
			} else if (element instanceof SubProcessComponent) {
				((SubProcessComponent) element).setType(IPattern.SUBPROCESS);
			}
		}
	}

	private static void determinePatternForComponent(final BondComponent bond,
			final List<AbstractBPMNElement> bondChildren, final EventTree<AbstractBPMNElement> processDecompositionTree) {
		final AbstractBPMNElement sourceElement = bond.getSourceElement();
		final AbstractBPMNElement sinkElement = bond.getSinkElement();

		// Detection of different patterns
		if ((sourceElement instanceof BPMNAndGateway || (sourceElement instanceof BPMNEventBasedGateway && ((BPMNEventBasedGateway) sourceElement)
				.getType().equals(BPMNEventBasedGatewayType.Parallel))) && sourceElement.getPredecessors().size() == 1) {
			bond.setType(IPattern.AND);
		} else if ((sourceElement instanceof BPMNXORGateway || (sourceElement instanceof BPMNEventBasedGateway && ((BPMNEventBasedGateway) sourceElement)
				.getType().equals(BPMNEventBasedGatewayType.Exclusive)))
				&& sourceElement.getPredecessors().size() == 1
				|| sinkElement instanceof BPMNXORGateway && sinkElement.getSuccessors().size() == 1) {
			bond.setType(IPattern.XOR);
		} else if (sourceElement instanceof BPMNXORGateway && PatternUtil.isCyclic(bond, processDecompositionTree)) {
			bond.setType(IPattern.LOOP);
		}
	}

	private static boolean isCyclic(final BondComponent bond,
			final EventTree<AbstractBPMNElement> processDecompositionTree) {
		final Set<AbstractBPMNElement> bondChildren = processDecompositionTree.getLeafs(bond);
		final AbstractBPMNElement sourceElement = bond.getSourceElement();
		final Set<AbstractBPMNElement> visitedElements = new HashSet<AbstractBPMNElement>();
		PatternUtil.visitSuccessors(sourceElement, bondChildren, visitedElements);
		return visitedElements.contains(sourceElement);
	}

	/**
	 * Adds all indirect successors of the startElement to visitedElements, if
	 * the element is contained in elements.
	 * 
	 * @param startElement
	 * @param elements
	 * @param visitedElements
	 */
	private static void visitSuccessors(final AbstractBPMNElement startElement,
			final Set<AbstractBPMNElement> elements, final Set<AbstractBPMNElement> visitedElements) {
		if (elements.contains(startElement) && !visitedElements.contains(startElement)) {
			visitedElements.add(startElement);
			for (final AbstractBPMNElement successor : startElement.getSuccessors()) {
				PatternUtil.visitSuccessors(successor, elements, visitedElements);
			}
		}
	}

}
