/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.utils.GeoUtils;
import de.hpi.unicorn.utils.GeoUtils.Coord;

public class GeoUtilsTest {

	Coord c1, c2;
	String lat1, lng1, lat2, lng2;

	@Before
	public void setUp() throws Exception {
		this.c1 = new Coord(52.39385811588161, 13.128627240657806);
		this.c2 = new Coord(52.39395959517328, 13.132838308811188);
		this.lat1 = "52.39385811588161";
		this.lng1 = "13.128627240657806";
		this.lat2 = "52.39395959517328";
		this.lng2 = "13.132838308811188";
	}

	@Test
	public void testCoordDistance() {
		Assert.assertTrue(GeoUtils.distance(this.c1, this.c2) < 0.3);
		Assert.assertTrue(GeoUtils.distance(this.c1, this.c2) > 0.25);
	}

	@Test
	public void testStringCoordinates() {
		Assert.assertEquals(GeoUtils.distance(this.c1, this.c2),
				GeoUtils.distance(this.lat1, this.lng1, this.lat2, this.lng2), 0);
	}

}
