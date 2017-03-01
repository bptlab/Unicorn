/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.json.importer;

import java.util.ArrayList;
import java.util.List;

import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.importer.json.JsonImporter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;

/**
 * This class tests the import of Json files.
 *
 */
public class JSONImportTest {

    EapEventType eventType = new EapEventType("Temp");

    private final static String timestampName = "timestamp";
    private final static String attribute1Name = "attribute1";
    private final static String attribute1Type = "String";
    private final static String attribute2Name = "attribute2";
    private final static String attribute3Name = "attribute3";
    private final static String intDependencyDefaultValue = "1";


    private static String jsonTemplate = "{\"eventTypeDependencies\" : {\"name\" : \"EventType\",\"timeStampName\" : \"%s\"," +
            "\"dependencies\" : [{\"base\": {\"name\" : \"%s\",\"type\" : \"%s\"}," +
            "\"dependent\": {\"name\" : \"" + attribute2Name + "\",\"type\" : \"Integer\"}," +
            "\"values\": {\"String1\" : \"%s\",\"String2\" : \"2;3\"}},{\"base\": {\"name\" : \"" + attribute3Name + "\",\"type\" : \"Float\"}," +
            "\"dependent\": {\"name\" : \"" + attribute2Name + "\",\"type\" : \"Integer\"}," +
            "\"values\": {\"4.0\" : \"4\"}}]}}";

    @Before
    public void setUp() {
        List<TypeTreeNode> attributes = new ArrayList<>();
        attributes.add(new TypeTreeNode(attribute1Name, AttributeTypeEnum.STRING));
        attributes.add(new TypeTreeNode(attribute2Name, AttributeTypeEnum.INTEGER));
        attributes.add(new TypeTreeNode(attribute3Name, AttributeTypeEnum.FLOAT));
        AttributeTypeTree attributeTree = new AttributeTypeTree(attributes);
        eventType = new EapEventType("EventType", attributeTree, "timestamp");
        Persistor.useTestEnviroment();
        EapEventType.removeAll();
    }

    @Test
    public void testAttributeDependencyGeneration() {
        // if given string is an invalid json, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(""));
        // if eventType doesn't exist, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, timestampName, attribute1Name, attribute1Type, intDependencyDefaultValue)));

        EapEventType.save(new ArrayList<EapEventType>() {{add(eventType);}});
        // if timestamp doesn't have correct name, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, "wrongTimestamp", attribute1Name, attribute1Type, intDependencyDefaultValue)));
        // if attributes don't exist in event type, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, timestampName, "wrongAttributeName", attribute1Type, intDependencyDefaultValue)));
        // if attributes don't have correct type, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, timestampName, attribute1Name, "Integer", intDependencyDefaultValue)));
        // if dependency values don't fit data type, return false
        Assert.assertEquals(false, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, timestampName, attribute1Name, attribute1Type, "wrongValue")));

        Assert.assertEquals(true, JsonImporter.generateAttributeDependenciesFromString(
                String.format(jsonTemplate, timestampName, attribute1Name, attribute1Type, intDependencyDefaultValue)));

    }
}
