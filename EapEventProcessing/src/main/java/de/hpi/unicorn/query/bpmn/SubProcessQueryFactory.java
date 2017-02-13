/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.query.bpmn;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.decomposition.Component;
import de.hpi.unicorn.bpmn.decomposition.SubProcessComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.query.PatternQuery;

/**
 * @author micha
 */
public class SubProcessQueryFactory extends AbstractPatternQueryFactory {

	public SubProcessQueryFactory(final PatternQueryGenerator patternQueryGenerator) {
		super(patternQueryGenerator);
	}

	@Override
	protected PatternQuery generateQuery(final AbstractBPMNElement element,
			final AbstractBPMNElement catchingMonitorableElement, final PatternQuery parentQuery)
			throws QueryGenerationException {
		if (element instanceof Component) {
			final Component component = (Component) element;
			PatternQuery query = null;
			if (component instanceof SubProcessComponent) {
				final SubProcessComponent subProcessComponent = (SubProcessComponent) component;

				final List<AbstractBPMNElement> catchingMonitorableElements = this
						.getCatchingMonitorableElement(subProcessComponent);

				AbstractBPMNElement subProcessCatchingMonitorableElement = null;
				if (!catchingMonitorableElements.isEmpty()) {
					subProcessCatchingMonitorableElement = catchingMonitorableElements.get(0);
				}

				// Sollte nur ein Element (Polygon-Component) sein, wenn
				// SubProcess wohlstrukturiert ist
				final List<AbstractBPMNElement> subProcessChildren = this.processDecompositionTree
						.getChildren(subProcessComponent);

				if (subProcessChildren.size() == 1 && subProcessChildren.get(0) instanceof Component) {
					query = new PatternQueryFactory(this.patternQueryGenerator).generateQuery(
							subProcessChildren.get(0), subProcessCatchingMonitorableElement, parentQuery);
				} else {
					throw new RuntimeException("SubProcess is not well structured.");
				}

			}

			return query;
		} else {
			throw new QueryGenerationException("Input element should be a component for an OR-query!");
		}
	}

	/**
	 * Tries to find the first monitorable element on a path after a subprocess
	 * starting with its catching intermediate event.
	 * 
	 * @param component
	 * @return
	 */
	private List<AbstractBPMNElement> getCatchingMonitorableElement(final SubProcessComponent component) {
		final Set<AbstractBPMNElement> successingMonitorableElements = new HashSet<AbstractBPMNElement>();
		final Set<AbstractBPMNElement> visitedElements = new HashSet<AbstractBPMNElement>();
		final AbstractBPMNElement attachedEvent = component.getSubProcess().getAttachedIntermediateEvent();
		if (attachedEvent != null) {
			this.traverseSuccessingMonitorableElements(attachedEvent, visitedElements, successingMonitorableElements);
		}
		return new ArrayList<AbstractBPMNElement>(successingMonitorableElements);
	}

}
