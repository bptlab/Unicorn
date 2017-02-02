package de.hpi.unicorn.application.pages.input.replayer;

import com.espertech.esper.client.EventType;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

import javax.swing.event.DocumentEvent;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.text.DateFormat;
import java.util.concurrent.ThreadLocalRandom;

public class EventGenerator {

    private int eventCount;
    private int replayScaleFactor = 10000;
    private static Random random = new Random();
    private static final Logger logger = Logger.getLogger(EventGenerator.class);

    public EventGenerator() {

    }

    public void generateEvents(int eventCount, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        //TODO: MAKE IT GENERIC
        generateEvents(eventCount, this.replayScaleFactor, eventType, attributeSchemas, "2017/02/01T12:00");
    }

    public void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        //TODO: MAKE IT GENERIC
        generateEvents(eventCount, scaleFactor, eventType, attributeSchemas, "2017/02/01T12:00");
    }


    public void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas, String
            eventTimestamps) {
        this.eventCount = eventCount;
        this.replayScaleFactor = scaleFactor;
        List<EapEvent> events = new ArrayList<EapEvent>();

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

    private Date getRandomDateFromInput(String input) {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");
        Date start = new Date();
        Date end = new Date();
        long timestamp;
        Date date = new Date();


        if(input.contains("-")) {
            try {
                start = formatter.parse(input.split("-")[0]);
                end = formatter.parse(input.split("-")[1]);
            } catch (ParseException e) { e.printStackTrace(); }
            timestamp = ThreadLocalRandom.current().nextLong(start.getTime(), end.getTime());
            date = new Date(timestamp);
        }
        else {
            try {
                date = formatter.parse(input);
            } catch (ParseException e) { e.printStackTrace(); }
        }
        return date;
    }

    private static String getRandomStringFromInput(String input) {
        String[] possibleValues = input.split(";");
        return possibleValues[getRandomIndex(possibleValues)];
    }

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

    private static Float getRandomFloatFromInput(String input) {
        String[] possibleValues = input.split(";");
        return Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
    }

    private static int getRandomIndex(Object[] inputArray) {
        return random.nextInt(inputArray.length);
    }

    private static Date getRandom(Date[] examples) {
        int element = random.nextInt(examples.length);
        return examples[element];
    }

    private static String getRandom(String[] examples) {
        int element = random.nextInt(examples.length);
        return examples[element];
    }

    private static int getRandom(int[] examples) {
        int element = random.nextInt(examples.length );
        return examples[element];
    }

    private static long getRandom(long[] examples) {
        int element = random.nextInt(examples.length );
        return examples[element];
    }

    private static double getRandom(double[] examples) {
        int element = random.nextInt(examples.length);
        return examples[element];
    }
}