package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.replayer.EventReplayer;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.text.DateFormat;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Can generate events and push them to replayer.
 */
public class EventGenerator {

    private int eventCount;
    private int replayScaleFactor = 10000;
    private static Random random = new Random();
    private static final Logger logger = Logger.getLogger(EventGenerator.class);
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

    /**
     * Creates a new EventGenerator
     */
    public EventGenerator() {

    }
    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them
     *
     * @param eventCount
     * @param eventType
     * @param attributeSchemas
     */
    public void generateEvents(int eventCount, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        generateEvents(eventCount, this.replayScaleFactor, eventType, attributeSchemas, getCurrentTimestamp());
    }
    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor
     *
     * @param eventCount
     * @param scaleFactor
     * @param eventType
     * @param attributeSchemas
     */
    public void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        generateEvents(eventCount, scaleFactor, eventType, attributeSchemas, getCurrentTimestamp());
    }

    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor and given timestamp
     *
     * @param eventCount
     * @param scaleFactor
     * @param eventType
     * @param attributeSchemas
     * @param eventTimestamps
     */
    public void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas, String
            eventTimestamps) {
        this.eventCount = eventCount;
        this.replayScaleFactor = scaleFactor;
        List<EapEvent> events = new ArrayList<EapEvent>();

        if (eventCount < 0 || scaleFactor < 0 || EapEventType.findByTypeName(eventType.getTypeName()) == null) {
            throw new IllegalArgumentException();
        }

        for (int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<String, Serializable>();
            for (Map.Entry<TypeTreeNode, String> attributeSchema : attributeSchemas.entrySet()) {
                switch (attributeSchema.getKey().getType()) {
                    case STRING:
                        values.put(attributeSchema.getKey().getName(), getRandomStringFromInput(attributeSchema.getValue()));
                        break;
                    case INTEGER:
                        values.put(attributeSchema.getKey().getName(), getRandomIntFromInput(attributeSchema.getValue()));
                        break;
                    case FLOAT:
                        values.put(attributeSchema.getKey().getName(), getRandomFloatFromInput(attributeSchema.getValue()));
                        break;
                    case DATE:
                        values.put(attributeSchema.getKey().getName(), getRandomDateFromInput(attributeSchema.getValue()));
                        break;
                    default:
                        values.put(attributeSchema.getKey().getName(), "UNDEFINED");
                        break;
                }
            }
            EapEvent event = new EapEvent(eventType, getRandomDateFromInput(eventTimestamps), values);
            events.add(event);
        }
        EventReplayer eventReplayer = new EventReplayer(events, this.replayScaleFactor);
        eventReplayer.replay();
    }

    /**
     * Find range in input and select random date from this range.
     *
     * @param input
     */
    private Date getRandomDateFromInput(String input) {
        Date start = new Date();
        Date end = new Date();
        long timestamp;
        Date date = new Date();

        if(input.contains("-")) {
            try {
                start = dateFormatter.parse(input.split("-")[0]);
                end = dateFormatter.parse(input.split("-")[1]);
            } catch (ParseException e) { e.printStackTrace(); }
            timestamp = ThreadLocalRandom.current().nextLong(start.getTime(), end.getTime());
            date = new Date(timestamp);
        }
        else {
            try {
                date = dateFormatter.parse(input);
            } catch (ParseException e) { e.printStackTrace(); }
        }
        return date;
    }

    private String getCurrentTimestamp() {
        return dateFormatter.format(new Date());
    }

    /**
     * Select random String from given list of Strings.
     *
     * @param input
     */
    private static String getRandomStringFromInput(String input) {
        String[] possibleValues = input.split(";");
        return possibleValues[getRandomIndex(possibleValues)];
    }

    /**
     * Find range in input and select random integer from this range.
     *
     * @param input
     */
    private static int getRandomIntFromInput(String input) {
        if(input.contains("-")) {
            int start = Integer.parseInt(input.split("-")[0]);
            int end = Integer.parseInt(input.split("-")[1]);
            return random.nextInt(end - start + 1) + start;
        }
        else {
            String[] possibleValues = input.split(";");
            return Integer.parseInt(possibleValues[getRandomIndex(possibleValues)]);
        }
    }

    /**
     * Select random Float from given list of Floats.
     *
     * @param input
     */
    private static Float getRandomFloatFromInput(String input) {
        String[] possibleValues = input.split(";");
        return Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
    }

    private static int getRandomIndex(Object[] inputArray) {
        return random.nextInt(inputArray.length);
    }
}