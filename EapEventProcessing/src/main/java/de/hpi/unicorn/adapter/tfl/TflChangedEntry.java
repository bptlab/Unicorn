package de.hpi.unicorn.adapter.tfl;

import org.json.JSONObject;

class TflChangedEntry {

	private JSONObject oldEntry;
	private JSONObject newEntry;

	TflChangedEntry(JSONObject oldEntry, JSONObject newEntry) {
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
