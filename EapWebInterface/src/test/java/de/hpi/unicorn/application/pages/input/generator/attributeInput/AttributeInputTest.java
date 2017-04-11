package de.hpi.unicorn.application.pages.input.generator.attributeInput;


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


public class AttributeInputTest extends TestCase {

    static final Logger logger = Logger.getLogger(AttributeInputTest.class);

    TypeTreeNode attribute3 = new TypeTreeNode("Attribute3", AttributeTypeEnum.DATE);
    private String attributeName = "TestAttribute";
    private String attributeValue = "AttributeValue";
    private TypeTreeNode attribute = new TypeTreeNode(attributeName);
    private AttributeTypeTree attributeTree = new AttributeTypeTree(attribute);


    @BeforeClass
    public void setUpClass() {

    }

    @Before
    public void setUp() {
        attributeTree.addRoot(attribute3);
        Persistor.useTestEnvironment();
        EapEvent.removeAll();
        EapEventType.removeAll();
    }


    public void testAttributeInputFactory() {

    }

}
