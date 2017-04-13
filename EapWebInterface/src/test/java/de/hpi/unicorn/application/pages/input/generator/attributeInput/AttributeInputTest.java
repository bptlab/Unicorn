package de.hpi.unicorn.application.pages.input.generator.attributeInput;


import de.hpi.unicorn.application.pages.input.generator.validation.DateRangeValidator;
import de.hpi.unicorn.application.pages.input.generator.validation.IntegerRangeValidator;
import de.hpi.unicorn.application.pages.input.generator.validation.RegexValidator;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;
import junit.framework.TestCase;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;

import java.util.Date;


public class AttributeInputTest extends TestCase {

    static final Logger logger = Logger.getLogger(AttributeInputTest.class);

    private AttributeTypeTree attributeTree;
    private TypeTreeNode stringAttribute = new TypeTreeNode("StringAttribute", AttributeTypeEnum.STRING);
    private TypeTreeNode integerAttribute = new TypeTreeNode("IntegerAttribute", AttributeTypeEnum.INTEGER);
    private TypeTreeNode floatAttribute = new TypeTreeNode("FloatAttribute", AttributeTypeEnum.FLOAT);
    private TypeTreeNode dateAttribute = new TypeTreeNode("DateAttribute", AttributeTypeEnum.DATE);

    private AttributeInput stringAttributeInput = AttributeInput.attributeInputFactory(stringAttribute);
    private AttributeInput integerAttributeInput = AttributeInput.attributeInputFactory(integerAttribute);
    private AttributeInput floatAttributeInput = AttributeInput.attributeInputFactory(floatAttribute);
    private AttributeInput dateAttributeInput = AttributeInput.attributeInputFactory(dateAttribute);


    @BeforeClass
    public void setUpClass() {
        Persistor.useTestEnvironment();
        TypeTreeNode attribute = new TypeTreeNode("StringAttribute", AttributeTypeEnum.STRING);
        attributeTree = new AttributeTypeTree(stringAttribute);
        attributeTree.addRoot(integerAttribute);
        attributeTree.addRoot(floatAttribute);
        attributeTree.addRoot(dateAttribute);
    }

    public void testAttributeInputFactory() {
        assertTrue("AttributeInput for string attribute wasn't StringAttributeInput",
                stringAttributeInput.getCalculatedValue() instanceof String);

        assertTrue("AttributeInput for integer attribute wasn't IntegerAttributeInput",
                integerAttributeInput.getCalculatedValue() instanceof Integer);

        assertTrue("AttributeInput for float attribute wasn't FloatAttributeInput",
                floatAttributeInput.getCalculatedValue() instanceof Float);

        assertTrue("AttributeInput for date attribute wasn't DateAttributeInput",
                dateAttributeInput.getCalculatedValue() instanceof Date);
    }

    public void testGetInputOrDefault() {
        final String defaultStringInput = "String1;String2;String3";
        final String defaultIntegerUniformInput = "1-50";
        final String defaultIntegerNormalInput = "5;1";
        final String defaultFloatUniformInput = "1.1;1.2;2.0;2.5";
        final String defaultFloatNormalInput = "5;1";
        final String defaultDateInput = "2017/01/22T12:00-2017/02/23T14:59";
        String userInput = "";

        //String attribute
        assertEquals("Default input for StringAttributeInput changed.", stringAttributeInput.getInputOrDefault(), defaultStringInput);
        userInput = "abc";
        stringAttributeInput.setInput(userInput);
        assertEquals("User input for StringAttributeInput not properly returned", stringAttributeInput.getInputOrDefault(), userInput);


        //Integer attribute
        integerAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.UNIFORM);
        assertEquals("Default input for IntegerAttributeInput (Uniform) changed.", integerAttributeInput.getInputOrDefault(),
                defaultIntegerUniformInput);
        integerAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.NORMAL);
        assertEquals("Default input for IntegerAttributeInput (Normal) changed.", integerAttributeInput.getInputOrDefault(),
                defaultIntegerNormalInput);
        userInput = "123";
        integerAttributeInput.setInput(userInput);
        assertEquals("User input for IntegerAttributeInput not properly returned", integerAttributeInput.getInputOrDefault(), userInput);

        //Float attribute
        floatAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.UNIFORM);
        assertEquals("Default input for FloatAttributeInput (Uniform) changed.", floatAttributeInput.getInputOrDefault(), defaultFloatUniformInput);
        floatAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.NORMAL);
        assertEquals("Default input for FloatAttributeInput (Normal) changed.", floatAttributeInput.getInputOrDefault(), defaultFloatNormalInput);
        userInput = "1.23";
        floatAttributeInput.setInput(userInput);
        assertEquals("User input for FloatAttributeInput not properly returned", floatAttributeInput.getInputOrDefault(), userInput);

        //Date attribute
        assertEquals("Default input for DateAttributeInput changed.", dateAttributeInput.getInputOrDefault(), defaultDateInput);
        userInput = "2017/01/22T12:00";
        dateAttributeInput.setInput(userInput);
        assertEquals("User input for DateAttributeInput not properly returned", dateAttributeInput.getInputOrDefault(), userInput);
    }

    public void testHasDifferentMethods() {
        assertFalse("String input shouldn't have different methods.", stringAttributeInput.hasDifferentMethods());
        assertTrue("Integer input should have different methods.", integerAttributeInput.hasDifferentMethods());
        assertTrue("Float input should have different methods.", floatAttributeInput.hasDifferentMethods());
        assertFalse("Date input shouldn't have different methods.", dateAttributeInput.hasDifferentMethods());
    }

    public void testGetAttributeInputValidator() {
        assertTrue("Wrong validator for StringAttributeInput.", stringAttributeInput.getAttributeInputValidator() instanceof RegexValidator);

        integerAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.UNIFORM);
        assertTrue("Wrong validator for IntegerAttributeInput (Uniform).", integerAttributeInput.getAttributeInputValidator() instanceof
                IntegerRangeValidator);
        integerAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.NORMAL);
        assertTrue("Wrong validator for IntegerAttributeInput (Normal).", integerAttributeInput.getAttributeInputValidator() instanceof
                RegexValidator);

        floatAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.UNIFORM);
        assertTrue("Wrong validator for FloatAttributeInput (Uniform).", floatAttributeInput.getAttributeInputValidator() instanceof
                RegexValidator);
        floatAttributeInput.setSelectedMethod(AttributeInput.ProbabilityDistributionEnum.NORMAL);
        assertTrue("Wrong validator for FloatAttributeInput (Normal).", floatAttributeInput.getAttributeInputValidator() instanceof
                RegexValidator);

        assertTrue("Wrong validator for DateAttributeInput.", dateAttributeInput.getAttributeInputValidator() instanceof DateRangeValidator);
    }

}
