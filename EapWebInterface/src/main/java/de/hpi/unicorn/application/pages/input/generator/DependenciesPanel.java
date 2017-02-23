/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.validator.PatternValidator;
import org.w3c.dom.Attr;

import javax.management.Attribute;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {@link Panel}, which allows the insertion of dependencies between event type attributes.
 */
public class DependenciesPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private DependenciesPanel panel;

    private Form dependencyForm;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test" );
    private TypeTreeNode selectedBaseAttribute = new TypeTreeNode("base attribute");
    private TypeTreeNode selectedDependentAttribute = new TypeTreeNode("dependent attribute");
    private String currentBaseAttributeInput = "";
    private String currentDependentAttributeInput = "";

    private DropDownChoice<TypeTreeNode> baseDropDown;
    private DropDownChoice<TypeTreeNode> dependentDropDown;
    private DropDownChoice<EapEventType> eventTypeDropDown;

    private TextField<String> baseAttributeInputField;
    private TextField<String> dependentAttributeInputField;

    private Label selectedBaseAttributeTypeLabel;
    private Label selectedDependentAttributeTypeLabel;

    private AjaxButton addDependencyButton;
    private AjaxButton addDependencyValueButton;
    private AjaxButton submitButton;

    private HashMap<String, String> dependenciesInput = new HashMap<>();
    private ListView<String> listview;
    private WebMarkupContainer listContainer;

    /**
     * Constructor for the dependencies panel. The page is initialized in this method,
     * including the event type dropdown and the according event type attributes dropdowns.
     *
     * @param id
     * @param page
     */
    DependenciesPanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;
        dependencyForm = new Form("dependencyForm");
        this.add(dependencyForm);


        addDropDowns();
        addAddDependencyButton();
        addDependencyValuesInputs();
        addAddDependencyValuesButton();
        addListOfDependencies();
        addSubmitButton();
    }

    /**
     * Adds dropdown with existing event types.
     * When selected, the two dropdowns containing the attributes of the choosen event type will be updated.
     * Using the two dropdowns the base and dependent attribute can be selected.
     */
    private void addDropDowns() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        if(!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
            setFirstAttributes();
        }

        baseDropDown = new DropDownChoice<>("baseAttributeField", new PropertyModel<TypeTreeNode>( this,
                "selectedBaseAttribute" ),
                selectedEventType.getValueTypes(), new ChoiceRenderer<>("name", "name"));

        dependentDropDown = new DropDownChoice<>("dependentAttributeField", new PropertyModel<TypeTreeNode>( this,
                "selectedDependentAttribute" ),
                getDependentAttributeChoiceList(), new ChoiceRenderer<>("name", "name"));

        selectedBaseAttributeTypeLabel = new Label("selectedBaseAttributeType", new PropertyModel<String>(this,
                "selectedBaseAttribute.type"));
        selectedDependentAttributeTypeLabel = new Label("selectedDependentAttributeType", new PropertyModel<String>(this,
                "selectedDependentAttribute.type"));

        eventTypeDropDown = new DropDownChoice<>("eventTypeField", new PropertyModel<EapEventType>( this, "selectedEventType" ),
                eventTypes);

        selectedBaseAttributeTypeLabel.setOutputMarkupId(true);
        selectedDependentAttributeTypeLabel.setOutputMarkupId(true);

        baseDropDown.setRequired(true);
        baseDropDown.setOutputMarkupId(true);
        dependentDropDown.setRequired(true);
        dependentDropDown.setOutputMarkupId(true);
        eventTypeDropDown.setRequired(true);
        eventTypeDropDown.setOutputMarkupId(true);

        baseDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    dependentDropDown.setChoices(getDependentAttributeChoiceList());
                    if(selectedBaseAttribute == selectedDependentAttribute) {
                        selectedDependentAttribute = dependentDropDown.getChoices().get(0);
                    }
                    target.add(dependentDropDown);
                    updateDependenciesMapForCurrentSelection();
                    updateLabelsAndList(target);
                }
            }
        });
        dependentDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    updateDependenciesMapForCurrentSelection();
                    updateLabelsAndList(target);
                }
            }
        });

        eventTypeDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    setFirstAttributes();
                    baseDropDown.setChoices(selectedEventType.getValueTypes());
                    dependentDropDown.setChoices(getDependentAttributeChoiceList());
                    target.add(baseDropDown);
                    target.add(dependentDropDown);
                    updateDependenciesMapForCurrentSelection();
                    updateLabelsAndList(target);
                }
            }
        });

        dependencyForm.add(eventTypeDropDown);
        dependencyForm.add(baseDropDown);
        dependencyForm.add(dependentDropDown);
        dependencyForm.add(selectedBaseAttributeTypeLabel);
        dependencyForm.add(selectedDependentAttributeTypeLabel);
    }

    private void addAddDependencyButton() {
        addDependencyButton = new AjaxButton("addDependencyButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                if(selectedBaseAttribute == selectedDependentAttribute) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("An attribute can't depend on itself.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }

                //DISABLE DEPENDENCY FORM
                disableDropDowns();
                addDependencyButton.setEnabled(false);
                target.add(eventTypeDropDown);
                target.add(baseDropDown);
                target.add(dependentDropDown);
                target.add(addDependencyButton);

                //ENABLE DEPENDENCY-VALUES FORM
                baseAttributeInputField.setEnabled(true);
                dependentAttributeInputField.setEnabled(true);
                addDependencyValueButton.setEnabled(true);
                submitButton.setEnabled(true);
                target.add(baseAttributeInputField);
                target.add(dependentAttributeInputField);
                target.add(addDependencyValueButton);
                target.add(submitButton);

                //ADD VALIDATORS FOR VALUE-INPUTS
                setValidatorsForInputFields(target);

                listview.removeAll();
                target.add(listContainer);

                //SUCCESS HINT
                DependenciesPanel.this.page.getFeedbackPanel().success("Please insert values for the dependency.");
                target.add(DependenciesPanel.this.page.getFeedbackPanel());
            }
        };
        dependencyForm.add(addDependencyButton);
    }

    private void addDependencyValuesInputs() {
        baseAttributeInputField = new TextField<String>("baseAttributeInput", new PropertyModel<String>(this,
                "currentBaseAttributeInput"));
        dependentAttributeInputField = new TextField<String>("dependentAttributeInput", new PropertyModel<String>(this,
                "currentDependentAttributeInput"));
        baseAttributeInputField.setLabel(new Model<String>("currentBaseAttributeInput"));
        baseAttributeInputField.setOutputMarkupId(true);
        baseAttributeInputField.setEnabled(false);
        dependentAttributeInputField.setLabel(new Model<String>("currentDependentAttributeInput"));
        dependentAttributeInputField.setOutputMarkupId(true);
        dependentAttributeInputField.setEnabled(false);
        dependencyForm.add(baseAttributeInputField);
        dependencyForm.add(dependentAttributeInputField);
    }

    private void addAddDependencyValuesButton() {
        addDependencyValueButton = new AjaxButton("addDependencyValueButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                if(currentBaseAttributeInput == null || currentDependentAttributeInput == null ||
                        currentBaseAttributeInput.isEmpty() || currentDependentAttributeInput.isEmpty()) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("You must provide an input!");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }
                if(dependenciesInput.containsKey(currentBaseAttributeInput)) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("This value is already registered!");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }
                dependenciesInput.put(currentBaseAttributeInput, currentDependentAttributeInput);
                DependenciesPanel.this.page.getFeedbackPanel().info("Added dependency!");
                target.add(DependenciesPanel.this.page.getFeedbackPanel());
                currentBaseAttributeInput = "";
                currentDependentAttributeInput = "";

                target.add(baseAttributeInputField);
                target.add(dependentAttributeInputField);
                listview.removeAll();
                target.add(listContainer);
            }
        };
        addDependencyValueButton.setEnabled(false);
        dependencyForm.add(addDependencyValueButton);
    }

    private void addListOfDependencies() {
        LoadableDetachableModel list =  new LoadableDetachableModel()
        {
            @Override
            protected Object load() {
                return new ArrayList<String>(dependenciesInput.keySet());
            }
        };
        listview = new ListView<String>("dependenciesListview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final String key = (String) item.getModelObject();
                item.add(new Label("baseAttributeValue", key));
                item.add(new Label("dependentAttributeValue", dependenciesInput.get(key)));
            }
        };
        listContainer = new WebMarkupContainer("dependenciesContainer");
        listContainer.add(new Label("selectedBaseAttributeLabel", new PropertyModel<String>(this, "selectedBaseAttribute.name")).setOutputMarkupId
                (true));
        listContainer.add(new Label("selectedDependentAttributeLabel", new PropertyModel<String>(this, "selectedDependentAttribute.name"))
                .setOutputMarkupId(true));
        listContainer.add(listview);
        listContainer.setOutputMarkupId(true);
        dependencyForm.add(listContainer);
    }

    private void addSubmitButton() {
        submitButton = new AjaxButton("submitButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                AttributeDependency dependency = new AttributeDependency(selectedEventType, selectedBaseAttribute, selectedDependentAttribute);
                dependency.save();
                if(dependency.addDependencyValues(dependenciesInput)) {
                    DependenciesPanel.this.page.getFeedbackPanel().success("Submitted.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                }
                else {
                    DependenciesPanel.this.page.getFeedbackPanel().error("Error while saving dependencies. Please try again.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                }
            }
            @Override
            public void onAfterSubmit(final AjaxRequestTarget target, final Form form) {
                dependenciesInput = new HashMap<>();
                currentBaseAttributeInput = "";
                currentDependentAttributeInput = "";
                baseAttributeInputField.setEnabled(false);
                dependentAttributeInputField.setEnabled(false);
                addDependencyValueButton.setEnabled(false);
                submitButton.setEnabled(false);
                target.add(baseAttributeInputField);
                target.add(dependentAttributeInputField);
                target.add(addDependencyValueButton);
                target.add(submitButton);

                setEnablementForDropDowns(true);
                addDependencyButton.setEnabled(true);
                target.add(addDependencyButton);
                target.add(baseAttributeInputField);
                target.add(dependentAttributeInputField);
                target.add(eventTypeDropDown);
                target.add(baseDropDown);
                target.add(dependentDropDown);
            }
        };
        submitButton.setEnabled(false);
        dependencyForm.add(submitButton);
    }

    private void setFirstAttributes() {
        if(selectedEventType.getValueTypes().size() >= 2) {
            selectedBaseAttribute = selectedEventType.getValueTypes().get(0);
            selectedDependentAttribute = selectedEventType.getValueTypes().get(1);
        }
    }

    private void disableDropDowns() {
        setEnablementForDropDowns(false);
    }
    private void setEnablementForDropDowns(Boolean enabled) {
        eventTypeDropDown.setEnabled(enabled);
        baseDropDown.setEnabled(enabled);
        dependentDropDown.setEnabled(enabled);
    }

    private List<TypeTreeNode> getDependentAttributeChoiceList() {
        List<TypeTreeNode> attributesWithoutBaseAttribute = selectedEventType.getValueTypes();
        try {
            attributesWithoutBaseAttribute.remove(selectedBaseAttribute);
        }
        finally {}
        return attributesWithoutBaseAttribute;
    }

    private void updateLabelsAndList(AjaxRequestTarget target) {
        listview.removeAll();
        target.add(selectedBaseAttributeTypeLabel);
        target.add(selectedDependentAttributeTypeLabel);
        target.add(listContainer);
    }

    private void updateDependenciesMapForCurrentSelection() {
        dependenciesInput = new HashMap<>();
        AttributeDependency attributeDependency = AttributeDependency.getAttributeDependencyBetweenTwoAttributes(selectedBaseAttribute, selectedDependentAttribute);
        if(attributeDependency != null) {
            for(AttributeValueDependency attributeValueDependency : AttributeValueDependency.getAttributeValueDependenciesForAttributeDependency
                    (attributeDependency)) {
                dependenciesInput.put(attributeValueDependency.getBaseAttributeValue(), attributeValueDependency.getDependentAttributeValues());
            }
        }
    }

    private void setValidatorsForInputFields(AjaxRequestTarget target) {
        baseAttributeInputField.add(getValidatorForAttribute(selectedBaseAttribute));
        dependentAttributeInputField.add(getValidatorForAttribute(selectedDependentAttribute));
        target.add(baseAttributeInputField);
        target.add(dependentAttributeInputField);
    }

    private IValidator<String> getValidatorForAttribute(TypeTreeNode attribute) {
        switch (attribute.getType()) {
            case INTEGER:
                return new IntegerRangeValidator();
            case STRING:
                return new PatternValidator("\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*(?:;\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*)*");
            case FLOAT:
                return new PatternValidator("\\d+(?:\\.\\d+)?(?:;\\d+(?:\\.\\d+)?)*");
            case DATE:
                return new DateRangeValidator();
            default:
                return new PatternValidator("");
        }
    }
}
