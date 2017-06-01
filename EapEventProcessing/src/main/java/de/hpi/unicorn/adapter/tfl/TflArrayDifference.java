package de.hpi.unicorn.adapter.tfl;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class TflArrayDifference {

	private List<JSONObject> newEntries;
	private List<TflChangedEntry> changedEntries;
	private List<JSONObject> deletedEntries;

	TflArrayDifference(JSONArray oldArray, JSONArray newArray, String idProperty, TflPropertyCompare... propertyCompares) {
		newEntries = new ArrayList<>();
		changedEntries = new ArrayList<>();
		deletedEntries = new ArrayList<>();

		calculateDifference(oldArray, newArray, idProperty, propertyCompares);
	}

	List<JSONObject> getNewEntries() {
		return newEntries;
	}

	List<TflChangedEntry> getChangedEntries() {
		return changedEntries;
	}

	List<JSONObject> getDeletedEntries() {
		return deletedEntries;
	}

	private void calculateDifference(JSONArray oldArray, JSONArray newArray, String idProperty, TflPropertyCompare... propertyCompares) {
		Map<String, JSONObject> oldMap = TflUtil.toMap(oldArray, idProperty);
		Map<String, JSONObject> newMap = TflUtil.toMap(newArray, idProperty);

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

	private void calculateChangedEntries(Map<String, JSONObject> oldMap, Map<String, JSONObject> newMap, TflPropertyCompare... propertyCompares) {
		for (Map.Entry<String, JSONObject> oldEntry : oldMap.entrySet()) {
			if (!newMap.containsKey(oldEntry.getKey())) {
				continue;
			}

			JSONObject oldObject = oldEntry.getValue();
			JSONObject newObject = newMap.get(oldEntry.getKey());

			for (TflPropertyCompare propertyCompare : propertyCompares) {
				if (!propertyCompare.isDifferent(oldObject, newObject)) {
					continue;
				}

				changedEntries.add(new TflChangedEntry(oldObject, newObject));
			}
		}
	}
}
