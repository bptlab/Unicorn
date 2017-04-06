/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.replayer.EventReplayer;
import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Can generate events and push them to replayer.
 */
class EventGenerator {

    private static final int DEFAULT_REPLAY_SCALEFACTOR = 1000;
    private int replayScaleFactor = DEFAULT_REPLAY_SCALEFACTOR;
    private static Random random = new Random();
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");
    private static final Logger logger = Logger.getLogger(EventGenerator.class);

    private static EnumMap<AttributeTypeEnum, String> defaultValues = new EnumMap<>(AttributeTypeEnum.class);
    static {
        defaultValues.put(AttributeTypeEnum.STRING, "String1;String2;String3");
        defaultValues.put(AttributeTypeEnum.INTEGER, "1-50");
        defaultValues.put(AttributeTypeEnum.FLOAT, "1.1;1.2;2.0;2.5");
        defaultValues.put(AttributeTypeEnum.DATE, "2017/01/22T12:00-2017/02/23T14:59");
    }

    private AttributeDependencyManager attributeDependencyManager;

    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them.
     *
     * @param eventCount number of events to be generated
     * @param eventType type the generated events should be of
     * @param attributeSchemas map containing all eventType-attributes and their user-defined, possible values
     */
    void generateEvents(int eventCount, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        generateEvents(eventCount, this.replayScaleFactor, eventType, attributeSchemas, getCurrentTimestamp());
    }
    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor.
     *
     * @param eventCount number of events to be generated
     * @param scaleFactor time-scale for the replayer to generate the events
     * @param eventType type the generated events should be of
     * @param attributeSchemas map containing all eventType-attributes and their user-defined, possible value
     */
    void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas) {
        generateEvents(eventCount, scaleFactor, eventType, attributeSchemas, getCurrentTimestamp());
    }

    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor and given timestamp.
     *
     * @param eventCount number of events to be generated
     * @param scaleFactor time-scale for the replayer to generate the events
     * @param eventType type the generated events should be of
     * @param attributeSchemas map containing all eventType-attributes and their user-defined, possible value
     * @param eventTimestamps timestamps for the events to be generated with
     */
    void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, Map<TypeTreeNode, String> attributeSchemas, String
            eventTimestamps) {
        this.replayScaleFactor = scaleFactor;
        List<EapEvent> events = new ArrayList<>();

        if (eventCount < 0 || scaleFactor < 0 || EapEventType.findByTypeName(eventType.getTypeName()) == null) {
            throw new IllegalArgumentException();
        }

        attributeDependencyManager = new AttributeDependencyManager(eventType);


        for (int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<>();
            for (Map.Entry<TypeTreeNode, String> attributeSchema : attributeSchemas.entrySet()) {
                // Assign a random value only if the attribute hasn't been filled before (by a dependency). So we don't overwrite values defined by
                // dependencies. In case the base attribute is considered later, it will then overwrite the random value with the dependency defined
                // one.
                if (values.get(attributeSchema.getKey().getName()) == null) {
                    //Set a random value for the current attribute
                    setRandomValueFromRangeForAttribute(attributeSchema.getKey(), attributeSchema.getValue(), values);
                }
                // Check if current attribute is a base attribute in a dependency and set dependent attribute values
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
     * attributes.
     *
     * @param baseAttribute the attribute to be handled as a base attribute
     * @param eventValues a map containing already set values, will set the new values in here too. (Value for base attribute has to be set already!)
     */
    private void tryToFillDependentAttributes(TypeTreeNode baseAttribute, Map<String, Serializable> eventValues) {
        if (!attributeDependencyManager.isBaseAttributeInAnyDependency(baseAttribute)) {
            return;
        }

        String baseAttributeInput;
        if (baseAttribute.getType() == AttributeTypeEnum.DATE) {
            baseAttributeInput = dateFormatter.format(eventValues.get(baseAttribute.getName()));
        }
        else {
            baseAttributeInput = String.valueOf(eventValues.get(baseAttribute.getName()));
        }

        for (AttributeDependency attributeDependency : attributeDependencyManager.getAttributeDependencies(baseAttribute)) {
            TypeTreeNode dependentAttribute = attributeDependency.getDependentAttribute();
            List<String> possibleDependentValues = new ArrayList<>();
            for (AttributeValueDependency attributeValueDependency
                    : attributeDependencyManager.getAttributeValueDependencies(attributeDependency)) {
                if (isInRange(baseAttributeInput, attributeValueDependency.getBaseAttributeValue(), baseAttribute.getType())) {
                    possibleDependentValues.add(attributeValueDependency.getDependentAttributeValues());
                }
            }
            if (!possibleDependentValues.isEmpty()) {
                String possibleDependentValue = possibleDependentValues.get(getRandomIndex(possibleDependentValues));
                setRandomValueFromRangeForAttribute(dependentAttribute, possibleDependentValue, eventValues);
                tryToFillDependentAttributes(dependentAttribute, eventValues);
            }
        }
    }

    /**
     * Chooses a random value from the given values and sets it in the given map for the given attribute.
     *
     * @param attribute the attribute a value should be set for
     * @param proposedPossibleValues a string containing the possible values which can be extracted by the
     *                               "getRandom[attributeType]FromInput"-function
     * @param eventValues the map containing already set values will be altered by reference
     */
    private void setRandomValueFromRangeForAttribute(TypeTreeNode attribute, String proposedPossibleValues, Map<String, Serializable> eventValues) {
        String possibleValues = proposedPossibleValues;

        if (possibleValues == null || possibleValues.isEmpty()) {
            possibleValues = defaultValues.get(attribute.getType());
        }

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
     * Returns the value of a "isInRange" function corresponding to the given type.
     * This "class of functions" in general return a boolean indicating if the given input lies within a given range.
     *
     * @param input the input value to be tested to be in a range
     * @param range the range the input value should be in
     * @param type the type of the range (corresponds to the attribute type the value is conceived for)
     * @return a boolean indicating if the input is within the range
     */
    private boolean isInRange(String input, String range, AttributeTypeEnum type) {
        switch (type) {
            case STRING:
                return isInStringRange(input, range);
            case INTEGER:
                return isInIntegerRange(input, range);
            case FLOAT:
                return isInFloatRange(input, range);
            case DATE:
                return isInDateRange(input, range);
            default:
                return false;
        }
    }

    /**
     * Implements the "isInRange" function for a string range.
     *
     * @param input to be checked whether in range
     * @param range to be searched in
     * @return bool if string is in range
     */
    private boolean isInStringRange(String input, String range) {
        String[] possibleValues = range.split(";");
        for (String possibleValue : possibleValues) {
            if (possibleValue.equals(input)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Implements the "isInRange" function for an integer range.
     *
     * @param input to be checked whether in range
     * @param range to be searched in
     * @return bool if int is in range
     */
    private boolean isInIntegerRange(String input, String range) {
        int formattedInput = Integer.parseInt(input);
        if (range.contains("-")) {
            int start = Integer.parseInt(range.split("-")[0]);
            int end = Integer.parseInt(range.split("-")[1]);
            return (formattedInput >= start) && (formattedInput <= end);
        }
        else {
            return isInStringRange(input, range);
        }
    }

    /**
     * Implements the "isInRange" function for a float range.
     * Defaults to the "isInRange" function for strings as they currently use the same schema.
     *
     * @param input to be checked whether in range
     * @param range to be searched in
     * @return bool if float is in range
     */
    private boolean isInFloatRange(String input, String range) {
        return isInStringRange(input, range);
    }

    /**
     * Implements the "isInRange" function for a date range.
     *
     * @param input to be checked whether in range
     * @param range to be searched in
     * @return bool if date is in range
     */
    private boolean isInDateRange(String input, String range) {
        Date start;
        Date end;
        Date inputDate = new Date();

        try {
            inputDate = dateFormatter.parse(input);
        }
        catch (ParseException e) {
            logger.debug("DateInRange: Parse input", e);
            return false;
        }

        if (range.contains("-")) {
            try {
                start = dateFormatter.parse(range.split("-")[0]);
                end = dateFormatter.parse(range.split("-")[1]);
            } catch (ParseException e) {
                logger.debug("DateInRange: Parse range", e);
                return false;
            }
            return (inputDate.compareTo(start) >= 0) && (inputDate.compareTo(end) <= 0);
        }
        else {
            try {
                start = dateFormatter.parse(range);
            } catch (ParseException e) {
                logger.debug("DateInRange: Parse single date", e);
                return false;
            }
            return inputDate.equals(start);
        }
    }

    /**
     * Find range in input and select random date from this range.
     *
     * @param input a range where a date should be picked from
     * @return a date selected from input range
     */
    private Date getRandomDateFromInput(String input) {
        Date start = new Date();
        Date end = new Date();
        long timestamp;
        Date date = new Date();

        if (input.contains("-")) {
            try {
                start = dateFormatter.parse(input.split("-")[0]);
                end = dateFormatter.parse(input.split("-")[1]);
            } catch (ParseException e) { logger.debug("Random Date from input", e); }
            timestamp = ThreadLocalRandom.current().nextLong(start.getTime(), end.getTime());
            date = new Date(timestamp);
        }
        else {
            try {
                date = dateFormatter.parse(input);
            } catch (ParseException e) { logger.debug("Random Date from input", e); }
        }
        return date;
    }

    /**
     * Formats the current time, to fit used date-format and returns it as string.
     *
     * @return the current timestamp formatted as string
     */
    private String getCurrentTimestamp() {
        return dateFormatter.format(new Date());
    }

    /**
     * Select random String from given list of Strings.
     *
     * @param input a list of possible strings, separated by ';'
     * @return a string randomly selected from given list
     */
    private static String getRandomStringFromInput(String input) {
        String[] possibleValues = input.split(";");
        return possibleValues[getRandomIndex(possibleValues)];
    }

    /**
     * Find range in input and select random integer from this range.
     *
     * @param input a range or list of possible numbers
     * @return a random int selected from input range
     */
    private static int getRandomIntFromInput(String input) {
        if (input.contains("-")) {
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
     * @param input a list of possible floats
     * @return a float randomly selected from input list
     */
    private static Float getRandomFloatFromInput(String input) {
        String[] possibleValues = input.split(";");
        return Float.parseFloat(possibleValues[getRandomIndex(possibleValues)]);
    }

    /**
     * Randomly selects an index from a given array.
     *
     * @param inputArray the array the random index should 'calculated' for
     * @return a int containing a random index for the given array
     */
    private static int getRandomIndex(Object[] inputArray) {
        return random.nextInt(inputArray.length);
    }

    /**
     * Randomly selects an index from a given list.
     *
     * @param inputList the list the random index should 'calculated' for
     * @return a int containing a random index for the given list
     */
    private static int getRandomIndex(List inputList) { return random.nextInt(inputList.size()); }
}
