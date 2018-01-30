package de.hpi.unicorn.adapter.BoschIot;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

final class BoschIotUtil {

	private BoschIotUtil() {

	}

	static Map<String, JSONObject> toMap(JSONArray array, String idProperty) {
		Map<String, JSONObject> map = new HashMap<>();

		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject entry = array.getJSONObject(i);

				map.put(entry.getString(idProperty), entry);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		return map;
	}
}
