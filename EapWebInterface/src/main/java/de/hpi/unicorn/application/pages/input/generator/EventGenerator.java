package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.replayer.EventReplayer;
import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

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
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

    private AttributeDependencyManager attributeDependencyManager;

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

        attributeDependencyManager = new AttributeDependencyManager(eventType);

        for (int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<String, Serializable>();
            for (Map.Entry<TypeTreeNode, String> attributeSchema : attributeSchemas.entrySet()) {
                //Check if current attribute has a dependency, if so we shouldn't touch its value as it will be filled later together with its base
                // attribute
                if(attributeDependencyManager.isDependentAttributeInDependency(attributeSchema.getKey())) {
                    continue;
                }
                //Set a value for the current attribute
                setValueForAttribute(attributeSchema.getKey(), attributeSchema.getValue(), values);
                //Check if current attribute is a base attribute in a dependency and set dependent attribute values
                tryToFillDependentAttributes(attributeSchema.getKey(), values);
            }
            EapEvent event = new EapEvent(eventType, getRandomDateFromInput(eventTimestamps), values);
            events.add(event);
        }
        EventReplayer eventReplayer = new EventReplayer(events, this.replayScaleFactor);
        eventReplayer.replay();
    }

    /**
     * If the given attribute can be found as a base attribute in a dependency, this function will set dependency-compliant values for dependent
     * attributes
     *
     * @param baseAttribute the attribute to be handled as a base attribute
     * @param eventValues a map containing already set values, will set the new values in here too. (Value for base attribute has to be set already!)
     */
    private void tryToFillDependentAttributes(TypeTreeNode baseAttribute, Map<String, Serializable> eventValues) {
        if(!attributeDependencyManager.isBaseAttributeInDependency(baseAttribute)) {
            return;
        }

        String baseAttributeInput = (String) eventValues.get(baseAttribute.getName());

        for(AttributeDependency attributeDependency : AttributeDependency.getAttributeDependenciesForAttribute(baseAttribute)) {
            TypeTreeNode dependentAttribute = attributeDependency.getDependentAttribute();
            List<String> possibleDependentValues = new ArrayList<>();
            for(AttributeValueDependency attributeValueDependency : AttributeValueDependency.getAttributeValueDependenciesForAttributeDependency
                    (attributeDependency)) {
                if(baseAttributeInput.equals(attributeValueDependency.getBaseAttributeValue())) {
                    possibleDependentValues.add(attributeValueDependency.getDependentAttributeValues());
                }
            }
            if(!possibleDependentValues.isEmpty()) {
                setValueForAttribute(dependentAttribute, possibleDependentValues.get(getRandomIndex(possibleDependentValues)), eventValues);
            }
        }
    }

    /**
     * Chooses a random value from the given values and sets it in the given map for the given attribute.
     *
     * @param attribute the attribute a value should be set for
     * @param possibleValues a string containing the possible values which can be extracted by the "getRandom[attributeType]FromInput"-function
     * @param eventValues the map containing already set values will be altered by reference
     */
    private void setValueForAttribute(TypeTreeNode attribute, String possibleValues, Map<String, Serializable> eventValues) {
        switch (attribute.getType()) {
            case STRING:
                eventValues.put(attribute.getName(), getRandomStringFromInput(possibleValues));
                break;
            case INTEGER:
                eventValues.put(attribute.getName(), getRandomIntFromInput(possibleValues));
                break;
            case FLOAT:
                eventValues.put(attribute.getName(), getRandomFloatFromInput(possibleValues));
                break;
            case DATE:
                eventValues.put(attribute.getName(), getRandomDateFromInput(possibleValues));
                break;
            default:
                eventValues.put(attribute.getName(), "UNDEFINED");
                break;
        }
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
    private static int getRandomIndex(List inputList) { return random.nextInt(inputList.size()); }
}