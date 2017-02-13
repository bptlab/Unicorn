package de.hpi.unicorn.application.rest;

import com.google.gson.Gson;
import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.notification.RestNotificationRule;
import de.hpi.unicorn.query.QueryWrapper;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Rest interface create EapEventTypes
 */
@Path("REST")
public class EventQueryRestWebservice {

	/**
	 * Method handling HTTP GET requests for EventQueries.
	 * The query is specified by the uuid of its notification rule.
	 * The QueryString will be sent to the user as "text/plain" media type.
	 *
	 * @param eventQueryUuid uuid of the NotificationRule belonging to the
	 *                       requested EventQuery
	 * @return String containing the requested EventQuery
	 */
	@GET
	@Path("/EventQuery/{eventQueryUuid}")
	@Produces(MediaType.TEXT_PLAIN)
	public String getEventQuery(@PathParam("eventQueryUuid") String eventQueryUuid) {
		try {
			final NotificationRuleForQuery notificationRule = NotificationRuleForQuery.findByUUID(eventQueryUuid);
			final QueryWrapper query = notificationRule.getQuery();
			return query.getQueryString();
		} catch (Exception e) {
			final RestNotificationRule notificationRule = RestNotificationRule.findByUUID(eventQueryUuid);
			final QueryWrapper query = notificationRule.getQuery();
			return query.getQueryString();
		}
	}

	/**
	 * Method handling HTTP DELETE requests for EventQueries.
	 * The query is specified by the uuid of its notification rule.
	 * The query and its notification rule will be removed from the database.
	 * Also, the corresponding Message Queue will be destroyed.
	 * @param eventQueryUuid uuid of the NotificationRule belonging to the
	 *                       requested EventQuery
	 */
	@DELETE
	@Path("/EventQuery/Queue/{eventQueryUuid}")
	public void deleteEventQueryWithQueue(@PathParam("eventQueryUuid") String eventQueryUuid) {
		EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
		service.unregisterQueryFromQueue(eventQueryUuid);
	}

	/**
	 * Method handling HTTP DELETE requests for EventQueries.
	 * The query is specified by the uuid of its notification rule.
	 * The query and its notification rule will be removed from the database.
	 * @param eventQueryUuid uuid of the NotificationRule belonging to the
	 *                       requested EventQuery
	 */
	@DELETE
	@Path("/EventQuery/REST/{eventQueryUuid}")
	public void deleteEventQueryWithRest(@PathParam("eventQueryUuid") String eventQueryUuid) {
		EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
		service.unregisterQueryFromRest(eventQueryUuid);
	}

	/**
	 * Method handling HTTP POST requests for EventQueries.
	 * The EventQuery in the POST body will be added to the database,
	 * and a NotificationRule will be created.
	 * @param queryJson JSON string containing title, queryString and an email
	 *                  for the NotificationRule
	 * @return the uuid of the NotificationRule
	 */
	@POST
	@Path("/EventQuery/Queue")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createEventQueryWithQueue(String queryJson) {
		Gson gson = new Gson();
		EventQueryJsonForQueue ele = gson.fromJson(queryJson, EventQueryJsonForQueue.class);
		EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
		// returns the uuid of the NotificationRule
		// or an error message if the query could not be registered
		String uuid = service.registerQueryForQueue(ele.getTitle(), ele.getQueryString(), ele.getEmail());
		if (uuid.startsWith("EPException")) {
			return Response.status(500).entity(uuid).type("text/plain").build();
		} else {
			return Response.ok(uuid).build();
		}
	}

	/**
	 * Method for handling HTTP POST registration of EventQueries.
	 * The EventQuery in the post body will be added to the database,
	 * and a notificationRule will be created.
	 *
	 * This is a one time Notification: after the notificationRule triggers,
	 * the query and the rule will be deleted.
	 * @param queryJson
	 * @return
     */
	@POST
	@Path("/EventQuery/REST")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createEventQueryWithRest(String queryJson) {
		Gson gson = new Gson();
		EventQueryJsonForRest ele = gson.fromJson(queryJson, EventQueryJsonForRest.class);
		EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
		// returns the uuid of the NotificationRule
		// or an error message if the query could not be registered
		String uuid = service.registerQueryForRest(ele.getQueryString(), ele.getNotificationPath());
		if (uuid.startsWith("EPException")) {
			return Response.status(500).entity(uuid).type("text/plain").build();
		} else {
			return Response.ok(uuid).build();
		}
	}

	/**
	 * Class for parsing Event Query JSON documents
	 * with a message queue notification
	 * that are received via the REST POST request
	 */
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
