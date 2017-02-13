/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.event;

import java.util.Date;

import org.junit.Before;

import de.hpi.unicorn.event.EapEvent;

public class EventTest {

	public static final String KEY1 = "key1";
	public static final String KEY2 = "key2";
	public static final String KEY3 = "key3";

	public static final String VALUE1 = "value1";
	public static final String VALUE2 = "value2";
	public static final String VALUE3 = "value3";

	public static final String SEPARATOR = ".";

	EapEvent event;

	@Before
	public void setup() {
		this.event = new EapEvent(null, new Date());
	}
}
