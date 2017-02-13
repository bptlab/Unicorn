/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.collection.TransformationTree;

public class MapTreeTest {

	private String rootElement1Key;
	private String rootElement1Child1Key;
	private String rootElement1Child1Child1Key;
	private String rootElement2Key;
	private String rootElement1Value;
	private String rootElement1Child1Value;
	private String rootElement1Child1Child1Value;
	private String rootElement2Value;

	@Before
	public void setup() {
		rootElement1Key = "RootElement1";
		rootElement1Child1Key = "RootElement1Child1";
		rootElement1Child1Child1Key = "RootElement1Child1Child1";
		rootElement2Key = "RootElement1";
		rootElement1Value = new String("Root Element 1");
		rootElement1Child1Value = new String("Root Element 1 Child 1");
		rootElement1Child1Child1Value = new String("Root Element 1 Child 1 Child 1");
		rootElement2Value = new String("Root Element 2");
	}

	@Test
	public void testTreeAdding() {
		TransformationTree<String, String> testMapTree = new TransformationTree<String, String>(rootElement1Key,
				rootElement1Value);
		assertFalse(testMapTree.isEmpty());
		assertTrue(testMapTree.getRootElementValues().size() == 1);
		assertTrue(testMapTree.getRootElementValues().get(0) == rootElement1Value);

		testMapTree.addRootElement(rootElement2Key, rootElement2Value);
		assertTrue(testMapTree.getRootElementValues().size() == 2);
		assertTrue(testMapTree.getRootElementValues().get(1) == rootElement2Value);

		testMapTree.addChild(rootElement1Key, rootElement1Child1Key, rootElement1Child1Value);
		assertTrue(testMapTree.getChildrenValues(rootElement1Key).size() == 1);
		assertTrue(testMapTree.getChildrenValues(rootElement1Key).get(0) == rootElement1Child1Value);

		testMapTree.addChild(rootElement1Child1Key, rootElement1Child1Child1Key, rootElement1Child1Child1Value);
		assertTrue(testMapTree.getChildrenValues(rootElement1Child1Key).size() == 1);
		assertTrue(testMapTree.getChildrenValues(rootElement1Child1Key).get(0) == rootElement1Child1Child1Value);
	}

	@Test
	public void testTreeRemoving() {
		TransformationTree<String, String> testMapTree = buildTestMap();
		assertTrue(testMapTree.size() == 4);

		testMapTree.remove(rootElement1Child1Child1Key);
		assertTrue(testMapTree.size() == 3);
		assertNull(testMapTree.findElement(rootElement1Child1Child1Key));

		testMapTree.remove(rootElement1Child1Key);
		assertTrue(testMapTree.size() == 2);
		assertNull(testMapTree.findElement(rootElement1Child1Key));
	}

	@Test
	public void testTreeChildRemoving() {
		TransformationTree<String, String> testMapTree = buildTestMap();
		assertTrue(testMapTree.size() == 4);

		testMapTree.remove(rootElement1Key);
		assertTrue(testMapTree.size() == 1);
		assertTrue(testMapTree.findElement(rootElement2Key) == rootElement2Value);
	}

	private TransformationTree<String, String> buildTestMap() {
		TransformationTree<String, String> testMapTree = new TransformationTree<String, String>(rootElement1Key,
				rootElement1Value);

		testMapTree.addRootElement(rootElement2Key, rootElement2Value);
		testMapTree.addChild(rootElement1Key, rootElement1Child1Key, rootElement1Child1Value);
		testMapTree.addChild(rootElement1Child1Key, rootElement1Child1Child1Key, rootElement1Child1Child1Value);

		return testMapTree;
	}

}
