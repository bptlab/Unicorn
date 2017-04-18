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
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.utils.TempFolderUtil;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Class to export objects as Json. Currently only used for the export of dependencies.
 */
public final class JsonExporter {

    private static final Logger logger = Logger.getLogger(JsonExporter.class);

    /**
     * Private constructor throwing exception as this is an utility class.
     */
    private JsonExporter() {
        throw new IllegalAccessError("Utility class");
    }

    /**
     * Generate a Json file for the given event type, including all dependencies
     * and dependency values that have been stored in the EventGenerator.
     *
     * @param eventType eventType to be processed
     * @return resulting json file
     */
    public static File generateExportFileWithDependencies(final EapEventType eventType) {
        // create file
        if (eventType.isHierarchical()) {
            return null;
        }
        final File file = new File(TempFolderUtil.getFolder() + System.getProperty("file.separator") + eventType.getTypeName() + "export.json");
        AttributeDependencyManager depManager = new AttributeDependencyManager(eventType);
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.append("{\"eventTypeDependencies\" : {\"name\" : \"" + eventType.getTypeName() + "\",");
            writer.append("\"timeStampName\" : \"" + eventType.getTimestampName() + "\",");
            writer.append("\"dependencies\" : [");
            List<AttributeDependency> dependencies = depManager.getAttributeDependencies();
            for (int j = 0; j < dependencies.size(); j++) {
                AttributeDependency dependency = dependencies.get(j);
                TypeTreeNode baseAtt = dependency.getBaseAttribute();
                TypeTreeNode dependentAtt = dependency.getDependentAttribute();
                writer.append("{\"base\": {");
                    writer.append("\"name\" : \"" + baseAtt.getName() + "\",");
                    writer.append("\"type\" : \"" + baseAtt.getType() + "\"},");
                writer.append("\"dependent\": {");
                    writer.append("\"name\" : \"" + dependentAtt.getName() + "\",");
                    writer.append("\"type\" : \"" + dependentAtt.getType() + "\"},");
                writer.append("\"values\": {");
                List<AttributeValueDependency> dependencyValues = depManager.getAttributeValueDependencies(dependency);
                for (int i = 0; i < dependencyValues.size(); i++) {
                    AttributeValueDependency dependencyValue = dependencyValues.get(i);
                    writer.append("\"" + dependencyValue.getBaseAttributeValue() + "\" : \"" + dependencyValue.getDependentAttributeValues() + "\"");
                    if (i < dependencyValues.size() - 1) {
                        writer.append(",");
                    }
                }
                writer.append("}}");
                if (j < dependencies.size() - 1) {
                    writer.append(",");
                }
            }
            writer.append("]}}");
            // write stream to file
            writer.flush();
            // close the stream
            writer.close();
        } catch (final IOException e) {
            logger.warn("Error while exporting JSON.", e);
            return null;
        }
        return file;
    }

    public static File generateExportFileWithValues(EapEventType eventType, Map<TypeTreeNode, String> inputValues, int eventCount, int scaleFactor, String timestamp) {
        // create file
        if (eventType.isHierarchical()) {
            return null;
        }
        final File file = new File(TempFolderUtil.getFolder() + System.getProperty("file.separator") + eventType.getTypeName() + "export.json");
        try (FileWriter writer = new FileWriter(file, false)) {
            // general information
            writer.append("{\"eventTypeValues\" : ");
            writer.append("{\"eventType\" : ");
            writer.append("{\"name\" : \"" + eventType.getTypeName() + "\",");
            writer.append("\"timeStampName\" : \"" + eventType.getTimestampName() + "\"},");
            writer.append("\"eventCount\" : " + eventCount + ",");
            writer.append("\"scaleFactor\" : " + scaleFactor + ",");
            writer.append("\"timestamp\" : " + timestamp + ",");

            // values
            writer.append("\"values\" : [");
            TypeTreeNode[] nodes = {};
            TypeTreeNode[] attributes = inputValues.keySet().toArray(nodes);
            for (int i = 0; i < inputValues.size(); i++) {
                TypeTreeNode node = attributes[i];
                writer.append("{\"attribute\" : {");
                writer.append("\"name\" : \"" + node.getName() + "\",");
                writer.append("\"type\" : \"" + node.getType() + "\"},");
                writer.append("\"value\" : \"" + inputValues.get(node) + "\"}");
                if (i != inputValues.size() - 1) {
                    writer.append(",");
                }
            }
            writer.append("]}}");
        } catch (final IOException e1) {
            e1.printStackTrace();
            return null;
        }
        return file;
    }
}
