package de.hpi.unicorn.adapter.BoschIot;

import org.json.JSONObject;

interface BoschIotPropertyCompare {

	boolean isDifferent(JSONObject oldEntry, JSONObject newEntry);
}
