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
    private int eventCount = 2;
    private int scaleFactor = 1000000;
    private String attributeName = "TestAttribute";
    private String attributeValue = "AttributeValue";
    private TypeTreeNode attribute = new TypeTreeNode(attributeName);
    private AttributeTypeTree attributeTree = new AttributeTypeTree(attribute);
    private EapEventType eventType = new EapEventType("TestType", attributeTree);
    private HashMap<TypeTreeNode, String> attributeSchemas = new HashMap<>();
    private ArrayList<EapEventType> eventTypes = new ArrayList<EapEventType>();
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

    @Before
    public void setUp() {
        Persistor.useTestEnvironment();
        EapEvent.removeAll();
        EapEventType.removeAll();
        attributeSchemas.put(attribute, attributeValue);
        eventTypes.add(eventType);
    }

    public void testGenerateEventExceptions() {

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

    public void testGenerateEventsWithEmptyValues() {
        EapEventType.save(eventTypes);

        TypeTreeNode attribute2 = new TypeTreeNode("Attribute2", AttributeTypeEnum.INTEGER);
        attributeTree.addRoot(attribute2);
        attributeSchemas.put(attribute2, "");

        TypeTreeNode attribute3 = new TypeTreeNode("Attribute3", AttributeTypeEnum.FLOAT);
        attributeTree.addRoot(attribute3);
        attributeSchemas.put(attribute3, "");

        TypeTreeNode attribute4 = new TypeTreeNode("Attribute4", AttributeTypeEnum.STRING);
        attributeTree.addRoot(attribute4);
        attributeSchemas.put(attribute4, "");

        TypeTreeNode attribute5 = new TypeTreeNode("Attribute5", AttributeTypeEnum.DATE);
        attributeTree.addRoot(attribute5);
        attributeSchemas.put(attribute5, "");

        generator.generateEvents(10, scaleFactor, eventType, attributeSchemas);
        try {
            TimeUnit.SECONDS.sleep(2);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // event attributes have default values
        List<EapEvent> events = EapEvent.findByEventType(eventType);
        for (EapEvent event : events) {
            int intValue = Integer.parseInt(event.getValuesForExport().get("Attribute2"));
            assertTrue(intValue >= 1 && intValue <= 50);

            float floatValue = Float.parseFloat(event.getValuesForExport().get("Attribute3"));
            assertTrue(floatValue == 1.1f || floatValue == 1.2f || floatValue == 2.0f || floatValue == 2.5f);

            String stringValue = event.getValuesForExport().get("Attribute4");
            assertTrue(stringValue.equals("String1") || stringValue.equals("String2") || stringValue.equals("String3"));

            try {
                String dateValue = event.getValuesForExport().get("Attribute5");
                Date startDate = dateFormatter.parse("2017/01/22T12:00");
                Date endDate = dateFormatter.parse("2017/02/23T14:59");
                Date date = new Date();
                try {
                    date = DateUtils.getFormatter().parse(dateValue);
                }catch (ParseException e) {
                    fail("Date was not in a parsable format");
                }
                assertTrue(date.after(startDate) && date.before(endDate));
            } catch (ParseException e) {
                fail("Error while parsing default dates");
            }

        }
    }

}
