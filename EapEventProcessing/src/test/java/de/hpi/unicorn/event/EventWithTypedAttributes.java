/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.TransformationTree;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

public class EventWithTypedAttributes {

	private String rootElement1Key;
	private String rootElement1Child1Key;
	private String rootElement1Child2Key;
	private String rootElement2Key;

	private String rootElement1Child1Value;
	private int rootElement1Child2Value;
	private Date rootElement2Value;

	private EapEventType type;
	private EapEvent event;

	@Before
	public void setup() {
		Persistor.useTestEnviroment();
		this.rootElement1Key = "RootElement";
		this.rootElement1Child1Key = "RootElementStringChild";
		this.rootElement1Child2Key = "RootElementIntegerChild";
		this.rootElement2Key = "RootDateElement";
		this.rootElement1Child1Value = new String("Root Element 1");
		this.rootElement1Child2Value = 2;
		this.rootElement2Value = new Date(2011 - 1900, 5 - 1, 17);
	}

	@Test
	public void testTreeAdding() {
		final TransformationTree<String, Serializable> testMapTree = new TransformationTree<String, Serializable>(
				this.rootElement1Key, null);
		Assert.assertFalse(testMapTree.isEmpty());
		Assert.assertTrue(testMapTree.getRootElementValues().size() == 1);
		Assert.assertTrue(testMapTree.getRootElementValues().get(0) == null);

		testMapTree.addRootElement(this.rootElement2Key, this.rootElement2Value);
		testMapTree.addChild(this.rootElement1Key, this.rootElement1Child1Key, this.rootElement1Child1Value);
		testMapTree.addChild(this.rootElement1Key, this.rootElement1Child2Key, this.rootElement1Child2Value);
	}

	@Test
	public void testCreateTypedEventType() {
		this.type = new EapEventType("testEventTypeTyped", this.createTree(), "testTimestamp");
		Broker.getInstance().importEventType(this.type);

		final QueryWrapper liveTyped = new QueryWrapper("testTypes", "SELECT * FROM testEventTypeTyped ",
				QueryTypeEnum.LIVE);
		liveTyped.addToEsper();

		this.event = new EapEvent(this.type, new Date(), this.createMap());
		Broker.getInstance().importEvent(this.event);

		// System.out.println(Arrays.asList((StreamProcessingAdapter.getInstance().getAttributesOfEventType(type))));
		Assert.assertTrue(Arrays.asList(StreamProcessingAdapter.getInstance().getAttributesOfEventType(this.type))
				.contains(this.rootElement1Key + "." + this.rootElement1Child2Key));

		Assert.assertTrue(StreamProcessingAdapter.getInstance().eventTypeHasAttribute(this.type,
				this.rootElement1Key + "." + this.rootElement1Child2Key));

		Assert.assertTrue("type of eventtype " + this.rootElement2Key + " was "
				+ StreamProcessingAdapter.getInstance().getEventTypeInfo(this.type, this.rootElement2Key),
				StreamProcessingAdapter.getInstance().getEventTypeInfo(this.type, this.rootElement2Key) == Date.class);
		Assert.assertTrue(StreamProcessingAdapter.getInstance().getEventTypeInfo(this.type, "Timestamp") == Date.class);
		Assert.assertTrue(
				"type of eventtype "
						+ this.rootElement1Key
						+ "."
						+ this.rootElement1Child2Key
						+ " was "
						+ StreamProcessingAdapter.getInstance().getEventTypeInfo(this.type,
								this.rootElement1Key + "." + this.rootElement1Child2Key),
				StreamProcessingAdapter.getInstance().getEventTypeInfo(this.type,
						this.rootElement1Key + "." + this.rootElement1Child2Key) == Integer.class);

		final QueryWrapper testTyped = new QueryWrapper("testTypes", "SELECT RootDateElement.getTime(), "
				+ this.rootElement1Key + "." + this.rootElement1Child1Key + " , " + this.rootElement1Key + "."
				+ this.rootElement1Child2Key + " FROM testEventTypeTypedWindow " + "WHERE " + this.rootElement1Key
				+ "." + this.rootElement1Child1Key + " = '" + this.rootElement1Child1Value + "' " + " AND "
				+ this.rootElement1Key + "." + this.rootElement1Child2Key + " > 0", QueryTypeEnum.ONDEMAND);
		final String result = testTyped.execute();
		// System.out.println(result);
		Assert.assertTrue(result.contains("Number of events found: 1"));

	}

	private Map<String, Serializable> createMap() {
		final Map<String, Serializable> map = new HashMap<String, Serializable>();
		map.put(this.rootElement2Key, this.rootElement2Value);
		map.put(this.rootElement1Key + "." + this.rootElement1Child1Key, this.rootElement1Child1Value);
		map.put(this.rootElement1Key + "." + this.rootElement1Child2Key, this.rootElement1Child2Value);

		return map;
	}

	private AttributeTypeTree createTree() {
		final TypeTreeNode rootElement1 = new TypeTreeNode(this.rootElement1Key);
		new TypeTreeNode(rootElement1, this.rootElement1Child1Key, AttributeTypeEnum.STRING);
		new TypeTreeNode(rootElement1, this.rootElement1Child2Key, AttributeTypeEnum.INTEGER);
		final TypeTreeNode rootElement2 = new TypeTreeNode(this.rootElement2Key, AttributeTypeEnum.DATE);

		final AttributeTypeTree tree = new AttributeTypeTree(rootElement1);
		tree.addRoot(rootElement2);
		return tree;
	}

}
