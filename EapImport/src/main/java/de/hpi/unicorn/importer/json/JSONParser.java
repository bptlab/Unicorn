package de.hpi.unicorn.importer.json;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.ConversionUtils;
import de.hpi.unicorn.utils.DateUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;

import java.util.*;

public class JSONParser {

    private static final Logger logger = Logger.getLogger(JsonImporter.class);

    /**
     * Parses a single event from a {@link String}.
     */
    public static List<EapEvent> generateEventsFromJsonString(final String jsonString) throws JSONParsingException, JSONException {
        JSONObject eventsObject = new JSONObject(jsonString);
        String eventTypeName = eventsObject.getString("eventType");
        EapEventType eventType = EapEventType.findBySchemaName(eventTypeName);
        if (eventType == null) {
            throw new JSONParsingException("No matching eventtype was found; please upload corresponding XSD");
        }
        final String timestampName = eventType.getTimestampName();

        JSONArray eventList = eventsObject.getJSONArray("events");
        List<Map<String, String>> events = new ArrayList<>();
        for (int i = 0; i < eventList.length(); i++) {
            logger.error(i);
            JSONObject eventJson = eventList.getJSONObject(i);
            logger.error(eventJson);
            Map<String, String> map = new HashMap<String, String>();
            Iterator<String> keysItr = eventJson.keys();
            while(keysItr.hasNext()) {
                String key = keysItr.next();
                logger.error(key);
                String value = eventJson.getString(key);
                logger.error(value);
                map.put(key, value);
            }
            events.add(map);
        }
        final List<EapEvent> generatedEvents = new ArrayList<>();
        for (final Map<String, String> eventValues : events) {
            Date eventTimestamp;
            if (timestampName == null) {
                eventTimestamp = new Date();
            } else {
                final String time = eventValues.get(eventType.getTimestampName());
                if (time == null) {
                    break;
                }
                eventTimestamp = (DateUtils.parseDate(time) != null) ? DateUtils.parseDate(time) : new Date();
            }
            eventValues.keySet().retainAll(eventType.getAttributeExpressionsWithoutTimestampName());
            final EapEvent event = new EapEvent(eventType, eventTimestamp);
            final String nameOfAttributeWithInvalidValue = ConversionUtils.validateEvent(eventType, eventValues);
            if (nameOfAttributeWithInvalidValue != null) {
                throw new JSONParsingException("Event in the XML files does not match to event type: " + "Value type of attribute '" + nameOfAttributeWithInvalidValue + "' in the event " + "does not match to the value type defined in the event type" + "or value for attribute '" + nameOfAttributeWithInvalidValue + "' is missing in the event.");
            }
            event.setValuesWithoutConversion(eventValues);
            generatedEvents.add(event);
        }
        if (events.size() == 0) {
            throw new JSONParsingException("Events in the XML file do not match to the given event type.");
        }
        return generatedEvents;
    }

}
