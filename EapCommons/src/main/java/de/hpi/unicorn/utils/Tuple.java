/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

/**
 * generic tuple class
 */
public class Tuple<X, Y> {
	public final X x;
	public final Y y;

	public Tuple(final X x, final Y y) {
		this.x = x;
		this.y = y;
	}

	public Tuple() {
		this.x = null;
		this.y = null;
	}

	@Override
	public String toString() {
		return "Tuple:" + this.x.toString() + "," + this.y.toString();
	}
}