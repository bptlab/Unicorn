/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.persistence.Persistor;

public class TreePersistenceTest implements PersistenceTest {

	private EventTree<String> testTree;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleTree();
		assertTrue("Value should be 1, but was " + EventTree.findAll().size(), EventTree.findAll().size() == 1);
		EventTree<String> loadedTree = EventTree.findAll().get(0);
		assertTrue(loadedTree.getElements().size() == 4);
		EventTree.removeAll();
		assertTrue("Value should be 0, b" + "ut was " + EventTree.findAll().size(), EventTree.findAll().size() == 0);
	}

	@Test
	@Override
	public void testFind() {
		// TODO Auto-generated method stub

	}

	@Test
	@Override
	public void testRemove() {
		// TODO Auto-generated method stub

	}

	private void storeExampleTree() {
		String rootElement1 = new String("Root Element 1");
		String rootElement1Child1 = new String("Root Element 1 Child 1");
		String rootElement1Child1Child1 = new String("Root Element 1 Child 1 Child 1");
		String rootElement2 = new String("Root Element 2");

		testTree = new EventTree<String>(rootElement1);

		testTree.addRootElement(rootElement2);
		testTree.addChild(rootElement1, rootElement1Child1);
		testTree.addChild(rootElement1Child1, rootElement1Child1Child1);
		testTree.save();
	}

}
