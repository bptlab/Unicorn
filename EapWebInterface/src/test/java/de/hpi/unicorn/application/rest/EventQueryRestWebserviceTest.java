package de.hpi.unicorn.application.rest;

import com.google.gson.Gson;
import de.hpi.unicorn.EventProcessingPlatformWebservice;

import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.notification.RestNotificationRule;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;

public class EventQueryRestWebserviceTest extends JerseyTest {
	private EventProcessingPlatformWebservice service;
	private String queryString;
	private String title;
	private String email;
	private String eventSchemaString;
	private String notificationPath;

	@Override
	protected Application configure() {
		return new ResourceConfig(EventQueryRestWebservice.class);
	}

	@Before
	public void setup() {

		service = new EventProcessingPlatformWebservice();
		queryString = "SELECT * FROM TestEvent";
		title = "TestQuery";
		email = "test@test.de";
		notificationPath = "/some/path/for/testing";
		eventSchemaString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"                <xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" xmlns=\"TestEvent.xsd\"\n" +
				"        targetNamespace=\"TestEvent.xsd\" elementFormDefault=\"qualified\">\n" +
				"        <xs:element name=\"TestEvent\">\n" +
				"        <xs:complexType>\n" +
				"        <xs:sequence>\n" +
				"        <xs:element name=\"TestValue\" type=\"xs:float\"\n" +
				"        minOccurs=\"1\" maxOccurs=\"1\" />\n" +
				"        <xs:element name=\"Timestamp\" type=\"xs:dateTime\" minOccurs=\"1\"\n" +
				"        maxOccurs=\"1\" />\n" +
				"        </xs:sequence>\n" +
				"        </xs:complexType>\n" +
				"        </xs:element>\n" +
				"        </xs:schema>";
		try {
			service.registerEventType(eventSchemaString, "TestEvent", "Timestamp");
		} catch (DuplicatedSchemaException | UnparsableException e) {
			e.printStackTrace();
		}
	}

	@After
	public void unregisterTestEvent() {
		try {
			service.unregisterEventType("TestEvent");
		} catch(NullPointerException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetQueue() {
		String uuid = service.registerQueryForQueue(title, queryString, email);

		String url = "REST/EventQuery/" + uuid;

		final String restQueryString = target(url).request().get(String.class);

		assertEquals(restQueryString, queryString);

		service.unregisterQueryFromQueue(uuid);
	}

	@Test
	public void testGetREST() {
		String uuid = service.registerQueryForRest(queryString, notificationPath);

		String url = "REST/EventQuery/" + uuid;

		final String restQueryString = target(url).request().get(String.class);

		assertEquals(restQueryString, queryString);

		service.unregisterQueryFromRest(uuid);
	}

	@Test
	public void testDeleteQueue() {

		String uuid = service.registerQueryForQueue(title, queryString, email);

		String url = "REST/EventQuery/Queue/" + uuid;

		target(url).request().delete();

		final NotificationRuleForQuery notificationRule = NotificationRuleForQuery.findByUUID(uuid);
		assertEquals(notificationRule, null);

		if(notificationRule != null) {
			service.unregisterQueryFromQueue(uuid);
		}
	}

	@Test
	public void testDeleteRest() {

		String uuid = service.registerQueryForRest(queryString, notificationPath);

		String url = "REST/EventQuery/REST/" + uuid;

		target(url).request().delete();

		final NotificationRuleForQuery notificationRule = NotificationRuleForQuery.findByUUID(uuid);
		assertEquals(notificationRule, null);

		if(notificationRule != null) {
			service.unregisterQueryFromRest(uuid);
		}
	}

	@Test
	public void testPostQueue() {

		EventQueryJsonForQueue json = new EventQueryJsonForQueue();
		json.setTitle(title);
		json.setQueryString(queryString);
		json.setEmail(email);

		Gson gson = new Gson();
		String postJson = gson.toJson(json);

		String url = "REST/EventQuery/Queue";

		Response response = target(url).request().post(Entity.json(postJson));
		String postUuid = response.readEntity(String.class);

		final NotificationRuleForQuery notificationRule = NotificationRuleForQuery.findByUUID(postUuid);
		String restQueryString;
		try {
			restQueryString = notificationRule.getQuery().getQueryString();
		} catch(NullPointerException e) {
			restQueryString = "";
		}
		assertEquals(restQueryString, queryString);

		service.unregisterQueryFromQueue(postUuid);
	}

	@Test
	public void testPostRest() {

		EventQueryJsonForRest json = new EventQueryJsonForRest();
		json.setQueryString(queryString);
		json.setNotificationPath(notificationPath);

		Gson gson = new Gson();
		String postJson = gson.toJson(json);

		String url = "REST/EventQuery/REST";

		Response response = target(url).request().post(Entity.json(postJson));
		String postUuid = response.readEntity(String.class);

		final RestNotificationRule notificationRule = RestNotificationRule.findByUUID(postUuid);
		String restQueryString;
		try {
			restQueryString = notificationRule.getQuery().getQueryString();
		} catch(NullPointerException e) {
			restQueryString = "";
		}
		assertEquals(restQueryString, queryString);

		service.unregisterQueryFromRest(postUuid);
	}

	private class EventQueryJsonForQueue {
		private String title;
		private String queryString;
		private String email;

		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getQueryString() {
			return queryString;
		}

		public void setQueryString(String queryString) {
			this.queryString = queryString;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}
	}

	/**
	 * Class for parsing Event Query JSON documents
	 * with a REST notification
	 * that are received via the REST POST request
	 */
	private class EventQueryJsonForRest {
		private String notificationPath;
		private String queryString;

		public String getQueryString() {
			return queryString;
		}

		public void setQueryString(String queryString) {
			this.queryString = queryString;
		}

		public String getNotificationPath() {
			return notificationPath;
		}

		public void setNotificationPath(String notificationPath) {
			this.notificationPath = notificationPath;
		}
	}
}
