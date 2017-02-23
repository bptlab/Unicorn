package de.hpi.unicorn.importer.json;

import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.log4j.Logger;

import javax.json.JsonObject;

public class JsonImporter {
    private static Logger logger = Logger.getLogger(JsonImporter.class);
    public static AttributeDependency generateAttributeDependencyFromString(String dependencyString) {
        try {
            JSONObject dependencyJson = new JSONObject(dependencyString);
            JSONObject eventTypeJson = dependencyJson.getJSONObject("eventTypeDependencies");
            EapEventType eventType = EapEventType.findByTypeName(eventTypeJson.getString("name"));
            if (!eventType.getTimestampName().equals(eventTypeJson.getString("timeStampName"))) {
                logger.info("No such event type!");
                return null;
            }
            JSONArray dependencies = eventTypeJson.getJSONArray("dependencies");
            for (int i = 0; i < dependencies.length(); i++) {
                JSONObject base = dependencies.getJSONObject(i).getJSONObject("base");
                boolean baseAttIncluded = eventType.containsValue(base.getString("name"), AttributeTypeEnum.fromString(base.getString("type")));
                JSONObject dependent = dependencies.getJSONObject(i).getJSONObject("dependent");
                boolean dependentAttIncluded = eventType.containsValue(dependent.getString("name"), AttributeTypeEnum.fromString(dependent.getString("type")));
                if (!baseAttIncluded || !dependentAttIncluded) {
                    logger.warn("Attribute not included in Event Type.");
                    return null;
                }
                AttributeDependency dep = new AttributeDependency(eventType, TypeTreeNode.findByName(base.getString("name")).get(0), TypeTreeNode.findByName(dependent.getString("name")).get(0));
                logger.info("Created Dependency with base attribute: " + dep.getBaseAttribute().getName() + " and dep attribute: " + dep.getDependentAttribute());
            }
            logger.info("Json successfully created for eventType: " + eventType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Json not created.");
            return null;
        }
        return new AttributeDependency();
    }

    private static boolean checkPrerequisites(JSONObject dependencyJson){
        try {
            JSONObject eventTypeJson = dependencyJson.getJSONObject("eventTypeDependencies");
            EapEventType eventType = EapEventType.findByTypeName(eventTypeJson.getString("name"));
            if (!eventType.getTimestampName().equals(eventTypeJson.getString("timeStampName"))) {
                logger.info("No such event type!");
                return false;
            }
            JSONArray dependencies = eventTypeJson.getJSONArray("dependencies");
            for (int i = 0; i < dependencies.length(); i++) {
                JSONObject base = dependencies.getJSONObject(i).getJSONObject("base");
                boolean baseAttIncluded = eventType.containsValue(base.getString("name"), AttributeTypeEnum.fromString(base.getString("type")));
                JSONObject dependent = dependencies.getJSONObject(i).getJSONObject("dependent");
                boolean dependentAttIncluded = eventType.containsValue(dependent.getString("name"), AttributeTypeEnum.fromString(dependent.getString("type")));
                if (!baseAttIncluded || !dependentAttIncluded) {
                    logger.warn("Attribute not included in Event Type.");
                    return false;
                }

            }
            logger.info("Json successfully created for eventType: " + eventType);
        } catch (Exception e) {
            e.printStackTrace();
            logger.info("Json not created.");
        }
        return true;
    }
}
