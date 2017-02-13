/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import static org.junit.Assert.fail;

import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * This class tests the throwing of exception for forbidden names for the
 * {@link EapEventType}.
 */
public class EventTypeTest {

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException1() {
		new EapEventType("abcdéf");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException2() {
		new EapEventType("abcdef!");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException3() {
		new EapEventType("abcd?");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException4() {
		new EapEventType("abcdef/");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException5() {
		new EapEventType("abcß");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException6() {
		new EapEventType("abcdef()");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException7() {
		new EapEventType("faâ");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException8() {
		new EapEventType("");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException11() {
		TypeTreeNode rootElement1 = new TypeTreeNode("Root Element 1");
		TypeTreeNode rootElement1Child1 = new TypeTreeNode(rootElement1, "Root Element 1 Child 1",
				AttributeTypeEnum.INTEGER);
		new TypeTreeNode(rootElement1Child1, "Root Element 1 Child 1 Child 1", AttributeTypeEnum.DATE);
		new TypeTreeNode(rootElement1Child1, "Root Element 1 Child 1 Child 2", AttributeTypeEnum.FLOAT);
		TypeTreeNode rootElement2 = new TypeTreeNode("Root Element 2", AttributeTypeEnum.STRING);
		AttributeTypeTree testTree = new AttributeTypeTree();
		testTree.addRoot(rootElement1);
		testTree.addRoot(rootElement2);
		testTree.save();

		new EapEventType("faâ", testTree);
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException12() {
		new EapEventType("fa/f");
	}

	@Test(expected = RuntimeException.class)
	public void testForbiddenCharactersInNameThrowException13() {
		new EapEventType("");
	}

	@SuppressWarnings("unused")
	@Test
	public void testAllowedCharacterInNameThrowNoException() {
		String[] names = { "ab c", "abc", "abc0", "abc_", "abc_s", "abc01_2", "abc-d", "abc-0-1", "___" };
		EapEventType type;
		String testedName = null;
		try {
			for (String name : names) {
				testedName = name;
				type = new EapEventType(name);
			}
		} catch (RuntimeException e) {
			fail(testedName);
		}
	}
}
