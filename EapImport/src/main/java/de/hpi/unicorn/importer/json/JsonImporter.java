/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.json;

import de.hpi.unicorn.attributeDependency.AttributeDependency;
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
                AttributeDependency dependency = AttributeDependency.getAttributeDependencyIfExists(eventType, baseAtt, dependentAtt);
                if (dependency == null) {
                    dependency = new AttributeDependency(eventType, baseAtt, dependentAtt);
                    if (dependency.save() == null) {
                        return false;
                    }
                }

                // create dependency values
                AttributeValidator baseValidator = AttributeValidator.getValidatorForAttribute(baseAtt);
                AttributeValidator dependentValidator = AttributeValidator.getValidatorForAttribute(dependentAtt);
                JSONObject valuesJson = dependencyJson.getJSONObject("values");
                Map<String, String> values = new HashMap<>();
                for (int j = 0; j < valuesJson.names().length(); j++) {
                    String baseValue = (String) valuesJson.names().get(j);
                    String dependentValue = valuesJson.getString(baseValue);
                    if (!dependentValidator.validate(dependentValue) || !baseValidator.validate(baseValue)) {
                        return false;
                    }
                    values.put(baseValue, dependentValue);
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
}
