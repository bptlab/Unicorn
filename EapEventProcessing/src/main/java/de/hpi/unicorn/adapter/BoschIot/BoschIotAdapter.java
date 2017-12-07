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
	private static final String FEATURE_ADDED_EVENT_TYPE_NAME = "FeatureAdded";
	private static final String PROPERTY_ADDED_EVENT_TYPE_NAME = "PropertyAdded";

	private String username;
	private String password;
	private String apikey;
	private EapEventType thingAddedEventType;
	private EapEventType attributeAddedEventType;
	private EapEventType featureAddedEventType;
	private EapEventType propertyAddedEventType;
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

		if (featureAddedEventType == null && featureAddedEventType() == null) {
			logger.info("Cannot find Bosch iot attributeAdded event type.");
			return;
		}

		if (propertyAddedEventType == null && propertyAddedEventType() == null) {
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

	private EapEventType featureAddedEventType() {
		featureAddedEventType = EapEventType.findByTypeName(FEATURE_ADDED_EVENT_TYPE_NAME);
		return featureAddedEventType;
	}

	private EapEventType propertyAddedEventType() {
		propertyAddedEventType = EapEventType.findByTypeName(PROPERTY_ADDED_EVENT_TYPE_NAME);
		return propertyAddedEventType;
	}

	private void calculateApiDifference() {
		JsonNode thingsResponse = getThings();
		JsonNode difference = JsonDiff.asJson(boschIotOldThings, thingsResponse);
		addThingsEvents(boschIotOldThings, thingsResponse, difference);
		boschIotOldThings = thingsResponse;
	}


	private void addThingsEvents(JsonNode oldThingsResponse, JsonNode newThingsResponse, JsonNode difference) {
		Iterator<JsonNode> elements = difference.elements();
		while (elements.hasNext()) {
			JsonNode element = elements.next();

			if (!element.has("op") || !element.has("path") || !element.has("value")) {
				continue;
			}
			String operation =  element.get("op").asText();
			String path = element.get("path").asText();
			JsonNode value =  element.get("value");
			switch (operation) {
				case "add":
					addThingRelatedEvent(path, value, thingAddedEventType);
					addAttributeRelatedEvent(path, value, newThingsResponse, attributeAddedEventType);
					addFeatureRelatedEvent(path, value, newThingsResponse, featureAddedEventType);
					addPropertyRelatedEvent(path, value, newThingsResponse, propertyAddedEventType);
					logger.info("Element added: " + element.toString());
					break;

				case "replace":
					addThingRelatedEvent(path, value, thingAddedEventType);
					addAttributeRelatedEvent(path, value, newThingsResponse, attributeAddedEventType);
					addFeatureRelatedEvent(path, value, newThingsResponse, featureAddedEventType);
					addPropertyRelatedEvent(path, value, newThingsResponse, propertyAddedEventType);
					logger.info("Element changed: " + element.toString());
					break;
				case "remove":
					addThingRelatedEvent(path, value, thingAddedEventType);
					addAttributeRelatedEvent(path, value, oldThingsResponse, attributeAddedEventType);
					addFeatureRelatedEvent(path, value, oldThingsResponse, featureAddedEventType);
					addPropertyRelatedEvent(path, value, oldThingsResponse, propertyAddedEventType);
					logger.info("Element removed: " + element.toString());
					break;
			}
		}
	}

	private void addThingRelatedEvent(String path, JsonNode change, EapEventType eventType) {
		Pattern pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)");
		Matcher matcher = pattern.matcher(path);

		if (matcher.matches()) {
            String item = matcher.group("item");
            String thingId = change.get("thingId").asText();
            String policyId = change.get("policyId").asText();
            String attributes = change.get("attributes").toString();
            String features = change.get("features").toString();

            logger.info("item: " + item);
            logger.info("thingId: " + thingId);
            logger.info("policyId: " + policyId);
            logger.info("attributes: " + attributes);
            logger.info("features: " + features);
            logger.info("Thing added!");

            Map<String, Serializable> eventValues = new HashMap<>();
            eventValues.put("thingId", thingId);
            eventValues.put("policyId", policyId);
            eventValues.put("attributes", attributes);
            eventValues.put("features", features);
            eventsToSend.add(new EapEvent(eventType, new Date(), eventValues));
        }
	}

	private void addAttributeRelatedEvent(String path, JsonNode change, JsonNode thingsResponse, EapEventType eventType) {
		Pattern pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)\\/attributes\\/(?<attribute>[^\\/]+)");
		Matcher matcher = pattern.matcher(path);
		if (matcher.matches()) {
			String item = matcher.group("item");
			String attribute = matcher.group("attribute");
			String thingId = thingsResponse.get("items").get(Integer.parseInt(item)).get("thingId").asText();
			String attributeValue = change.asText();

			logger.info("item: " + item);
			logger.info("thingId: " + thingId);
			logger.info("attribute: " + attribute);
			logger.info("attributeValue: " + attributeValue);
			logger.info("Attribute added!");

			Map<String, Serializable> eventValues = new HashMap<>();
			eventValues.put("thingId", thingId);
			eventValues.put("attribute", attribute);
			eventValues.put("attributeValue", attributeValue);
			eventsToSend.add(new EapEvent(eventType, new Date(), eventValues));
		}
	}

	private void addFeatureRelatedEvent(String path, JsonNode change, JsonNode thingsResponse, EapEventType eventType) {
		Pattern pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)\\/features\\/(?<feature>[^\\/]+)");
		Matcher matcher = pattern.matcher(path);
		if (matcher.matches()) {
			String item = matcher.group("item");
			String feature = matcher.group("feature");
			String thingId = thingsResponse.get("items").get(Integer.parseInt(item)).get("thingId").asText();
			String featureValue = change.toString();

			logger.info("item: " + item);
			logger.info("thingId: " + thingId);
			logger.info("feature: " + feature);
			logger.info("featureValue: " + featureValue);
			logger.info("Feature added!");

			Map<String, Serializable> eventValues = new HashMap<>();
			eventValues.put("thingId", thingId);
			eventValues.put("feature", feature);
			eventValues.put("featureValue", featureValue);
			eventsToSend.add(new EapEvent(eventType, new Date(), eventValues));
		}
	}

	private void addPropertyRelatedEvent(String path, JsonNode change, JsonNode thingsResponse, EapEventType eventType) {
		Pattern pattern = Pattern.compile("\\/items\\/(?<item>[0-9]+)\\/features\\/(?<feature>[^\\/]+)\\/properties\\/(?<property>[^\\/]+)");
		Matcher matcher = pattern.matcher(path);
		if (matcher.matches()) {
			String item = matcher.group("item");
			String feature = matcher.group("feature");
			String property = matcher.group("property");
			String thingId = thingsResponse.get("items").get(Integer.parseInt(item)).get("thingId").asText();
			String propertyValue = change.asText();

			logger.info("item: " + item);
			logger.info("thingId: " + thingId);
			logger.info("feature: " + feature);
			logger.info("property: " + property);
			logger.info("propertyValue: " + propertyValue);
			logger.info("Property added!");

			Map<String, Serializable> eventValues = new HashMap<>();
			eventValues.put("thingId", thingId);
			eventValues.put("feature", feature);
			eventValues.put("property", property);
			eventValues.put("propertyValue", propertyValue);
			eventsToSend.add(new EapEvent(eventType, new Date(), eventValues));
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
				response = mapper.readTree(responseBody);

			} else {
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
