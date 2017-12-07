package de.hpi.unicorn.adapter.BoschIot;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.flipkart.zjsonpatch.JsonDiff;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class BoschIotAdapter extends EventAdapter {
	private static final Logger logger = LoggerFactory.getLogger(BoschIotAdapter.class);

	private static final String THING_ADDED_EVENT_TYPE_NAME = "ThingAdded";
	private static final String ATTRIBUTE_ADDED_EVENT_TYPE_NAME = "AttributeAdded";

	private String username;
	private String password;
	private String apikey;
	private EapEventType thingAddedEventType;
	private EapEventType attributeAddedEventType;
	private List<EapEvent> eventsToSend;
	private ObjectMapper mapper;
	private JsonNode boschIotOldThings;

	public BoschIotAdapter(String name, String... modeNames) {
		super(name);

		this.username = EapConfiguration.boschIotUsername;
		this.password = EapConfiguration.boschIotPassword;
		this.apikey = EapConfiguration.boschIotApiKey;
		this.eventsToSend = new ArrayList<>();
		this.mapper = new ObjectMapper();
		try {
			boschIotOldThings = mapper.readTree("{\"items\":[], \"nextPageOffset\": -1}");
		} catch (Exception e) {
			logger.error("cannot parse default json: ", e);
		}


		if (username.isEmpty() || password.isEmpty()) {
			logger.info("Bosch iot api username and / or password not configured");
		}
	}

	@Override
	public void trigger() {
		logger.info("Bosch iot adapter triggered.");
		if (username.isEmpty() || password.isEmpty()) {
			return;
		}

		if (thingAddedEventType == null && thingAddedEventType() == null) {
			logger.info("Cannot find Bosch iot thingAdded event type.");
			return;
		}

		if (attributeAddedEventType == null && attributeAddedEventType() == null) {
			logger.info("Cannot find Bosch iot attributeAdded event type.");
			return;
		}

		calculateApiDifference();

		logger.info("Bosch iot adapter produced " + eventsToSend.size() + " events.");

		Broker.getEventImporter().importEvents(eventsToSend);
		eventsToSend.clear();

		logger.info("Bosch iot adapter finished.");
	}

	private EapEventType thingAddedEventType() {
		thingAddedEventType = EapEventType.findByTypeName(THING_ADDED_EVENT_TYPE_NAME);
		return thingAddedEventType;
	}

	private EapEventType attributeAddedEventType() {
		attributeAddedEventType = EapEventType.findByTypeName(ATTRIBUTE_ADDED_EVENT_TYPE_NAME);
		return attributeAddedEventType;
	}

	private void calculateApiDifference() {
		JsonNode things = getThings();
		JsonNode difference = JsonDiff.asJson(boschIotOldThings, things);
		addThingsEvents(things, difference);
		boschIotOldThings = things;
	}


	private void addThingsEvents(JsonNode response, JsonNode difference) {
		Iterator<JsonNode> elements = difference.elements();
		while (elements.hasNext()) {
			JsonNode element = elements.next();

			if (!element.has("op") || !element.has("path") || !element.has("value")) {
				continue;
			}
			String operation =  element.get("op").asText();
			String path = element.get("path").asText();
			String[] splittedPath = element.get("path").asText().split("/");
			JsonNode value =  element.get("value");
			switch (operation) {
				case "add":
					// Declare variables
					String thing, attribute, feature, property;


					Pattern pattern;
					Matcher matcher;

					pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)");
					matcher = pattern.matcher(path);
					if (matcher.matches()) {
						String item = matcher.group("item");
						String thingId = value.get("thingId").asText();
						String policyId = value.get("policyId").asText();
						String attributes = value.get("attributes").toString();
						String features = value.get("features").toString();

						logger.info("item: " + item);
						logger.info("thingId: " + thingId);
						logger.info("policyId: " + policyId);
						logger.info("attributes: " + attributes);
						logger.info("features: " + features);
						logger.info("Thing added!");

						Map<String, Serializable> eventValues = new HashMap<>();
						eventValues.put("thingId", thingId);
						eventValues.put("thingId", policyId);
						eventValues.put("thingId", attributes);
						eventValues.put("thingId", features);
						eventsToSend.add(new EapEvent(attributeAddedEventType, new Date(), eventValues));

						break;
					}

					pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)");
					matcher = pattern.matcher(path);
					if (matcher.matches()) {
						logger.info("item: " + matcher.group("item"));
					}








					if (!(splittedPath.length >= 3 && splittedPath[1].equals("items"))) {
						// Malformed change route
						break;
					}

					// THING changed
					// Item ID found
					thing = splittedPath[2];
					logger.info("thing: " + thing);

					if (Pattern.matches("\\/items\\/(?<item>[0-9]*)", path)) {
						logger.info("MATCH item!");
					}

					if (!(splittedPath.length >= 5)) {
						// If url is for example: /items/3
						logger.info("Thing added: " + thing);
						logger.info("Thing value: " + value.toString());
						break;
					}

					if (Pattern.matches("\\/items\\/(?<item>[0-9]+)\\/attributes\\/(?<attribute>.+)", path)) {
						logger.info("MATCH attribute!");
					}

					Pattern attributePattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)\\/attributes\\/(?<attribute>.+)");
					Matcher matcher = attributePattern.matcher(path);
					if (matcher.matches()) {
						logger.info("MATCH!");
						logger.info("item: " + matcher.group("item"));
						logger.info("attribute: " + matcher.group("attribute"));
					}



					// ATTRIBUTE changed
					if (splittedPath[3].equals("attributes")) {
						attribute = splittedPath[4];
						logger.info("attribute: " + attribute);
						logger.info("Attribute added: " + attribute);
						logger.info("Attribute value: " + value.asText());

						// TODO
						Map<String, Serializable> eventValues = new HashMap<>();
						eventValues.put("thingId", response.get("items").get(Integer.parseInt(thing)).get("thingId").asText());
						eventValues.put("attribute", attribute);
						eventValues.put("attributeValue", value.asText());
						eventsToSend.add(new EapEvent(attributeAddedEventType, new Date(), eventValues));
						break;
					}

					// FEATURE changed
					if (splittedPath[3].equals("features")) {
						feature = splittedPath[4];
						logger.info("feature: " + feature);

						if (!(splittedPath.length >= 7 && splittedPath[5].equals("properties"))) {
							logger.info("Feature added: " + feature);
							logger.info("Feature value: " + value.toString());
							break;
						}
					}

					if (splittedPath.length >= 7 && splittedPath[5].equals("properties")) {
						property = splittedPath[6];
						logger.info("property: " + property);
						logger.info("Property added: " + property);
						logger.info("Property value: " + value.asText());
					}

					logger.info("Element added: " + element.toString());
					break;
				case "replace":
					logger.info("Value replaced: " + element.toString());
					break;
				case "remove":
					logger.info("Element removed: " + element.toString());
					break;
			}
		}
	}

	@NotNull
	private JsonNode getThings() {

	    return getFromAPI(getThingsUrl());
	}

	private String getThingsUrl() {

		return "https://things.apps.bosch-iot-cloud.com/api/2/search/things";
	}

	@NotNull
	private JsonNode getFromAPI(String url) {
		final HttpClient client = new DefaultHttpClient();
        Credentials credentials = new UsernamePasswordCredentials(username, password);
		String responseBody = "";
		JsonNode response = null;

		try {

			final HttpGet request = new HttpGet(url);
			request.addHeader(BasicScheme.authenticate(credentials, "UTF-8", false));
			request.setHeader("x-cr-api-token", apikey);
			logger.info("request: "+request);
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

				//response = new JSONObject(responseBody);

				response = mapper.readTree(responseBody);

			} else {
				//response = new JSONObject();

				response = mapper.readTree("");
			}
			return response;
		} catch (final Exception e) {
			System.err.println("ERROR: Unexpected Bosch iot api response or no connection possible: " + responseBody);
			e.printStackTrace();
		} finally {
			client.getConnectionManager().shutdown();
		}
		return response;

	}
}
