package de.hpi.unicorn.adapter.tfl;

import de.hpi.unicorn.adapter.EventAdapter;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TflAdapter extends EventAdapter {
	private static final Logger logger = LoggerFactory.getLogger(TflAdapter.class);

	private static final String EXPECTED_ARRIVAL_CHANGED_EVENT_TYPE_NAME = "ExpectedArrivalChanged";
	private static final String VEHICLE_STARTED_EVENT_TYPE_NAME = "VehicleStarted";
	private static final String VEHICLE_ARRIVED_EVENT_TYPE_NAME = "VehicleArrived";
	private static final String TRANSPORT_MODE_NAME = "modeName";
	private static final String LINE_ID = "id";
	private static final String EXPECTED_ARRIVAL = "expectedArrival";
	private static final String DESTINATION_NAME = "destinationName";
	private static final String DESTINATION_NAPTAN_ID = "destinationNaptanId";
	private static final String VEHICLE_ID = "vehicleId";

	private static final long TIME_TOLERANCE = 30L * 1000L;

	private String appId;
	private String appKey;
	private SimpleDateFormat tflDateFormat;
	private EapEventType expectedArrivalChangedEventType;
	private EapEventType vehicleStartedEventType;
	private EapEventType vehicleArrivedEventType;
	private List<String> modeNames;
	private Map<String, JSONObject> tflTransportModes; // id --> mode-object
	private Map<String, HashMap<String, JSONObject>> tflTransportLines; // transportMode-id --> (line-id --> line-object)
	private Map<String, HashMap<String, JSONArray>> tflLastArrivals;
	private List<EapEvent> eventsToSend;

	public TflAdapter(String name, String... modeNames) {
		super(name);

		this.appId = EapConfiguration.tflAppId;
		this.appKey = EapConfiguration.tflAppCode;
		this.modeNames = Arrays.asList(modeNames);
		tflDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		tflTransportModes = new HashMap<>();
		tflTransportLines = new HashMap<>();
		tflLastArrivals = new HashMap<>();
		eventsToSend = new ArrayList<>();

		if (appId.isEmpty() || appKey.isEmpty()) {
			logger.info("tfl app key and/or app id not configured");
		}
	}

	@Override
	public void trigger() {
		logger.info("tfl adapter triggered: " + (tflDateFormat.format(new Date())));
		if (appId.isEmpty() || appKey.isEmpty()) {
			return;
		}

		if (expectedArrivalChangedEventType == null && getExpectedArrivalChangedEventType() == null) {
			logger.info("cannot find tfl ExpectedArrivalChanged event type");
			return;
		}

//		if (vehicleStartedEventType == null && getVehicleStartedEventType() == null) {
//			logger.info("cannot find tfl VehicleStarted event type");
//			return;
//		}
//
//		if (vehicleArrivedEventType == null && getVehicleArrivedEventType() == null) {
//			logger.info("cannot find tfl VehicleArrived event type");
//			return;
//		}

		loadModes();
		loadLines();
		calculateNewArrivals();

		logger.info("tfl adapter produced " + eventsToSend.size() + " events");

		Broker.getEventImporter().importEvents(eventsToSend);
		eventsToSend.clear();

		logger.info("tfl adapter finished: " + (tflDateFormat.format(new Date())));
	}

	public void setModes(String... modeNames) {
		this.modeNames = Arrays.asList(modeNames);
		tflTransportModes = new HashMap<>();
		tflTransportLines = new HashMap<>();
		tflLastArrivals = new HashMap<>();
	}

	private void calculateNewArrivals() {
		for (String modeName : tflTransportModes.keySet()) {
			if (!modeNames.isEmpty() && !modeNames.contains(modeName)) {
				continue;
			}

			if (!tflLastArrivals.containsKey(modeName)) {
				tflLastArrivals.put(modeName, new HashMap<String, JSONArray>());
			}

			for (String lineName : tflTransportLines.get(modeName).keySet()) {
				if (!tflLastArrivals.get(modeName).containsKey(lineName)) {
					tflLastArrivals.get(modeName).put(lineName, new JSONArray());
				}

				JSONArray oldResponse = tflLastArrivals.get(modeName).get(lineName);
				JSONArray newResponse = getArrivals(lineName);

				if (newResponse.length() == 0) {
					continue;
				}

				tflLastArrivals.get(modeName).put(lineName, newResponse);

				TflArrayDifference diff = new TflArrayDifference(oldResponse, newResponse, "id",
						new TflPropertyCompare() {
							@Override
							public boolean isDifferent(JSONObject oldEntry, JSONObject newEntry) {

								if (!oldEntry.has(DESTINATION_NAME)
										|| !newEntry.has(DESTINATION_NAME)
										|| !oldEntry.has(EXPECTED_ARRIVAL)
										|| !newEntry.has(EXPECTED_ARRIVAL)) {
									return false;
								}


								try {
									if (!oldEntry.getString(DESTINATION_NAPTAN_ID).equals(newEntry.getString(DESTINATION_NAPTAN_ID))) {
										return false;
									}

									Date oldExpectedArrival = tflDateFormat.parse(oldEntry.getString(EXPECTED_ARRIVAL));
									Date newExpectedArrival = tflDateFormat.parse(newEntry.getString(EXPECTED_ARRIVAL));

									return !isInTolerance(oldExpectedArrival.getTime(), TIME_TOLERANCE, newExpectedArrival.getTime());
								} catch (Exception e) {
									logger.error("ERROR: cannot parse tfl expected arrival date", e);
								}

								return false;
							}
						});

				addArrivalEvents(modeName, lineName, diff);
//				addVehicleStartedEvents(modeName, lineName, diff);
//				addVehicleArrivedEvents(modeName, lineName, diff);
			}
		}
	}

	private boolean isInTolerance(long expected, long tolerance, long actual) {
		return Math.abs(actual - expected) <= tolerance;
	}

	private void addArrivalEvents(String modeName, String lineName, TflArrayDifference difference) {
		for (TflChangedEntry changedEntry : difference.getChangedEntries()) {
			try {
				Map<String, Serializable> eventValues = new HashMap<>();

				eventValues.put("transportationModeId", modeName);
				eventValues.put("lineId", lineName);
				eventValues.put("naptanId", changedEntry.getNewEntry().getString("naptanId"));
				eventValues.put("oldExpectedArrival", changedEntry.getOldEntry().getString(EXPECTED_ARRIVAL));
				eventValues.put("newExpectedArrival", changedEntry.getNewEntry().getString(EXPECTED_ARRIVAL));
				eventValues.put("destinationName", changedEntry.getNewEntry().getString(DESTINATION_NAME));

				eventsToSend.add(new EapEvent(expectedArrivalChangedEventType, new Date(), eventValues));

			} catch (Exception e) {
				logger.error("cannot parse tfl difference", e);
			}
		}
	}

	private void addVehicleStartedEvents(String modeName, String lineName, TflArrayDifference difference) {
		for (JSONObject newEntry : difference.getNewEntries()) {
			addVehicleEvent(vehicleStartedEventType, modeName, lineName, newEntry);
		}
	}

	private void addVehicleArrivedEvents(String modeName, String lineName, TflArrayDifference difference) {
		for (JSONObject deletedEntry : difference.getDeletedEntries()) {
			addVehicleEvent(vehicleArrivedEventType, modeName, lineName, deletedEntry);
		}
	}

	private void addVehicleEvent(EapEventType eventType, String modeName, String lineName, JSONObject arrivalObject) {
		if (!arrivalObject.has(VEHICLE_ID)) {
			return;
		}

		try {
			Map<String, Serializable> eventValues = new HashMap<>();

			eventValues.put("transportationModeId", modeName);
			eventValues.put("lineId", lineName);
			eventValues.put("vehicleId", arrivalObject.getString(VEHICLE_ID));

			eventsToSend.add(new EapEvent(eventType, new Date(), eventValues));

		} catch (Exception e) {
			logger.error("cannot parse tfl arrival object", e);
		}
	}

	private EapEventType getExpectedArrivalChangedEventType() {
		expectedArrivalChangedEventType = EapEventType.findByTypeName(EXPECTED_ARRIVAL_CHANGED_EVENT_TYPE_NAME);
		return expectedArrivalChangedEventType;
	}

	private EapEventType getVehicleStartedEventType() {
		vehicleStartedEventType = EapEventType.findByTypeName(VEHICLE_STARTED_EVENT_TYPE_NAME);
		return vehicleStartedEventType;
	}

	private EapEventType getVehicleArrivedEventType() {
		vehicleArrivedEventType = EapEventType.findByTypeName(VEHICLE_ARRIVED_EVENT_TYPE_NAME);
		return vehicleArrivedEventType;
	}

	private void loadModes() {
		if (!tflTransportModes.isEmpty()) {
			return;
		}

		JSONArray modes = getFromAPI(getModesUrl());
		tflTransportModes = TflUtil.toMap(modes, TRANSPORT_MODE_NAME);
	}

	private void loadLines() {
		if (tflTransportModes.isEmpty()) {
			return;
		}

		for (String modeName : tflTransportModes.keySet()) {
			if (!modeNames.isEmpty() && !modeNames.contains(modeName)) {
				continue;
			}


			if (!tflTransportLines.containsKey(modeName)) {
				tflTransportLines.put(modeName, new HashMap<String, JSONObject>());
			}
			JSONArray lines = getFromAPI(getLinesUrl(modeName));

			for (int i = 0; i < lines.length(); i++) {
				try {
					JSONObject line = lines.getJSONObject(i);
					tflTransportLines.get(modeName).put(line.getString(LINE_ID), line);
				} catch (JSONException e) {
					logger.error("cannot load tfl lines", e);
				}
			}
		}
	}

	@NotNull
	private JSONArray getArrivals(String lineName) {
		return getFromAPI(getArrivalsUrl(lineName));
	}

	private String getModesUrl() {
		String url = "https://api.tfl.gov.uk/Line/Meta/Modes?app_id=<<app_id>>&app_key=<<app_key>>";

		url = finalizeUrl(url);

		return url;
	}

	private String getLinesUrl(String modeName) {
		String url = "https://api.tfl.gov.uk/Line/Mode/<<mode_name>>?app_id=<<app_id>>&app_key=<<app_key>>";

		url = setUrlParameter(url, "mode_name", modeName);
		url = finalizeUrl(url);

		return url;
	}

	private String getArrivalsUrl(String lineName) {
		String url = "https://api.tfl.gov.uk/Line/<<line_name>>/Arrivals?app_id=<<app_id>>&app_key=<<app_key>>";

		url = setUrlParameter(url, "line_name", lineName);
		url = finalizeUrl(url);

		return url;
	}

	private String finalizeUrl(String url) {
		String finalUrl = url;
		finalUrl = setUrlParameter(finalUrl, "app_id", appId);
		finalUrl = setUrlParameter(finalUrl, "app_key", appKey);

		//logger.info("tfl final url: " + finalUrl);

		return finalUrl;

	}

	private String setUrlParameter(String url, String parameterName, String parameterValue) {
		return url.replaceAll("<<" + parameterName + ">>", parameterValue);
	}

	@NotNull
	private JSONArray getFromAPI(String url) {
		final HttpClient client = new DefaultHttpClient();
		String responseBody = "";
		JSONArray response = new JSONArray();

		try {
			final HttpGet request = new HttpGet(url);
			HttpResponse httpResponse = client.execute(request);
			int tries = 0;

			while (httpResponse.getStatusLine().getStatusCode() != 200) {
				Thread.sleep(500);
				httpResponse = client.execute(request);

				if (tries++ >= 10) {
					break;
				}
			}

			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				ResponseHandler<String> responseHandler = new BasicResponseHandler();
				responseBody = responseHandler.handleResponse(httpResponse);
				response = new JSONArray(responseBody);
			} else {
				response = new JSONArray();
			}

		} catch (final Exception e) {
			System.err.println("ERROR: unexpected TflAPI respond or no connection possible: " + responseBody);
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}

		return response;
	}
}
