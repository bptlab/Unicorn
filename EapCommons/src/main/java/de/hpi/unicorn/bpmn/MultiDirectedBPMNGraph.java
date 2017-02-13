/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn;

import org.jbpt.graph.abs.AbstractMultiDirectedGraph;

import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;

/**
 * This class is intented as a edge-oriented representation of a
 * {@link BPMNProcess}. It produces a directed multi-graph for the RPST
 * computation.
 * 
 * @author micha
 */
public class MultiDirectedBPMNGraph extends AbstractMultiDirectedGraph<DirectedBPMNEdge, AbstractBPMNElement> {

	@Override
	public DirectedBPMNEdge addEdge(final AbstractBPMNElement s, final AbstractBPMNElement t) {
		final DirectedBPMNEdge e = new DirectedBPMNEdge(this, s, t);
		return e;
	}

}
