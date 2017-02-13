package de.hpi.unicorn.application.rest;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.gson.Gson;
import de.hpi.unicorn.EventProcessingPlatformWebservice;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.exception.DuplicatedSchemaException;
import de.hpi.unicorn.exception.UnparsableException;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.notification.RestNotificationRule;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Rule;
import org.junit.Test;
import org.w3c.dom.Document;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Application;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 *
 */
public class AutomaticNotifcationTest extends JerseyTest {

    String eventSchemaString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
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

    String eventString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?> " +
            "<cpoi xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" " +
            "xsi:noNamespaceSchemaLocation=\"TestEvent.xsd\"> " +
            "<TestValue>1.0</TestValue> " +
            "<Timestamp>2015-09-05T20:05:32.799</Timestamp> " +
            "</cpoi>";


    @Override
    protected Application configure() {
        return new ResourceConfig();
    }

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(9000);

    @Test
    public void testNotificationRule() {
        stubFor(post(urlEqualTo("/my/resource"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Some content")));

        QueryWrapper wrapper = new QueryWrapper("MyTitle", "", QueryTypeEnum.LIVE);
        String notificationPath = "http://localhost:9000/my/resource";
        RestNotificationRule rule = new RestNotificationRule(wrapper, notificationPath);
        boolean succesfulRule = rule.trigger(new HashMap<Object, Serializable>());
        System.out.println(succesfulRule);
    }

    @Test
    public void testRestNotification() throws UnparsableException, XMLParsingException, DuplicatedSchemaException {
        EventProcessingPlatformWebservice service = new EventProcessingPlatformWebservice();
        stubFor(post(urlEqualTo("/my/resource"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("Some content")));

        // Register Test Event Type
        if (EapEventType.findBySchemaName("TestEvent") != null) {
            service.unregisterEventType("TestEvent");
        }
        service.registerEventType(eventSchemaString, "TestEvent", "Timestamp");

        // Register event query with REST notification Rule
        String uuid = service.registerQueryForRest("SELECT * FROM TestEvent", "http://localhost:9000/my/resource");

        // Try to trigger the Rule by receiving an Event
        final Document doc = XMLParser.XMLStringToDoc(eventString);
        if (doc == null) {
            throw new UnparsableException(UnparsableException.ParseType.EVENT);
        }

        assertNotNull(RestNotificationRule.findByUUID(uuid));

        List<EapEvent> events = XMLParser.generateEventsFromDoc(doc);
        EapEvent newEvent = events.get(0);
        Broker.getEventImporter().importEvent(newEvent);

        // RestNotificationRules are deleted after they trigger
        // (if they trigger correctly)
        assertNull(RestNotificationRule.findByUUID(uuid));
    }


}
