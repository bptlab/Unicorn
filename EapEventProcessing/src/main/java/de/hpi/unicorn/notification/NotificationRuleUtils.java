package de.hpi.unicorn.notification;

import de.hpi.unicorn.utils.DateUtils;
import net.sf.json.JSONObject;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public final class NotificationRuleUtils {

    private static final String EVENT_KEY_HIRARCHY_SEPERATOR = "\\.";

    public static JSONObject toJSON(final Map<Object, Serializable> eventObject) throws UnsupportedJsonTransformation {
        final JSONObject event = new JSONObject();
        // the keys of the "values" field describe
        // hierarchy dependencies through the string
        // pattern "\\."
        for (final Object key : eventObject.keySet()) {
            final String rootKey = getRootKey(event, key.toString());
            Object rootValue = getRootValue(eventObject, event, key.toString());
            if (rootValue instanceof Date) {
                rootValue = DateUtils.getFormatter().format(rootValue);
            }
            event.put(rootKey, rootValue);
        }
        return event;
    }

    /**
     * Returns the object that has to be inserted on the root level of the JSON
     * object, to insert the value of the specified key into the right place of
     * the given given event.
     *
     * @param event
     * @param key
     * @return root level object
     * @throws UnsupportedJsonTransformation
     */
    private static Object getRootValue(final Map<Object, Serializable> eventObject, final JSONObject event, final String key)
            throws UnsupportedJsonTransformation {
        return getRootValueInternal(event, key.split(EVENT_KEY_HIRARCHY_SEPERATOR), 0,
                eventObject.get(key));
    }

    /**
     * Do not invoke this method directly refer to description of
     *
     * @param value
     * @throws UnsupportedJsonTransformation
     */
    private static Object getRootValueInternal(final JSONObject object, final String[] keys, int iterator, final Object value)
            throws UnsupportedJsonTransformation {
        Object returnObject;
        if (object.has(keys[iterator])) {
            returnObject = combineValue(object, keys, iterator, value);
        } else {
            returnObject = buildNewValue(keys, ++iterator, value);
        }
        return returnObject;
    }

    /**
     * Merge the value into an already existing hierarchical JSON object.
     *
     * @param object
     * @param keys
     * @param iterator
     * @param value
     * @return
     * @throws UnsupportedJsonTransformation
     */
    private static Object combineValue(final JSONObject object, final String[] keys, int iterator, final Object value)
            throws UnsupportedJsonTransformation {
        final Object valueToCombine = object.get(keys[iterator++]);
        if (!(valueToCombine instanceof JSONObject)) {
            throw new UnsupportedJsonTransformation();
        }
        if (((JSONObject) valueToCombine).containsKey(keys[iterator])) {
            ((JSONObject) valueToCombine).put(keys[iterator],
                    getRootValueInternal((JSONObject) valueToCombine, keys, iterator, value));
        } else {
            ((JSONObject) valueToCombine).put(keys[iterator], buildNewValue(keys, ++iterator, value));
        }
        return valueToCombine;
    }

    /**
     * Create a new hierarchical json object value.
     *
     * @param keys
     * @param iterator
     * @param value
     * @return value
     */
    private static Object buildNewValue(final String[] keys, int iterator, final Object value) {
        if (iterator == keys.length) {
            return value;
        } else {
            final JSONObject newValue = new JSONObject();
            newValue.put(keys[iterator], buildNewValue(keys, ++iterator, value));
            return newValue;
        }
    }

    /**
     * Returns the key on the root level of the json object where the specified
     * key will be a child of.
     *
     * @param event
     * @param key
     * @return root level key
     */
    private static String getRootKey(final JSONObject event, final String key) {
        return key.split(EVENT_KEY_HIRARCHY_SEPERATOR)[0];
    }
}
