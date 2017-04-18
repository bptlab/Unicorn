/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.generator.validation.AttributeValidator;
import de.hpi.unicorn.attributeDependency.AttributeDependency;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.attributeDependency.AttributeValueDependency;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.Validatable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * {@link Panel}, which allows the insertion of dependencies between event type attributes.
 */
public class DependenciesPanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private DependenciesPanel thisPanel;

    private Form dependencyForm;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test");
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
    private ArrayList<DependencyInput> dependencyValues = new ArrayList<>();
    private ListView<String> listview;
    private WebMarkupContainer listContainer;
    private boolean allSelected = false;

    private static final Logger logger = Logger.getLogger(DependenciesPanel.class);

    /**
     * Constructor for the dependencies thisPanel. The page is initialized in this method,
     * including the event type dropdown and the according event type attributes dropdowns.
     *
     * @param id initialized ID
     * @param page the page the panel belongs to
     */
    DependenciesPanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.thisPanel = this;
        dependencyForm = new Form("dependencyForm");
        this.add(dependencyForm);

        addDropDowns();
        addAddDependencyButton();
        addDeleteDependencyButton();
        addDependencyValuesInputs();
        addAddDependencyValuesButton();
        addListOfDependencies();
        addSubmitButton();
        addDeleteValuesButton();
    }

    /**
     * Create and add dropdown with existing event types.
     * When selected, the two dropdowns containing the attributes of the chosen event type will be updated.
     * Using the two dropdowns the base and dependent attribute can be selected.
     *
     */
    private void addDropDowns() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        final String ajaxUpdatingBehavior = "onchange";

        if (!eventTypes.isEmpty()) {
            selectedEventType = eventTypes.get(0);
            setFirstAttributes();
        }

        baseDropDown = new DropDownChoice<>("baseAttributeField", new PropertyModel<TypeTreeNode>(this,
                "selectedBaseAttribute"),
                selectedEventType.getValueTypes(), new ChoiceRenderer<>("name", "name"));

        dependentDropDown = new DropDownChoice<>("dependentAttributeField", new PropertyModel<TypeTreeNode>(this,
                "selectedDependentAttribute"),
                getDependentAttributeChoiceList(), new ChoiceRenderer<>("name", "name"));

        selectedBaseAttributeTypeLabel = new Label("selectedBaseAttributeType", new PropertyModel<String>(this,
                "selectedBaseAttribute.type"));
        selectedDependentAttributeTypeLabel = new Label("selectedDependentAttributeType", new PropertyModel<String>(this,
                "selectedDependentAttribute.type"));

        eventTypeDropDown = new DropDownChoice<>("eventTypeField", new PropertyModel<EapEventType>(this, "selectedEventType"),
                eventTypes);

        selectedBaseAttributeTypeLabel.setOutputMarkupId(true);
        selectedDependentAttributeTypeLabel.setOutputMarkupId(true);

        baseDropDown.setRequired(true);
        baseDropDown.setOutputMarkupId(true);
        dependentDropDown.setRequired(true);
        dependentDropDown.setOutputMarkupId(true);
        eventTypeDropDown.setRequired(true);
        eventTypeDropDown.setOutputMarkupId(true);

        baseDropDown.add(new AjaxFormComponentUpdatingBehavior(ajaxUpdatingBehavior) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedEventType != null) {
                    dependentDropDown.setChoices(getDependentAttributeChoiceList());
                    if (selectedBaseAttribute == selectedDependentAttribute) {
                        selectedDependentAttribute = dependentDropDown.getChoices().get(0);
                    }
                    target.add(dependentDropDown);
                    updateDependenciesMapForCurrentSelection();
                    updateLabelsAndList(target);
                }
            }
        });
        dependentDropDown.add(new AjaxFormComponentUpdatingBehavior(ajaxUpdatingBehavior) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedEventType != null) {
                    updateDependenciesMapForCurrentSelection();
                    updateLabelsAndList(target);
                }
            }
        });

        eventTypeDropDown.add(new AjaxFormComponentUpdatingBehavior(ajaxUpdatingBehavior) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedEventType != null) {
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

    /**
     * Create and add a button to set the attribute dependency and enable the dependency value fields.
     *
     */
    private void addAddDependencyButton() {
        addDependencyButton = new AjaxButton("addDependencyButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                if (selectedBaseAttribute == selectedDependentAttribute) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("An attribute can't depend on itself.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }

                //DISABLE DEPENDENCY FORM
                setEnablementForDropDowns(false);
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
                removeOldValidatorsFromInputFields();
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

    /**
     * Create and add a link to delete the selected attribute dependency.
     */
    private void addDeleteDependencyButton() {
        AjaxLink deleteDependencyButton = new AjaxLink<Void>("deleteDependencyButton") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                AttributeDependency dependency = AttributeDependency
                        .getAttributeDependencyIfExists(selectedEventType, selectedBaseAttribute, selectedDependentAttribute);
                if (dependency == null) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("Error while deleting dependency. "
                            + "Dependency is already deleted.");
                } else if (dependency.remove() == null) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("Error while deleting dependency. "
                            + "Please delete the corresponding values first.");
                } else {
                    DependenciesPanel.this.page.getFeedbackPanel().success("Dependency deleted.");
                }
                target.add(DependenciesPanel.this.page.getFeedbackPanel());
            }
        };
        dependencyForm.add(deleteDependencyButton);
    }

    /**
     * Create and add input fields for new values of the dependency.
     *
     */
    private void addDependencyValuesInputs() {
        baseAttributeInputField = new TextField<>("baseAttributeInput", new PropertyModel<String>(this,
                "currentBaseAttributeInput"));
        dependentAttributeInputField = new TextField<>("dependentAttributeInput", new PropertyModel<String>(this,
                "currentDependentAttributeInput"));
        baseAttributeInputField.setLabel(new Model<String>("Input for base attribute"));
        baseAttributeInputField.setOutputMarkupId(true);
        baseAttributeInputField.setEnabled(false);
        dependentAttributeInputField.setLabel(new Model<String>("Input for dependent attribute"));
        dependentAttributeInputField.setOutputMarkupId(true);
        dependentAttributeInputField.setEnabled(false);
        dependencyForm.add(baseAttributeInputField);
        dependencyForm.add(dependentAttributeInputField);
    }

    /**
     * Create and add a button to enter a new value dependency into the temporary map of dependency values.
     *
     */
    private void addAddDependencyValuesButton() {
        addDependencyValueButton = new AjaxButton("addDependencyValueButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                if (currentBaseAttributeInput == null || currentDependentAttributeInput == null
                        || currentBaseAttributeInput.isEmpty() || currentDependentAttributeInput.isEmpty()) {
                    DependenciesPanel.this.page.getFeedbackPanel().error("You must provide an input!");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                    return;
                }
                if (dependenciesInput.containsKey(currentBaseAttributeInput)) {
                    // We try to append the new input to the old one, using validators to make sure the map stays valid
                    Validatable<String> currentDependencyValue = new Validatable<>(
                            dependenciesInput.get(currentBaseAttributeInput) + ";" + currentDependentAttributeInput);
                    IValidator<String> newDependencyValueValidator = AttributeValidator.getValidatorForAttribute(selectedDependentAttribute);
                    newDependencyValueValidator.validate(currentDependencyValue);
                    if (currentDependencyValue.getErrors().isEmpty()) {
                        currentDependentAttributeInput = currentDependencyValue.getValue();
                    } else {
                        DependenciesPanel.this.page.getFeedbackPanel().error("Could not append the value to the existing dependency.");
                        target.add(DependenciesPanel.this.page.getFeedbackPanel());
                        return;
                    }
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
            @Override
            public void onError(AjaxRequestTarget target, Form form) {
                target.add(DependenciesPanel.this.page.getFeedbackPanel());
            }
        };
        addDependencyValueButton.setEnabled(false);
        dependencyForm.add(addDependencyValueButton);
    }

    /**
     * Create and add a container displaying labels with the selected attributes and the list with already submitted values.
     *
     */
    private void addListOfDependencies() {
        updateDependenciesMapForCurrentSelection();
        LoadableDetachableModel list =  new LoadableDetachableModel() {
            @Override
            protected Object load() {
                ArrayList<String> keys = new ArrayList<>(dependenciesInput.keySet());
                dependencyValues = new ArrayList<>();
                for (String key : keys) {
                    dependencyValues.add(new DependencyInput(key));
                }
                return dependencyValues;
            }
        };
        listContainer = new WebMarkupContainer("dependenciesContainer");
        listContainer.setOutputMarkupId(true);
        Model selectAllModel = new Model<Boolean>() {
            @Override
            public Boolean getObject() {
                return thisPanel.allSelected;
            }
            @Override
            public void setObject(Boolean bool) {
                thisPanel.allSelected = bool;
            }
        };
        listContainer.add(new AjaxCheckBox("selectAllCheckbox", selectAllModel) {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                for (DependencyInput input : dependencyValues) {
                    input.setSelected(thisPanel.allSelected);
                }
                listview.removeAll();
                updateDependenciesMapForCurrentSelection();
                target.add(listContainer);
            }
        });
        listview = new ListView<String>("dependenciesListview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final DependencyInput key = (DependencyInput) item.getModelObject();
                item.add(new Label("baseAttributeValue", key.baseValue));
                item.add(new Label("dependentAttributeValue", dependenciesInput.get(key.baseValue)));
                IModel<Boolean> dependencyInputModel = new Model<Boolean>() {
                    @Override
                    public Boolean getObject() {
                        if (thisPanel.allSelected) {
                            return true;
                        }
                        for (DependencyInput input : dependencyValues) {
                            if (input.baseValue.equals(key.baseValue)) {
                               return input.getSelected();
                            }
                        }
                        return false;
                    }
                    @Override
                    public void setObject(Boolean bool) {
                        for (DependencyInput input : dependencyValues) {
                            if (input.baseValue.equals(key.baseValue)) {
                                input.setSelected(bool);
                            }
                        }
                    }
                };
                item.add(new AjaxCheckBox("deleteCheckbox", dependencyInputModel) {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        // no special implementation
                    }
                });
            }
        };
        listview.setOutputMarkupId(true);
        listContainer.add(new Label(
                "selectedBaseAttributeLabel",
                new PropertyModel<String>(this, "selectedBaseAttribute.name")).setOutputMarkupId(true));
        listContainer.add(new Label(
                "selectedDependentAttributeLabel",
                new PropertyModel<String>(this, "selectedDependentAttribute.name")).setOutputMarkupId(true));
        listContainer.add(listview);
        dependencyForm.add(listContainer);
    }

    /**
     * Create and add the submit button to save the dependency and inserted dependency values.
     *
     */
    private void addSubmitButton() {
        submitButton = new AjaxButton("submitButton", this.dependencyForm) {
            private static final long serialVersionUID = 1L;
            @Override
            public void onSubmit(final AjaxRequestTarget target, final Form form) {
                AttributeDependency dependency = AttributeDependencyManager
                        .getAttributeDependency(selectedEventType, selectedBaseAttribute, selectedDependentAttribute);
                if (dependency.addDependencyValues(dependenciesInput)) {
                    DependenciesPanel.this.page.getFeedbackPanel().success("Submitted.");
                    target.add(DependenciesPanel.this.page.getFeedbackPanel());
                } else {
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

    /**
     * Create and add a link to delete the selected attribute value dependencies.
     */
    private void addDeleteValuesButton() {
        dependencyForm.add(new AjaxLink<Void>("deleteValuesButton") {
            private static final long serialVersionUID = 1L;

            @Override
            public void onClick(final AjaxRequestTarget target) {
                List<DependencyInput> valuesToRemove = new ArrayList<>();
                if (allSelected && !dependencyValues.isEmpty()) {
                    valuesToRemove.addAll(dependencyValues);
                }

                for (DependencyInput dependencyValue : dependencyValues) {
                    if (dependencyValue.selected) {
                        valuesToRemove.add(dependencyValue);
                    }
                }
                try {
                    for (DependencyInput valueToRemove : valuesToRemove) {
                        AttributeDependency dependency = AttributeDependencyManager.getAttributeDependency(
                                selectedEventType,
                                selectedBaseAttribute,
                                selectedDependentAttribute);
                        AttributeValueDependency value = AttributeValueDependency.getAttributeValueDependencyFor(
                                dependency,
                                valueToRemove.baseValue);
                        if (value != null) {
                            value.remove();
                            dependencyValues.remove(valueToRemove);
                            DependenciesPanel.this.page.getFeedbackPanel().success("Submitted.");
                            target.add(DependenciesPanel.this.page.getFeedbackPanel());
                        } else {
                            DependenciesPanel.this.page.getFeedbackPanel().error("Error while deleting dependencies. Dependency is already deleted.");
                            target.add(DependenciesPanel.this.page.getFeedbackPanel());
                        }
                    }
                } catch (Exception e) {
                    logger.debug("addDeleteValuesButton", e);
                }
                listview.removeAll();
                updateDependenciesMapForCurrentSelection();
                target.add(listContainer);
            }
        });
    }

    /**
     * Try to initialize the base and dependent attribute with the first to attributes of the chosen event type.
     *
     */
    private void setFirstAttributes() {
        final int minNumAttributes = 2;
        if (selectedEventType.getValueTypes().size() >= minNumAttributes) {
            selectedBaseAttribute = selectedEventType.getValueTypes().get(0);
            selectedDependentAttribute = selectedEventType.getValueTypes().get(1);
        }
    }

    /**
     * set the enablement of all dropdowns in the form.
     *
     * @param enabled defines if the dropdown should be enabled (true) or disabled (false)
     */
    private void setEnablementForDropDowns(Boolean enabled) {
        eventTypeDropDown.setEnabled(enabled);
        baseDropDown.setEnabled(enabled);
        dependentDropDown.setEnabled(enabled);
    }

    /**
     * Builds a List containing all attributes of the chosen event type but the attribute select as base attribute for the dependency.
     *
     * @return a list of attributes trimmed by the base attribute
     */
    private List<TypeTreeNode> getDependentAttributeChoiceList() {
        List<TypeTreeNode> attributesWithoutBaseAttribute = selectedEventType.getValueTypes();
        if (attributesWithoutBaseAttribute.contains(selectedBaseAttribute)) {
            attributesWithoutBaseAttribute.remove(selectedBaseAttribute);
        }
        return attributesWithoutBaseAttribute;
    }

    /**
     * Update the list containing the values map and the labels displaying the types of the chosen attributes.
     * It will also update the given ajax target.
     *
     * @param target to be updated
     */
    private void updateLabelsAndList(AjaxRequestTarget target) {
        listview.removeAll();
        target.add(selectedBaseAttributeTypeLabel);
        target.add(selectedDependentAttributeTypeLabel);
        target.add(listContainer);
    }

    /**
     * Creates a new dependency-value map and tries to load attribute value dependencies that already exist for the given attribute dependency,
     * into the map.
     *
     */
    private void updateDependenciesMapForCurrentSelection() {
        dependenciesInput = new HashMap<>();
        AttributeDependency attributeDependency = AttributeDependency
                .getAttributeDependencyIfExists(selectedEventType, selectedBaseAttribute, selectedDependentAttribute);
        if (attributeDependency != null) {
            for (AttributeValueDependency attributeValueDependency : AttributeValueDependency
                    .getAttributeValueDependenciesFor(attributeDependency)) {
                dependenciesInput.put(attributeValueDependency.getBaseAttributeValue(), attributeValueDependency.getDependentAttributeValues());
            }
        }
    }

    /**
     * Set input validators for the two input fields (base and dependent attribute value).
     * The given ajax target will be updated for these two fields too.
     *
     * @param target to be updated
     */
    private void setValidatorsForInputFields(AjaxRequestTarget target) {
        baseAttributeInputField.add(AttributeValidator.getValidatorForAttribute(selectedBaseAttribute));
        dependentAttributeInputField.add(AttributeValidator.getValidatorForAttribute(selectedDependentAttribute));
        target.add(baseAttributeInputField);
        target.add(dependentAttributeInputField);
    }

    /**
     * Remove the old input validators from input field,
     * in case they are different to the ones needed when updated.
     */
    private void removeOldValidatorsFromInputFields() {
        for (IValidator validator : baseAttributeInputField.getValidators()) {
            baseAttributeInputField.remove(validator);
        }
        for (IValidator validator : dependentAttributeInputField.getValidators()) {
            dependentAttributeInputField.remove(validator);
        }
    }

    /**
     * Class to indicate list items by their base value and the status of their checkbox.
     */
    private class DependencyInput implements Serializable {
        private String baseValue;
        private Boolean selected;

        /**
         * Constructor for the DependencyInput class.
         *
         * @param baseValue base value of the input
         */
        DependencyInput(String baseValue) {
            this.baseValue = baseValue;
            this.selected = false;
        }

        public boolean getSelected() { return this.selected; }
        public void setSelected(Boolean bool) { this.selected = bool; }
        public String getBaseValue() { return this.baseValue; }
        public void setBaseValue(String newValue) { this.baseValue = newValue; }
    }

}
