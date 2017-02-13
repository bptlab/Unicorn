package de.hpi.unicorn.application.rest;

import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.UnparsableException;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertNotNull;

/**
 *
 */
public class EventRestWebserviceTest extends JerseyTest {
    private EventProcessingPlatformWebservice service;
    private String eventString;
    private String eventSchemaString;

    @After
    public void removeEventType() {
        try{
            service.unregisterEventType("TestEvent");
        } catch (Exception e) {
            //Do nothing
        }
    }

    @Before
    public void setup() {
        service = new EventProcessingPlatformWebservice();
        eventString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> " +
                "<cpoi xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
                "xsi:noNamespaceSchemaLocation=\"TestEvent.xsd\"> " +
                "<TestValue>1.0</TestValue> " +
                "<Timestamp>2015-09-05T20:05:32.799</Timestamp> " +
                "</cpoi>";
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
        } catch (UnparsableException e) {
            e.printStackTrace();
        } catch (DuplicatedSchemaException e) {
            // Do nothing, schema already exsits
        }

    }

    @Override
    protected Application configure() {
        return new ResourceConfig(EventRestWebservice.class);
    }

   @Test
    public void testPost() throws ParserConfigurationException, SAXException, IOException {
        String url = "REST/Event";
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.parse(new InputSource(new StringReader(eventString)));
        Response response = target(url).request().post(Entity.xml(doc));

        try {
            int eventId = response.readEntity(int.class);
            assertNotNull(EapEvent.findByID(eventId));
        } catch (Exception e) {
            assert(false);
        }
    }
}
