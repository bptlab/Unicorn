/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.bpmn.decomposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Set;

import org.jbpt.algo.tree.rpst.IRPSTNode;
import org.jbpt.algo.tree.rpst.RPST;
import org.jbpt.algo.tree.tctree.TCTree;
import org.jbpt.algo.tree.tctree.TCTreeNode;
import org.jbpt.algo.tree.tctree.TCType;
import org.jbpt.graph.DirectedEdge;
import org.jbpt.graph.MultiDirectedGraph;
import org.jbpt.hypergraph.abs.Vertex;
import org.junit.Test;

import de.hpi.unicorn.bpmn.DirectedBPMNEdge;
import de.hpi.unicorn.bpmn.decomposition.RPSTBuilder;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.event.collection.EventTree;

/**
 * This class tests the decomposition of graphs with the RPST implementation and
 * serves as a first example.
 * 
 * @author micha
 */
public class RPSTTest extends AbstractDecompositionTest {

	private MultiDirectedGraph getGraph() {
		// System.out.println("SIMPLE SEQUENCE (3)");
		MultiDirectedGraph g = new MultiDirectedGraph();

		Vertex u = new Vertex("u");
		Vertex v = new Vertex("v");
		Vertex w = new Vertex("w");
		Vertex x = new Vertex("x");

		g.addEdge(u, v);
		g.addEdge(v, w);
		g.addEdge(w, x);

		return g;
	}

	@Test
	public void testSequenceRPST() {
		RPST<DirectedEdge, Vertex> rpst = new RPST<DirectedEdge, Vertex>(getGraph());

		for (IRPSTNode<DirectedEdge, Vertex> node : rpst.getRPSTNodes()) {
			System.out.print(node.getName() + ": ");
			for (IRPSTNode<DirectedEdge, Vertex> child : rpst.getPolygonChildren(node)) {
				System.out.print(child.getName() + " ");
			}
			// System.out.println();
		}

		// System.out.println("ROOT:" + rpst.getRoot());

		assertNotNull(rpst.getRoot());
		assertEquals(1, rpst.getRPSTNodes(TCType.POLYGON).size());
		assertEquals(3, rpst.getRPSTNodes(TCType.TRIVIAL).size());
		assertEquals(0, rpst.getRPSTNodes(TCType.RIGID).size());
		assertEquals(0, rpst.getRPSTNodes(TCType.BOND).size());
		assertEquals(TCType.POLYGON, rpst.getRoot().getType());

		// System.out.println("-----------------------------------------------------------------------");
	}

	@Test
	public void testComplexRPST() {
		RPST<DirectedBPMNEdge, AbstractBPMNElement> rpst = new RPST<DirectedBPMNEdge, AbstractBPMNElement>(
				new RPSTBuilder(process).getGraph());

		IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> rootNode = rpst.getRoot();
		assertNotNull(rootNode);

		Set<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> rpstNodes = rpst.getRPSTNodes();
		assertNotNull(rpstNodes);

		EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>> rpstNodesTree = new EventTree<IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement>>();
		assertNotNull(rpstNodesTree);

		for (IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> node : rpst.getRPSTNodes()) {
			if (node.getType() != TCType.TRIVIAL) {
				System.out.print(node.getType() + ": " + node.getName() + ": ");
				for (IRPSTNode<DirectedBPMNEdge, AbstractBPMNElement> child : rpst.getChildren(node)) {
					System.out.print(child.getName() + " | ");
				}
				// System.out.println();
			}
		}

	}

	@Test
	public void testComplexSPQR() {
		TCTree<DirectedBPMNEdge, AbstractBPMNElement> tcTree = new TCTree<DirectedBPMNEdge, AbstractBPMNElement>(
				new RPSTBuilder(process).getGraph());

		for (TCTreeNode<DirectedBPMNEdge, AbstractBPMNElement> node : tcTree.getTCTreeNodes()) {
			if (node.getType() != TCType.TRIVIAL) {
				System.out.print(node.getType() + ": " + node.getName() + ": ");
				for (TCTreeNode<DirectedBPMNEdge, AbstractBPMNElement> child : tcTree.getChildren(node)) {
					System.out.print(child.getName() + " | ");
				}
				// System.out.println();
			}
		}

	}

	@Test
	public void testTreeCreation() {
		RPSTBuilder rpst = new RPSTBuilder(process);
		assertNotNull(rpst);

		// System.out.println(rpst.getProcessDecompositionTree());
	}

}
