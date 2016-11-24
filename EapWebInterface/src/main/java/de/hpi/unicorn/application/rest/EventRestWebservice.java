package de.hpi.unicorn.application.rest;

import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import org.w3c.dom.Document;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 *
 */
@Path("REST")
public class EventRestWebservice {
	/**
	 * This method allows registering events via REST.
	 *
	 * @param eventXmlDoc Xml representation of the event
	 * @return Response containing the ID of the newly registered event
	 */
	@POST
	@Path("/Event")
	@Consumes(MediaType.APPLICATION_XML)
	@Produces(MediaType.TEXT_PLAIN)
	public Response postEvent(Document eventXmlDoc) {
		try {
			if (eventXmlDoc == null) {
				throw new UnparsableException(UnparsableException.ParseType.EVENT);
			}
			List<EapEvent> events = XMLParser.generateEventsFromDoc(eventXmlDoc);
			EapEvent newEvent = events.get(0);
			Broker.getEventImporter().importEvent(newEvent);

			return Response.ok(newEvent.getID()).build();
		} catch (UnparsableException | XMLParsingException e) {
			return Response.status(Response.Status.BAD_REQUEST).entity("Event xml could not be parsed: " + e.getMessage()).build();
		}
	}
}
