package de.hpi.unicorn.application.pages.input.replayer;


import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

import de.hpi.unicorn.persistence.Persistor;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class EventGeneratorTest extends TestCase {
    private EventGenerator generator = new EventGenerator();

    @Before
    public void setup() {
        Persistor.useTestEnviroment();
        EapEvent.removeAll();
        EapEventType.removeAll();
    }

    @Test
    public void testGenerateEventExceptions() {
        int eventCount = 2;
        int scaleFactor = 1000000;
        TypeTreeNode attribute = new TypeTreeNode("TestAttribute");
        AttributeTypeTree attributeTree = new AttributeTypeTree(attribute);
        EapEventType eventType = new EapEventType("TestType", attributeTree);
        HashMap<TypeTreeNode, String> attributeSchemas = new HashMap<>();
        attributeSchemas.put(attribute, "AttributeValue");

        setup();

        try {
            generator.generateEvents(-1, scaleFactor, eventType, attributeSchemas);
            fail("Event count should not be allowed to be negative!");
        } catch (IllegalArgumentException expected) {
        }

        try {
            generator.generateEvents(eventCount, -1, eventType, attributeSchemas);
            fail("Scale factor should not be allowed to be negative!");
        } catch (IllegalArgumentException expected) {
        }

        try {
            generator.generateEvents(eventCount, scaleFactor, eventType, attributeSchemas);
            fail("Event Type should not be allowed to not be defined!");
        } catch (IllegalArgumentException expected) {
        }
    }

    public void testGenerateEvents() {
        int eventCount = 2;
        int scaleFactor = 1000000;
        String attributeName = "TestAttribute";
        String attributeValue = "AttributeValue";
        TypeTreeNode attribute = new TypeTreeNode(attributeName);
        AttributeTypeTree attributeTree = new AttributeTypeTree(attribute);
        EapEventType eventType = new EapEventType("TestType", attributeTree);
        HashMap<TypeTreeNode, String> attributeSchemas = new HashMap<>();
        attributeSchemas.put(attribute, attributeValue);

        setup();

        ArrayList<EapEventType> eventTypes = new ArrayList<EapEventType>();
        eventTypes.add(eventType);
        EapEventType.save(eventTypes);

        int globalEventCountBefore = EapEvent.findAll().size();

        generator.generateEvents(eventCount, scaleFactor, eventType, attributeSchemas);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        int globalEventCountAfter = EapEvent.findAll().size();

        assertEquals(globalEventCountBefore + eventCount, globalEventCountAfter);

        // events have correct event type
        List<EapEvent> events = EapEvent.findByEventType(eventType);
        assertEquals(eventCount, events.size());

        // event attributes have correct values
        for (EapEvent event : events) {
            for(Map.Entry<String, Serializable> entry : event.getValues().entrySet()) {
                assertEquals(attributeName, entry.getKey());
                assertEquals(attributeValue, entry.getValue());
            }
        }

        // TODO Test other methods, ranges etc
    }
}
