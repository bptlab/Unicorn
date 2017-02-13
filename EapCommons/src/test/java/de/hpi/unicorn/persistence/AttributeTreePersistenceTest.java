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

import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class tests the saving, finding and removing of
 * {@link AttributeTypeTree}.
 */
public class AttributeTreePersistenceTest implements PersistenceTest {

	private AttributeTypeTree testTree;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleTree();
		assertTrue("Value should be 1, but was " + AttributeTypeTree.findAll().size(), AttributeTypeTree.findAll()
				.size() == 1);
		AttributeTypeTree loadedTree = AttributeTypeTree.findAll().get(0);
		assertTrue(loadedTree.getAttributes().size() == 4);
		AttributeTypeTree.removeAll();
		assertTrue("Value should be 0, but was " + AttributeTypeTree.findAll().size(), AttributeTypeTree.findAll()
				.size() == 0);
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
		TypeTreeNode rootElement1 = new TypeTreeNode("Root Element 1");
		TypeTreeNode rootElement1Child1 = new TypeTreeNode(rootElement1, "Root Element 1 Child 1",
				AttributeTypeEnum.INTEGER);
		new TypeTreeNode(rootElement1Child1, "Root Element 1 Child 1 Child 1", AttributeTypeEnum.DATE);
		TypeTreeNode rootElement2 = new TypeTreeNode("Root Element 2", AttributeTypeEnum.STRING);

		testTree = new AttributeTypeTree();
		testTree.addRoot(rootElement1);
		testTree.addRoot(rootElement2);
		testTree.save();
		// System.out.println(testTree.toString());
	}

}