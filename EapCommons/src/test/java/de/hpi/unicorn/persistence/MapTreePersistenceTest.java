/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.persistence;

import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.collection.EventTransformationElement;
import de.hpi.unicorn.event.collection.TransformationTree;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class tests the saving, finding and removing of
 * {@link TransformationTree}.
 */
public class MapTreePersistenceTest implements PersistenceTest {

	private TransformationTree<String, String> testMapTree;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
	}

	@Test
	public void testMapElementStoreAndRetrieve() {
		EventTransformationElement<String, String> mapElement = new EventTransformationElement<String, String>("Key",
				"Value");
		mapElement.save();
		assertTrue("Value should be 1, but was " + EventTransformationElement.findAll().size(),
				EventTransformationElement.findAll().size() == 1);
		EventTransformationElement.removeAll();
		assertTrue("Value should be 0, but was " + EventTransformationElement.findAll().size(),
				EventTransformationElement.findAll().size() == 0);
	}

	@Test
	@Override
	public void testStoreAndRetrieve() {
		storeExampleMapTree();
		assertTrue("Value should be 1, but was " + TransformationTree.findAll().size(), TransformationTree.findAll()
				.size() == 1);
		TransformationTree<String, String> loadedMapTree = TransformationTree.findAll().get(0);
		assertTrue(loadedMapTree.keySet().size() == 4);
		TransformationTree.removeAll();
		assertTrue("Value should be 0, but was " + TransformationTree.findAll().size(), TransformationTree.findAll()
				.size() == 0);
	}

	@Test
	public void testSaveDifferentKindsOfSushiMaptree() {
		// it should be possible to save different types of sushimaptrees

		// TransformationTree with String Values
		storeExampleMapTree();
		List<TransformationTree> allSushiMapTrees = TransformationTree.findAll();
		assertTrue("Expected " + String.class + " but got " + allSushiMapTrees.get(0).get("RootElement1").getClass(),
				allSushiMapTrees.get(0).get("RootElement1").getClass() == String.class);

		// TransformationTree with Integer Values
		String rootElement1Key = "RootElement1";
		String rootElement1Child1Key = "RootElement1Child1";
		String rootElement1Child1Child1Key = "RootElement1Child1Child1";
		String rootElement2Key = "RootElement2";
		int rootElement1Value = 1;
		int rootElement1Child1Value = 2;
		int rootElement1Child1Child1Value = 3;
		int rootElement2Value = 4;

		TransformationTree<String, Integer> testMapTree2 = new TransformationTree<String, Integer>(rootElement1Key,
				rootElement1Value);
		testMapTree2.addRootElement(rootElement2Key, rootElement2Value);
		testMapTree2.addChild(rootElement1Key, rootElement1Child1Key, rootElement1Child1Value);
		testMapTree2.addChild(rootElement1Child1Key, rootElement1Child1Child1Key, rootElement1Child1Child1Value);
		testMapTree2.save();
		allSushiMapTrees = TransformationTree.findAll();
		assertTrue("Expected " + Integer.class + " but got " + allSushiMapTrees.get(1).get("RootElement1").getClass(),
				allSushiMapTrees.get(1).get("RootElement1").getClass() == Integer.class);

		// TransformationTree with Date Values
		String rootElement1Key2 = "RootElement1";
		String rootElement1Child1Key2 = "RootElement1Child1";
		String rootElement1Child1Child1Key2 = "RootElement1Child1Child1";
		String rootElement2Key2 = "RootElement2";
		Date rootElement1Value2 = new Date();
		Date rootElement1Child1Value2 = new Date();
		Date rootElement1Child1Child1Value2 = new Date();
		Date rootElement2Value2 = new Date();

		TransformationTree<String, Date> testMapTree3 = new TransformationTree<String, Date>(rootElement1Key2,
				rootElement1Value2);
		testMapTree3.save();
		allSushiMapTrees = TransformationTree.findAll();
		assertTrue("Expected " + Date.class + " but got " + allSushiMapTrees.get(2).get("RootElement1").getClass(),
				allSushiMapTrees.get(2).get("RootElement1").getClass() == Date.class);
	}

	@Test
	public void testObjectValueType() {
		String rootElement1Key = "RootElement1";
		String rootElement1Child1Key = "RootElement1Child1";
		String rootElement1Child1Child1Key = "RootElement1Child1Child1";
		String rootElement2Key = "RootElement2";
		Date rootElement1Value = new Date();
		Date rootElement1Child1Value = new Date();
		int rootElement1Child1Child1Value = 0;
		String rootElement2Value = "0";

		TransformationTree<String, Serializable> testMapTree = new TransformationTree<String, Serializable>(
				rootElement1Key, rootElement1Value);
		testMapTree.addRootElement(rootElement2Key, rootElement2Value);
		testMapTree.addChild(rootElement1Key, rootElement1Child1Key, rootElement1Child1Value);
		testMapTree.addChild(rootElement1Child1Key, rootElement1Child1Child1Key, rootElement1Child1Child1Value);
		testMapTree.save();
		List<TransformationTree> allSushiMapTrees = TransformationTree.findAll();
		assertTrue("Expected " + Date.class + " but got " + allSushiMapTrees.get(0).get("RootElement1").getClass(),
				allSushiMapTrees.get(0).get("RootElement1").getClass() == Date.class);
		assertTrue("Expected " + Date.class + " but got " + allSushiMapTrees.get(0).get("RootElement1").getClass(),
				allSushiMapTrees.get(0).get("RootElement1").getClass() != String.class);
		assertTrue("Expected " + Integer.class + " but got "
				+ allSushiMapTrees.get(0).get("RootElement1Child1Child1").getClass(),
				allSushiMapTrees.get(0).get("RootElement1Child1Child1").getClass() == Integer.class);
		assertTrue("Expected " + String.class + " but got " + allSushiMapTrees.get(0).get("RootElement2").getClass(),
				allSushiMapTrees.get(0).get("RootElement2").getClass() == String.class);

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

	private void storeExampleMapTree() {
		String rootElement1Key = "RootElement1";
		String rootElement1Child1Key = "RootElement1Child1";
		String rootElement1Child1Child1Key = "RootElement1Child1Child1";
		String rootElement2Key = "RootElement2";
		String rootElement1Value = new String("Root Element 1");
		String rootElement1Child1Value = new String("Root Element 1 Child 1");
		String rootElement1Child1Child1Value = new String("Root Element 1 Child 1 Child 1");
		String rootElement2Value = new String("Root Element 2");

		testMapTree = new TransformationTree<String, String>(rootElement1Key, rootElement1Value);

		testMapTree.addRootElement(rootElement2Key, rootElement2Value);
		testMapTree.addChild(rootElement1Key, rootElement1Child1Key, rootElement1Child1Value);
		testMapTree.addChild(rootElement1Child1Key, rootElement1Child1Child1Key, rootElement1Child1Child1Value);
		testMapTree.save();
	}

}
