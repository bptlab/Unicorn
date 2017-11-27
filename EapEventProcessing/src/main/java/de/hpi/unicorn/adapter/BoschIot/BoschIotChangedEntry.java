package de.hpi.unicorn.adapter.BoschIot;

import org.json.JSONObject;

class BoschIotChangedEntry {

	private JSONObject oldEntry;
	private JSONObject newEntry;

	BoschIotChangedEntry(JSONObject oldEntry, JSONObject newEntry) {
		this.oldEntry = oldEntry;
		this.newEntry = newEntry;
	}

	JSONObject getOldEntry() {
		return oldEntry;
	}

	JSONObject getNewEntry() {
		return newEntry;
	}
}
