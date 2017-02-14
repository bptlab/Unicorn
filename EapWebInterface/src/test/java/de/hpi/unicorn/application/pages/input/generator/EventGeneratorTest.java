package de.hpi.unicorn.application.pages.input.generator;


import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

import de.hpi.unicorn.persistence.Persistor;
import de.hpi.unicorn.utils.DateUtils;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class EventGeneratorTest extends TestCase {
    private EventGenerator generator = new EventGenerator();
    int eventCount = 2;
    int scaleFactor = 1000000;
    String attributeName = "TestAttribute";
    String attributeValue = "AttributeValue";
    TypeTreeNode attribute = new TypeTreeNode(attributeName);
    AttributeTypeTree attributeTree = new AttributeTypeTree(attribute);
    EapEventType eventType = new EapEventType("TestType", attributeTree);
    HashMap<TypeTreeNode, String> attributeSchemas = new HashMap<>();
    ArrayList<EapEventType> eventTypes = new ArrayList<EapEventType>();
    DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

    @Before
    public void setup() {
        Persistor.useTestEnviroment();
        EapEvent.removeAll();
        EapEventType.removeAll();
        attributeSchemas.put(attribute, attributeValue);
        eventTypes.add(eventType);
    }

    @Test
    public void testGenerateEventExceptions() {
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
        setup();
        EapEventType.save(eventTypes);

        int globalEventCountBefore = EapEvent.findAll().size();

        generator.generateEvents(eventCount, eventType, attributeSchemas);
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
    }

    public void testGenerateEventsWithScaleFactor() {
        setup();
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
    }

    public void testGenerateEventsWithTimestamp() {
        String timestampString = "2017/02/10T10:14";
        Date date = new Date();

        try {
            date = dateFormatter.parse(timestampString);
        } catch (Exception e) {
            e.printStackTrace();
        }

        setup();
        EapEventType.save(eventTypes);

        int globalEventCountBefore = EapEvent.findAll().size();

        generator.generateEvents(eventCount, scaleFactor, eventType, attributeSchemas, timestampString);
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
            assertEquals(date, event.getTimestamp());
            for (Map.Entry<String, Serializable> entry : event.getValues().entrySet()) {
                assertEquals(attributeName, entry.getKey());
                assertEquals(attributeValue, entry.getValue());
            }
        }
    }

    public void testGenerateEventsRange() {
        setup();
        EapEventType.save(eventTypes);

        TypeTreeNode attribute2 = new TypeTreeNode("Attribute2", AttributeTypeEnum.INTEGER);
        attributeTree.addRoot(attribute2);
        attributeSchemas.put(attribute2, "1-3");

        TypeTreeNode attribute3 = new TypeTreeNode("Attribute3", AttributeTypeEnum.DATE);
        attributeTree.addRoot(attribute3);
        Date startDate = new Date();
        Date endDate = new Date();
        try {
            startDate = dateFormatter.parse("2017/02/10T12:00");
            endDate = dateFormatter.parse("2017/02/11T12:00");
        } catch (ParseException e) { e.printStackTrace(); }
        attributeSchemas.put(attribute3, "2017/02/10T12:00-2017/02/11T12:00");


        generator.generateEvents(eventCount, scaleFactor, eventType, attributeSchemas);
        try {
            TimeUnit.SECONDS.sleep(3);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // event attributes have correct values
        List<EapEvent> events = EapEvent.findByEventType(eventType);
        for (EapEvent event : events) {
            int intValue = Integer.parseInt(event.getValuesForExport().get("Attribute2"));
            assertTrue(intValue == 1 || intValue == 2 || intValue == 3);
            String dateValue = event.getValuesForExport().get("Attribute3");
            Date date = new Date();
            try {
                date = DateUtils.getFormatter().parse(dateValue);
            }catch (Exception e) { e.printStackTrace(); }
            assertTrue(date.after(startDate) && date.before(endDate));
        }
    }

    public void testGenerateEventsEnumeration() {
        setup();
        EapEventType.save(eventTypes);

        TypeTreeNode attribute2 = new TypeTreeNode("Attribute2", AttributeTypeEnum.INTEGER);
        attributeTree.addRoot(attribute2);
        attributeSchemas.put(attribute2, "1;3;4");

        TypeTreeNode attribute3 = new TypeTreeNode("Attribute3", AttributeTypeEnum.FLOAT);
        attributeTree.addRoot(attribute3);
        attributeSchemas.put(attribute3, "1.1;34.67");

        TypeTreeNode attribute4 = new TypeTreeNode("Attribute4", AttributeTypeEnum.STRING);
        attributeTree.addRoot(attribute4);
        attributeSchemas.put(attribute4, "String1;String2");

        generator.generateEvents(eventCount, scaleFactor, eventType, attributeSchemas);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // event attributes have correct values
        List<EapEvent> events = EapEvent.findByEventType(eventType);
        for (EapEvent event : events) {
            int intValue = Integer.parseInt(event.getValuesForExport().get("Attribute2"));
            assertTrue(intValue == 1 || intValue == 3 || intValue == 4);

            float floatValue = Float.parseFloat(event.getValuesForExport().get("Attribute3"));
            assertTrue(floatValue == 1.1f || floatValue == 34.67f);

            String stringValue = event.getValuesForExport().get("Attribute4");
            assertTrue(stringValue.equals("String1") || stringValue.equals("String2"));
        }
    }
}
