package de.hpi.unicorn.adapter.BoschIot;

import de.hpi.unicorn.adapter.BoschIot.BoschIotChangedEntry;
import de.hpi.unicorn.adapter.BoschIot.BoschIotPropertyCompare;
import de.hpi.unicorn.adapter.BoschIot.BoschIotUtil;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class BoschIotArrayDifference {

	private List<JSONObject> newEntries;
	private List<BoschIotChangedEntry> changedEntries;
	private List<JSONObject> deletedEntries;

	BoschIotArrayDifference(JSONArray oldArray, JSONArray newArray, String idProperty, BoschIotPropertyCompare... propertyCompares) {
		newEntries = new ArrayList<>();
		changedEntries = new ArrayList<>();
		deletedEntries = new ArrayList<>();

		calculateDifference(oldArray, newArray, idProperty, propertyCompares);
	}

	List<JSONObject> getNewEntries() {
		return newEntries;
	}

	List<BoschIotChangedEntry> getChangedEntries() {
		return changedEntries;
	}

	List<JSONObject> getDeletedEntries() {
		return deletedEntries;
	}

	private void calculateDifference(JSONArray oldArray, JSONArray newArray, String idProperty, BoschIotPropertyCompare... propertyCompares) {
		Map<String, JSONObject> oldMap = BoschIotUtil.toMap(oldArray, idProperty);
		Map<String, JSONObject> newMap = BoschIotUtil.toMap(newArray, idProperty);

		calculateDeletedEntries(oldMap, newMap);
		calculateNewEntries(oldMap, newMap);
		calculateChangedEntries(oldMap, newMap, propertyCompares);
	}

	private void calculateDeletedEntries(Map<String, JSONObject> oldMap, Map<String, JSONObject> newMap) {
		for (Map.Entry<String, JSONObject> oldEntry : oldMap.entrySet()) {
			if (newMap.containsKey(oldEntry.getKey())) {
				continue;
			}

			deletedEntries.add(oldEntry.getValue());
		}
	}

	private void calculateNewEntries(Map<String, JSONObject> oldMap, Map<String, JSONObject> newMap) {
		for (Map.Entry<String, JSONObject> newEntry : newMap.entrySet()) {
			if (oldMap.containsKey(newEntry.getKey())) {
				continue;
			}

			newEntries.add(newEntry.getValue());
		}
	}

	private void calculateChangedEntries(Map<String, JSONObject> oldMap, Map<String, JSONObject> newMap, BoschIotPropertyCompare... propertyCompares) {
		for (Map.Entry<String, JSONObject> oldEntry : oldMap.entrySet()) {
			if (!newMap.containsKey(oldEntry.getKey())) {
				continue;
			}

			JSONObject oldObject = oldEntry.getValue();
			JSONObject newObject = newMap.get(oldEntry.getKey());

			for (BoschIotPropertyCompare propertyCompare : propertyCompares) {
				if (!propertyCompare.isDifferent(oldObject, newObject)) {
					continue;
				}

				changedEntries.add(new BoschIotChangedEntry(oldObject, newObject));
				break;
			}
		}
	}
}
