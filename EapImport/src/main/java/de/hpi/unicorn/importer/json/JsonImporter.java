/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.json;

import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.validation.AttributeValidator;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 *  Class to import Json. Currently only used for import of dependencies.
 */
public final class JsonImporter {

    private static final Logger logger = Logger.getLogger(JsonImporter.class);

    /**
     * Private constructor throwing exception as this is an utility class.
     */
    private JsonImporter() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Generate Attribute dependencies for the event generator out of given json string.
     * Export a file containing that string via @JsonExporter.
     *
     * @param dependenciesString jsonString that contains event type with dependencies
     * @return Boolean if generating was successful
     */
    public static boolean generateAttributeDependenciesFromString(String dependenciesString) {
        try {
            JSONObject dependenciesJson = new JSONObject(dependenciesString);
            JSONObject eventTypeJson = dependenciesJson.getJSONObject("eventTypeDependencies");
            // check if corresponding event type exists
            EapEventType eventType = EapEventType.findByTypeName(eventTypeJson.getString("name"));
            if (eventType == null || !eventType.getTimestampName().equals(eventTypeJson.getString("timeStampName"))) {
                return false;
            }
            // add every dependency
            JSONArray eventTypeDependenciesJson = eventTypeJson.getJSONArray("dependencies");
            for (int i = 0; i < eventTypeDependenciesJson.length(); i++) {
                JSONObject dependencyJson = eventTypeDependenciesJson.getJSONObject(i);

                // check if corresponding event type exists with correct attributes
                JSONObject baseJson = dependencyJson.getJSONObject("base");
                boolean baseAttIncluded = eventType.containsValue(baseJson.getString("name"),
                        AttributeTypeEnum.fromString(baseJson.getString("type")));
                JSONObject dependentJson = dependencyJson.getJSONObject("dependent");
                boolean dependentAttIncluded = eventType.containsValue(dependentJson.getString("name"),
                        AttributeTypeEnum.fromString(dependentJson.getString("type")));
                if (!baseAttIncluded || !dependentAttIncluded) {
                    return false;
                }

                // check if dependency rule is already stored and create accordingly
                TypeTreeNode baseAtt = eventType.getValueTypeTree().getAttributeByExpression(baseJson.getString("name"));
                TypeTreeNode dependentAtt = eventType.getValueTypeTree().getAttributeByExpression(dependentJson.getString("name"));
                if (baseAtt == null || dependentAtt == null) {
                    return false;
                }

                AttributeDependency dependency = AttributeDependencyManager.getAttributeDependency(eventType, baseAtt, dependentAtt);
                if (dependency == null) {
                    return false;
                }

                // create dependency values
                Map<String, String> values = JsonImporter.buildMapForDependencyValuesFromJSON(dependencyJson.getJSONObject("values"),
                        baseAtt, dependentAtt);
                if (values == null) {
                    return false;
                }
                if (!dependency.addDependencyValues(values)) {
                    return false;
                }
            }
        } catch (Exception e) {
            logger.warn("Error while importing JSON.", e);
            return false;
        }
        return true;
    }

    /**
     * Parse a given Json string to objects that can be used to import the values specified in json.
     * Used to import input field values.
     *
     * @param valuesString json to be parsed
     * @return map of objects to use imported values
     */
    public static Map<Object, Object> generateValuesFromString(String valuesString) {
        Map<Object, Object> result = new HashMap<>();
        try {
            JSONObject eventTypeValueJson = (new JSONObject(valuesString)).getJSONObject("eventTypeValues");
            JSONObject eventTypeJson = eventTypeValueJson.getJSONObject("eventType");
            // check if corresponding event type exists
            EapEventType eventType = EapEventType.findByTypeName(eventTypeJson.getString("name"));
            if (eventType == null || !eventType.getTimestampName().equals(eventTypeJson.getString("timeStampName"))) {
                return null;
            }
            result.put("eventType", eventType);
            result.put("eventCount", eventTypeValueJson.getInt("eventCount"));
            result.put("scaleFactor", eventTypeValueJson.getInt("scaleFactor"));
            result.put("timestamp", eventTypeValueJson.getString("timestamp"));
            Map<TypeTreeNode, String> values = new HashMap<>();
            JSONArray valuesJson = eventTypeValueJson.getJSONArray("values");
            for (int i = 0; i < valuesJson.length(); i++) {
                JSONObject valuePair = valuesJson.getJSONObject(i);
                JSONObject attributeJson = valuePair.getJSONObject("attribute");
                TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(attributeJson.getString("name"));
                if (attribute == null) {
                    return null;
                }
                values.put(attribute, valuePair.getString("value"));
            }
            result.put("values", values);
        } catch (Exception e) {
            logger.warn("ImportException", e);
            return null;
        }
        return result;
    }

    /**
     * Build a <String, String> Map containing the values for new AttributeValueDependencies.
     *
     * @param jsonValues that need to be parsed
     * @param baseAttribute the dependencies will be defined for
     * @param dependentAttribute the dependencies will be defined for
     * @return a map containing the values
     */
    private static Map<String, String> buildMapForDependencyValuesFromJSON(JSONObject jsonValues, TypeTreeNode baseAttribute,
                                                                          TypeTreeNode dependentAttribute) {
        Map<String, String> values = new HashMap<>();
        AttributeValidator baseValidator = AttributeValidator.getValidatorForAttribute(baseAttribute);
        AttributeValidator dependentValidator = AttributeValidator.getValidatorForAttribute(dependentAttribute);

        try {
            for (int j = 0; j < jsonValues.names().length(); j++) {
                String baseValue = (String) jsonValues.names().get(j);
                String dependentValue = jsonValues.getString(baseValue);
                if (!dependentValidator.validate(dependentValue) || !baseValidator.validate(baseValue)) {
                    return null;
                }
                values.put(baseValue, dependentValue);
            }
        } catch (Exception e) {
            logger.warn("Error while parsing JSON.", e);
            return null;
        }
        return values;
    }
}
