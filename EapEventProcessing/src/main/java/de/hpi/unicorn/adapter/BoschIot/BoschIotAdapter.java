package de.hpi.unicorn.adapter.BoschIot;

import de.hpi.unicorn.adapter.EventAdapter;
import de.hpi.unicorn.configuration.EapConfiguration;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.auth.Credentials;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class BoschIotAdapter extends EventAdapter {
	private static final Logger logger = LoggerFactory.getLogger(BoschIotAdapter.class);

	private static final String THING_ADDED_EVENT_TYPE_NAME = "ThingAdded";
	private static final String EXPECTED_ARRIVAL = "expectedArrival";
	private static final String DESTINATION_NAME = "destinationName";
	private static final String DESTINATION_NAPTAN_ID = "destinationNaptanId";

	private static final long TIME_TOLERANCE = 30L * 1000L;

	private String username;
	private String password;
	private SimpleDateFormat biotDateFormat;
	private EapEventType thingAddedEventType;
	private List<EapEvent> eventsToSend;
	private JSONArray boschIotOldThings;

	public BoschIotAdapter(String name, String... modeNames) {
		super(name);

		this.username = EapConfiguration.boschIotUsername;
		this.password = EapConfiguration.boschIotPassword;
		eventsToSend = new ArrayList<>();
        boschIotOldThings = new JSONArray();

		if (username.isEmpty() || password.isEmpty()) {
			logger.info("Bosch iot api username and / or password not configured");
		}
	}

	@Override
	public void trigger() {
		logger.info("Bosch iot adapter triggered: " + (biotDateFormat.format(new Date())));
		if (username.isEmpty() || password.isEmpty()) {
			return;
		}

		if (thingAddedEventType == null && thingAddedEventType() == null) {
			logger.info("Cannot find Bosch iot thingAdded event type.");
			return;
		}

		calculateNewArrivals();

		logger.info("Bosch iot adapter produced " + eventsToSend.size() + " events.");

		Broker.getEventImporter().importEvents(eventsToSend);
		eventsToSend.clear();

		logger.info("Bosch iot adapter finished: " + (biotDateFormat.format(new Date())));
	}

	private EapEventType thingAddedEventType() {
		thingAddedEventType = EapEventType.findByTypeName(THING_ADDED_EVENT_TYPE_NAME);
		return thingAddedEventType;
	}

	private void calculateNewArrivals() {

        JSONArray newResponse = getThings();
        BoschIotArrayDifference diff = new BoschIotArrayDifference(boschIotOldThings, newResponse, "thingId",
                new BoschIotPropertyCompare() {
                    @Override
                    public boolean isDifferent(JSONObject oldEntry, JSONObject newEntry) {
                        if (!oldEntry.has("attributes")
                                || !newEntry.has("attributes")) {
                            return false;
                        }
                        try {
                            String oldManufacturer = oldEntry.getJSONObject("attributes").getString("manufacturer");
                            String newManufacturer = newEntry.getJSONObject("attributes").getString("manufacturer");
                            return !oldManufacturer.equals(newManufacturer);
                        } catch (Exception e) {
                            logger.error("cannot parse Bosch iot api difference", e);
                        }
                        return false;
                    }
                });

        addThingsEvents(diff);

	}

	private void addThingsEvents(BoschIotArrayDifference difference) {
		for (BoschIotChangedEntry changedEntry : difference.getChangedEntries()) {
			try {
				Map<String, Serializable> eventValues = new HashMap<>();

				eventValues.put("thingId", changedEntry.getNewEntry().getString("thingId"));
                eventValues.put("policyId", changedEntry.getNewEntry().getString("policyId"));
                eventValues.put("attributes", changedEntry.getNewEntry().getString("attributes"));
                eventValues.put("features", changedEntry.getNewEntry().getString("features"));

				eventsToSend.add(new EapEvent(thingAddedEventType, new Date(), eventValues));

			} catch (Exception e) {
				logger.error("cannot parse Bosch iot api difference", e);
			}
		}
	}

	@NotNull
	private JSONArray getThings() {

	    return getFromAPI(getThingsUrl());
	}

	private String getThingsUrl() {

		return "https://things.apps.bosch-iot-cloud.com/api/2/search/things";
	}

	@NotNull
	private JSONArray getFromAPI(String url) {
		final HttpClient client = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
		String responseBody = "";
		JSONArray response = new JSONArray();

		try {
			final HttpGet request = new HttpGet(url);
			request.addHeader(BasicScheme.authenticate(credentials, "UTF-8", false));
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
			System.err.println("ERROR: Unexpected Bosch iot api response or no connection possible: " + responseBody);
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}

		return response;
	}
}
