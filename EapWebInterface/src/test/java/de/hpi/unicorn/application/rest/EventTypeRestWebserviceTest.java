package de.hpi.unicorn.application.rest;

import com.google.gson.Gson;
import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.EventTypeNotFoundException;
import de.hpi.unicorn.exception.UnparsableException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;

import static org.junit.Assert.*;

public class EventTypeRestWebserviceTest extends JerseyTest{

    private EventProcessingPlatformWebservice service;
    private String eventSchemaString;
    @Before
    public void setup() {
        service = new EventProcessingPlatformWebservice();
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

    }



    @Override
    protected Application configure() {
        return new ResourceConfig(EventTypeRestWebservice.class);
    }

    @Test
    public void testGet() {

        String url = "REST/EventType/TestEvent";

        try {
            service.registerEventType(eventSchemaString, "TestEvent", "Timestamp");
        } catch (DuplicatedSchemaException | UnparsableException e) {
            e.printStackTrace();
        }

        final String restXsd = target(url).request().get(String.class);

        assertEquals(restXsd, eventSchemaString);

        service.unregisterEventType("TestEvent");
    }

    @Test
    public void testDelete() {


        String url = "REST/EventType/TestEvent";
        try {
            service.registerEventType(eventSchemaString, "TestEvent", "Timestamp");
        } catch (DuplicatedSchemaException | UnparsableException e) {
            e.printStackTrace();
        }
        target(url).request().delete();
        EapEventType deleteEvent = EapEventType.findBySchemaName("TestEvent");
        assertEquals(deleteEvent, null);
        if (deleteEvent != null) {
            service.unregisterEventType("TestEvent");
        }
    }

    @Test
    public void testPost() {
        String restXsd = "";

        EventTypeJson json = new EventTypeJson();
        json.setXsd(eventSchemaString);
        json.setSchemaName("TestEvent");
        json.setTimestampName("Timestamp");

        Gson gson = new Gson();
        String postJson = gson.toJson(json);

        String url = "REST/EventType";

        target(url).request().post(Entity.json(postJson));

        try {
            restXsd = service.getEventTypeXSD("TestEvent");
        } catch (EventTypeNotFoundException e) {
            e.printStackTrace();
        }

        assertEquals(eventSchemaString, restXsd);
        service.unregisterEventType("TestEvent");
    }

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
