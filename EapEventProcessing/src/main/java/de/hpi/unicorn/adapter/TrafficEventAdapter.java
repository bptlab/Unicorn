/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class TrafficEventAdapter extends EventAdapter {

	public TrafficEventAdapter(final String name) {
		super(name);
	}

	// public void TrafficEventAdapter(double lat1, double lng1, double lat2,
	// double lng2);
	// public void TrafficEventAdapter(int width, double ... coordinates);
	// public void TrafficEventAdapter(double lat, double lng, int width);
	public abstract JSONObject callWebservice();

	public abstract void parseResponse(JSONObject response) throws JSONException;

	public abstract void sendEventsToEAP();
}
