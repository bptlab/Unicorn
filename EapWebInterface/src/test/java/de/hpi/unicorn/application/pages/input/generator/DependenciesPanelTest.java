package de.hpi.unicorn.application.pages.input.generator;


import de.hpi.unicorn.application.UNICORNApplication;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.persistence.Persistor;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.iterator.ComponentHierarchyIterator;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;

import java.util.ArrayList;
import java.util.List;

public class DependenciesPanelTest extends TestCase {
    private WicketTester tester;
    private String formPath;
    private String listViewPath;
    private TypeTreeNode baseAttribute = new TypeTreeNode("BaseAttribute");
    private TypeTreeNode depAttribute = new TypeTreeNode("DepAttribute", AttributeTypeEnum.INTEGER);
    private List<TypeTreeNode> attributes = new ArrayList<>();
    private EapEventType eventType;

    public void setUp() throws Exception{
        super.setUp();
        Persistor.useTestEnvironment();
        attributes.add(baseAttribute);
        attributes.add(depAttribute);
        AttributeTypeTree attributeTree = new AttributeTypeTree(attributes);
        eventType = new EapEventType("TestType", attributeTree);
        ArrayList<EapEventType> eventTypes = new ArrayList<>();
        eventTypes.add(eventType);
        EapEventType.save(eventTypes);
        tester = new WicketTester(new UNICORNApplication());
        tester.startPage(GeneratorPage.class);
        tester.startComponentInPage(new DependenciesPanel("dependenciesPanel" , new GeneratorPage()));
        // get form
        List<Form> list = new ArrayList<>();
        Page x = tester.getLastRenderedPage();
        for (Component form : x.visitChildren(Form.class)) {
            list.add((Form) form);
        }
        formPath = list.get(0).getPageRelativePath();
        listViewPath = formPath + ":dependenciesContainer:dependenciesListview";
    }

    public void testBasicRender() {
        Assert.assertTrue(tester.getLastResponse().getDocument().contains("dependencyForm"));
    }

    public void testEnablingOfComponents() {
        tester.assertDisabled(formPath + ":submitButton");
        tester.assertDisabled(formPath + ":addDependencyValueButton");
        tester.assertEnabled(formPath + ":baseAttributeField");
        tester.assertEnabled(formPath + ":dependentAttributeField");
        tester.assertEnabled(formPath + ":eventTypeField");
        tester.assertEnabled(formPath + ":deleteDependencyButton");
        tester.assertEnabled(formPath + ":deleteValuesButton");

        tester.executeAjaxEvent(formPath + ":addDependencyButton", "click");

        tester.assertEnabled(formPath + ":submitButton");
        tester.assertEnabled(formPath + ":addDependencyValueButton");
        tester.assertDisabled(formPath + ":baseAttributeField");
        tester.assertDisabled(formPath + ":dependentAttributeField");
        tester.assertDisabled(formPath + ":eventTypeField");
    }

    public void testDropDowns() {
        // event type drop down
        DropDownChoice eventTypeDropDown = (DropDownChoice) tester.getComponentFromLastRenderedPage(formPath + ":eventTypeField");
        Assert.assertNotNull(eventTypeDropDown.getChoices());
        FormTester formTester = tester.newFormTester(formPath, false);
        formTester.select("eventTypeField", 0);
        Assert.assertEquals(eventType.getTypeName(), ((EapEventType) eventTypeDropDown.getChoices().get(0)).getTypeName());

        // attribute drop downs
        DropDownChoice baseDropDown = (DropDownChoice) tester.getComponentFromLastRenderedPage(formPath + ":baseAttributeField");
        Assert.assertEquals(2, baseDropDown.getChoices().size());
        Assert.assertEquals(attributes, baseDropDown.getChoices());
        DropDownChoice depDropDown = (DropDownChoice) tester.getComponentFromLastRenderedPage(formPath + ":dependentAttributeField");
        Assert.assertEquals(1, depDropDown.getChoices().size());
        Assert.assertEquals(depAttribute, depDropDown.getChoices().get(0));

        // shown type labels
        Label baseTypeLabel = (Label) tester.getComponentFromLastRenderedPage(formPath + ":selectedBaseAttributeType");
        Assert.assertEquals("String", baseTypeLabel.getDefaultModelObjectAsString());
        Label depTypeLabel = (Label) tester.getComponentFromLastRenderedPage(formPath + ":selectedDependentAttributeType");
        Assert.assertEquals("Integer", depTypeLabel.getDefaultModelObjectAsString());
    }

    public void testDependencyDeletion() {
        // no dependency saved
        tester.executeAjaxEvent(formPath + ":deleteDependencyButton", "click");
        Assert.assertTrue(tester.getLastResponse().getDocument().contains("Error while deleting dependency. Dependency is already deleted."));

        // create dependency
        tester.executeAjaxEvent(formPath + ":addDependencyButton", "click");
        tester.executeAjaxEvent(formPath + ":submitButton", "click");
        tester.executeAjaxEvent(formPath + ":deleteDependencyButton", "click");
        Assert.assertTrue(tester.getLastResponse().getDocument().contains("Dependency deleted."));

        // dependency has Values
        addDependencyValues();
        tester.executeAjaxEvent(formPath + ":deleteDependencyButton", "click");
        Assert.assertTrue(tester.getLastResponse().getDocument().contains("Error while deleting dependency. Please Delete the corresponding values first."));
    }

    public void testSelectAllCheckbox() {
        addDependencyValues();
        Assert.assertFalse(((AjaxCheckBox) tester.getComponentFromLastRenderedPage(listViewPath + ":0:deleteCheckbox")).getModel().getObject());

        // click checkbox
        FormTester formTester = tester.newFormTester(formPath, false);
        formTester.setValue("dependenciesContainer:selectAllCheckbox", true);
        tester.executeAjaxEvent(formPath + ":dependenciesContainer:selectAllCheckbox", "onclick");

        ComponentHierarchyIterator allCheckboxes = tester.getLastRenderedPage().visitChildren(AjaxCheckBox.class);
        int numberOfCheckedBoxes = 0;
        for (Component box : allCheckboxes) {
            numberOfCheckedBoxes++;
            System.out.println("PATH: " + box.getPageRelativePath());
            Assert.assertTrue(((AjaxCheckBox) box).getModel().getObject());
        }
        Assert.assertEquals(3, numberOfCheckedBoxes);
    }

    public void testDependencyValueDeletion() {
        addDependencyValues();
        final Label oldLabel = (Label) tester.getComponentFromLastRenderedPage(listViewPath + ":0:baseAttributeValue");

        FormTester formTester = tester.newFormTester(formPath, false);
        formTester.setValue("dependenciesContainer:dependenciesListview:0:deleteCheckbox", true);
        tester.executeAjaxEvent(listViewPath + ":0:deleteCheckbox", "onclick");
        tester.executeAjaxEvent(formPath + ":deleteValuesButton", "onclick");

        final Label newLabel = (Label) tester.getComponentFromLastRenderedPage(listViewPath + ":0:baseAttributeValue");

        Assert.assertFalse(oldLabel.getDefaultModelObjectAsString().equals(newLabel.getDefaultModelObjectAsString()));
    }

    private void addDependencyValues() {
        tester.executeAjaxEvent(formPath + ":addDependencyButton", "click");
        FormTester formTester = tester.newFormTester(formPath, false);
        formTester.setValue("baseAttributeInput", "String1");
        formTester.setValue("dependentAttributeInput", "1");
        tester.executeAjaxEvent(formPath + ":addDependencyValueButton", "click");
        formTester.setValue("baseAttributeInput", "String2");
        formTester.setValue("dependentAttributeInput", "2");
        tester.executeAjaxEvent(formPath + ":addDependencyValueButton", "click");
        tester.executeAjaxEvent(formPath + ":submitButton", "click");
    }
}
