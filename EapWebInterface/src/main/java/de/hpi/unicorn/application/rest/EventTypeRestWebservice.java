package de.hpi.unicorn.application.rest;

import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.event.EapEventType;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.google.gson.*;
import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.UnparsableException;

/**
 * Rest interface create EapEventTypes
 */
@Path("REST")
public class EventTypeRestWebservice {

	/**
	 * Method handling HTTP GET requests for EventTypes.
	 * The requested EventType will be sent
	 * to the client as "text/plain" media type.
	 *
	 * @param schemaName The name of the requested EventType
	 * @return Schema Definition of the EventType as a text/plain response.
	 */
	@GET
	@Path("/EventType/{schemaName}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getEventType(@PathParam("schemaName") String schemaName) {
		EapEventType type = EapEventType.findBySchemaName(schemaName);
		if (type != null) {
			return Response.ok(type.getXsdString()).build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Event type could not be found.").build();
		}
	}

	/**
	 * Method handling HTTP DELETE requests for EventTypes.
	 * The requested EventType will be removed from the Database.
	 *
	 * @param schemaName The name of the requested EventType
	 */
	@DELETE
	@Path("/EventType/{schemaName}")
	@Produces(MediaType.TEXT_PLAIN)
	public Response deleteEventType(@PathParam("schemaName") String schemaName) {
		EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
		boolean success = service.unregisterEventType(schemaName);
		if (success) {
			return Response.ok().build();
		} else {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Event type could not be found or unregistered.").build();
		}
	}

	/**
	 * Method handling HTTP POST requests for EventTypes.
	 * The EventType in the POST body will be added to the Database.
	 *
	 * @param typeJson JSON string containing schemaDefinition, schemaName and timestampName
	 */
	@POST
	@Path("/EventType")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public Response createEventType(String typeJson) {
		try {
			Gson gson = new Gson();
			EventTypeJson ele = gson.fromJson(typeJson, EventTypeJson.class);
			EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
			service.registerEventType(ele.getXsd(), ele.getSchemaName(), ele.getTimestampName());
			return Response.ok().build();
		} catch (DuplicatedSchemaException | UnparsableException | JsonSyntaxException e) {
			return Response.status(Response.Status.BAD_REQUEST).type(MediaType.TEXT_PLAIN).entity("Event type could not be created: " + e.getMessage()).build();
		}
	}

	/**
	 * Class for parsing Event Type JSON documents
	 * that are received via the REST POST request
	 */
	private class EventTypeJson {
		private String xsd;
		private String schemaName;
		private String timestampName;

		public String getTimestampName() {
			return timestampName;
		}

		public void setTimestampName(String timestampName) {
			this.timestampName = timestampName;
		}

		public String getSchemaName() {
			return schemaName;
		}

		public void setSchemaName(String schemaName) {
			this.schemaName = schemaName;
		}

		public String getXsd() {
			return xsd;
		}

		public void setXsd(String xsd) {
			this.xsd = xsd;
		}
	}
}
