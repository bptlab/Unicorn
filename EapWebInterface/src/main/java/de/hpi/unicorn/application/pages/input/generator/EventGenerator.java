/*******************************************************************************
 *
 * Copyright (c) 2012-2017, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.generator.attributeInput.AttributeInput;
import de.hpi.unicorn.application.pages.input.generator.attributeInput.DateAttributeInput;
import de.hpi.unicorn.application.pages.input.replayer.EventReplayer;
import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Can generate events and push them to replayer.
 */
class EventGenerator {

    private static final int DEFAULT_REPLAY_SCALEFACTOR = 1000;
    private int replayScaleFactor = DEFAULT_REPLAY_SCALEFACTOR;
    private static final DateFormat dateFormatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");

    private AttributeDependencyManager attributeDependencyManager;

    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them.
     *
     * @param eventCount number of events to be generated
     * @param eventType type the generated events should be of
     * @param attributeUserInput list of all AttributeInputs containing the user input
     */
    void generateEvents(int eventCount, EapEventType eventType, List<AttributeInput> attributeUserInput) {
        generateEvents(eventCount, this.replayScaleFactor, eventType, attributeUserInput, getCurrentTimestamp());
    }
    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor.
     *
     * @param eventCount number of events to be generated
     * @param scaleFactor time-scale for the replayer to generate the events
     * @param eventType type the generated events should be of
     * @param attributeUserInput list of all AttributeInputs containing the user input
     */
    void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, List<AttributeInput> attributeUserInput) {
        generateEvents(eventCount, scaleFactor, eventType, attributeUserInput, getCurrentTimestamp());
    }

    /**
     * Generates (eventCount many) events with given event type and random selection of input values for attribute values
     * and replays them with given scale factor and given timestamp.
     *
     * @param eventCount number of events to be generated
     * @param scaleFactor time-scale for the replayer to generate the events
     * @param eventType type the generated events should be of
     * @param eventTimestamps timestamps for the events to be generated with
     * @param attributeUserInput list of all AttributeInputs containing the user input
     */
    void generateEvents(int eventCount, int scaleFactor, EapEventType eventType, List<AttributeInput> attributeUserInput, String
            eventTimestamps) {
        this.replayScaleFactor = scaleFactor;
        List<EapEvent> events = new ArrayList<>();

        if (eventCount < 0 || scaleFactor < 0 || EapEventType.findByTypeName(eventType.getTypeName()) == null) {
            throw new IllegalArgumentException();
        }

        attributeDependencyManager = new AttributeDependencyManager(eventType);

        for (int j = 0; j < eventCount; j++) {
            Map<String, Serializable> values = new HashMap<>();
            for (AttributeInput attributeInput : attributeUserInput) {
                // Assign a random value only if the attribute hasn't been filled before (by a dependency). So we don't overwrite values defined by
                // dependencies. In case the base attribute is considered later, it will then overwrite the random value with the dependency defined
                // one.
                if (values.get(attributeInput.getAttributeName()) == null) {
                    //Set a random value for the current attribute
                    setRandomValueFromRangeForAttribute(attributeInput, values);
                }
                // Check if current attribute is a base attribute in a dependency and set dependent attribute values
                tryToFillDependentAttributes(attributeInput, values);
            }
            AttributeInput timestampUserInput = new DateAttributeInput(null);
            timestampUserInput.setInput(eventTimestamps);
            EapEvent event = new EapEvent(eventType, (Date) timestampUserInput.getCalculatedValue(), values);
            events.add(event);
        }

        EventReplayer eventReplayer = new EventReplayer(events, this.replayScaleFactor);
        eventReplayer.replay();
    }

    /**
     * If the given attribute can be found as a base attribute in a dependency, this function will set dependency-compliant values for dependent
     * attributes.
     *
     * @param baseAttributeInput contains the concerned event type attribute and its user input
     * @param eventValues a map containing already set values, will set the new values in here too. (Value for base attribute has to be set already!)
     */
    private void tryToFillDependentAttributes(AttributeInput baseAttributeInput, Map<String, Serializable> eventValues) {
        TypeTreeNode baseAttribute = baseAttributeInput.getAttribute();
        if (!attributeDependencyManager.isBaseAttributeInAnyDependency(baseAttribute)) {
            return;
        }

        // iterate over each dependency defined for the base-attribute
        for (AttributeDependency attributeDependency : attributeDependencyManager.getAttributeDependencies(baseAttribute)) {
            TypeTreeNode dependentAttribute = attributeDependency.getDependentAttribute();
            List<AttributeInput> possibleDependentAttributeInputs = new ArrayList<>();
            // iterate over each value-dependency defined on this dependency
            for (AttributeValueDependency attributeValueDependency : attributeDependencyManager.getAttributeValueDependencies
                    (attributeDependency)) {
                // if the (already computed) value for the base attribute is within the value range of the current value-dependency-rule for the
                // base attribute (hence this dependency-rule can be applied), add the values of the corresponding dependent-attribute to the list of
                // possible values.
                if (baseAttributeInput.isInRange(attributeValueDependency.getBaseAttributeValue())) {
                    AttributeInput possibleDependentAttributeInput = AttributeInput.attributeInputFactory(dependentAttribute);
                    possibleDependentAttributeInput.setInput(attributeValueDependency.getDependentAttributeValues());
                    possibleDependentAttributeInputs.add(possibleDependentAttributeInput);
                }
            }
            if (!possibleDependentAttributeInputs.isEmpty()) {
                AttributeInput dependentAttributeInput = possibleDependentAttributeInputs.get(
                        AttributeInput.getRandomIndex(possibleDependentAttributeInputs));
                setRandomValueFromRangeForAttribute(dependentAttributeInput, eventValues);
                tryToFillDependentAttributes(dependentAttributeInput, eventValues);
            }
        }
    }

    /**
     * Chooses a random value from the given values and sets it in the given map for the given attribute.
     *
     * @param attributeInput contains the concerned event type attribute and its user input
     * @param eventValues the map containing already set values will be altered by reference
     */
    private void setRandomValueFromRangeForAttribute(AttributeInput attributeInput, Map<String, Serializable> eventValues) {
        attributeInput.calculateRandomValue();
        eventValues.put(attributeInput.getAttributeName(), attributeInput.getCalculatedValue());
    }

    /**
     * Returns the current timestamp to a string using the dateFormatter.
     *
     * @return a string with the formatted timestamp
     */
    private String getCurrentTimestamp() {
        return dateFormatter.format(new Date());
    }
}
