/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn;

import org.jbpt.graph.abs.AbstractDirectedEdge;
import org.jbpt.graph.abs.AbstractMultiDirectedGraph;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;

/**
 * Edge between two {@link AbstractBPMNElement}s in a
 * {@link MultiDirectedBPMNGraph}.
 * 
 * @author micha
 */
public class DirectedBPMNEdge extends AbstractDirectedEdge<AbstractBPMNElement> {

	protected DirectedBPMNEdge(final AbstractMultiDirectedGraph<?, AbstractBPMNElement> g,
			final AbstractBPMNElement source, final AbstractBPMNElement target) {
		super(g, source, target);
	}

}