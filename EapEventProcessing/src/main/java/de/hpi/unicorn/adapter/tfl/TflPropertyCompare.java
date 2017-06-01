package de.hpi.unicorn.adapter.tfl;

import org.json.JSONObject;

interface TflPropertyCompare {

	boolean isDifferent(JSONObject oldEntry, JSONObject newEntry);
}
