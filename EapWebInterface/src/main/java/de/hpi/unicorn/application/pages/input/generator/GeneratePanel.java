/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.application.pages.input.generator.attributeInput.AttributeInput;
import de.hpi.unicorn.application.pages.input.generator.validation.DateRangeValidator;
import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.StringResourceModel;

import java.util.List;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.validation.validator.RangeValidator;

import java.util.ArrayList;

/**
 * {@link Panel}, which allows the generation of events.
 */
public class GeneratePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private GeneratePanel panel;
    private static final Integer DEFAULT_EVENTCOUNT = 10;
    private static final Integer DEFAULT_SCALEFACTOR = 10000;
    private static final Integer MAXIMUM_EVENTCOUNT = 100;
    private Integer eventCount = DEFAULT_EVENTCOUNT;
    private Integer scaleFactor = DEFAULT_SCALEFACTOR;
    private String eventTimestamps;
    private Form layoutForm;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test");
    private ListView<TypeTreeNode> listview;
    private List<AttributeInput> attributeInputs = new ArrayList<>();
    private WebMarkupContainer listContainer;
    private AttributeDependencyManager attributeDependencyManager;
    private final List<EapEventType> eventTypes = EapEventType.findAll();

    /**
     * Constructor for the generate panel. The page is initialized in this method,
     * including the event type dropdown and the according list of input fields.
     *
     * @param id initialized ID
     * @param page the page the panel belongs to
     */
    GeneratePanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;

        layoutForm = new Form("layoutForm") {

            @Override
            public void onSubmit() {
                EventGenerator eventGenerator = new EventGenerator();
                if (eventTimestamps == null) {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInputs);
                }
                else {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInputs, eventTimestamps);
                }
                success("Event(s) successfully created");
            }
        };
        this.add(layoutForm);

        if (eventTypes.isEmpty()) {
            selectedEventType = new EapEventType("test");
        } else {
            selectedEventType = eventTypes.get(0);
        }
        attributeDependencyManager = new AttributeDependencyManager(selectedEventType);

        addEventCountField();
        addScaleFactorField();
        addTimestampField();
        addEventTypeDropDown();
        addSubmitButton();
   }

    /**
     * Add field to specify the number of events to generate.
     */
    private void addEventCountField() {
        final TextField<Integer> eventCountField = new TextField<>("eventCountField", new PropertyModel<Integer>(this, "eventCount"));
        eventCountField.setRequired(true);
        eventCountField.add(new RangeValidator<Integer>(1, MAXIMUM_EVENTCOUNT));
        layoutForm.add(eventCountField);
    }

    /**
     * Add field to specify the scale factor to replay with.
     */
    private void addScaleFactorField() {
        final TextField<Integer> scaleFactorField = new TextField<>("scaleFactorField", new PropertyModel<Integer>(this, "scaleFactor"));
        scaleFactorField.setRequired(true);
        layoutForm.add(scaleFactorField);
    }

    /**
     * Add field to specify the timestamp of the events to generate.
     */
    private void addTimestampField() {
        final TextField<String> timestampField = new TextField<>("timestampField", new PropertyModel<String>(this, "eventTimestamps"));
        timestampField.setLabel(new Model<String>("Timestamp"));
        timestampField.add(new DateRangeValidator());
        layoutForm.add(timestampField);
    }


    /**
     * Adds dropdown with existing event types.
     * When selected, a list is generated that contains the attributes of the selected event types and input fields to fill in values.
     * Every input field gets a validator.
     * Additionally a description is provided so that the user knows the format of input that has to be used.
     */
    private void addEventTypeDropDown() {
        LoadableDetachableModel list =  new LoadableDetachableModel() {
            @Override
            protected Object load() {
                return selectedEventType.getValueTypes();
            }
        };
        listview = new ListView<TypeTreeNode>("listview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final TypeTreeNode attribute = (TypeTreeNode) item.getModelObject();
                final AttributeInput attributeInput = AttributeInput.attributeInputFactory(attribute);
                item.add(new Label("attribute", attributeInput.getAttributeName()));
                item.add(new Label("attributeType", attributeInput.getAttributeType().getName()));
                final Label attributeInputDescriptionLabel = new Label("attributeInputDescription", getAttributeInputDescription(attributeInput));
                attributeInputDescriptionLabel.setOutputMarkupId(true);
                item.add(attributeInputDescriptionLabel);
                attributeInputs.add(attributeInput);
                IModel<String> attributeInputModel = new Model<String>() {
                    @Override
                    public String getObject() {
                        int indexOfAttributeInput = attributeInputs.indexOf(attributeInput);
                        return attributeInputs.get(indexOfAttributeInput).getInput();
                    }
                    @Override
                    public void setObject(String inputValue) {
                        int indexOfAttributeInput = attributeInputs.indexOf(attributeInput);
                        attributeInputs.get(indexOfAttributeInput).setInput(inputValue);
                    }
                };

                final TextField<String> inputField = new TextField<>("attributeInput", attributeInputModel);
                inputField.add(attributeInput.getAttributeInputValidator());
                inputField.setLabel(new Model<String>(attribute.getName()));
                inputField.setOutputMarkupId(true);
                item.add(inputField);
                Boolean isDependentAttribute = attributeDependencyManager.isDependentAttributeInAnyDependency(attribute);
                item.add(new Label("attributeInputWarning", "").setVisible(isDependentAttribute));

                DropDownChoice<AttributeInput.ProbabilityDistributionEnum> methodDropDown = new DropDownChoice<>(
                        "attributeInputMethodSelection",
                        new PropertyModel<AttributeInput.ProbabilityDistributionEnum>(attributeInput, "selectedMethod"),
                        attributeInput.getAvailableMethods());
                methodDropDown.add(new AjaxFormComponentUpdatingBehavior("onChange") {
                    @Override
                    protected void onUpdate(AjaxRequestTarget target) {
                        attributeInputDescriptionLabel.detachModels();
                        attributeInputDescriptionLabel.setDefaultModel(getAttributeInputDescription(attributeInput));
                        inputField.remove(inputField.getValidators().get(0));
                        inputField.add(attributeInput.getAttributeInputValidator());
                        target.add(attributeInputDescriptionLabel);
                        target.add(inputField);
                    }
                });
                methodDropDown.setVisible(attributeInput.hasDifferentMethods());
                item.add(methodDropDown);
            }
        };
        listview.setReuseItems(true);

        listContainer = new WebMarkupContainer("theContainer");
        listContainer.add(listview);
        layoutForm.add(listContainer);
        listContainer.setOutputMarkupId(true);

        DropDownChoice<EapEventType> dropDown = new DropDownChoice<>("eventTypeField",
                new PropertyModel<EapEventType>( this, "selectedEventType" ), eventTypes);
        dropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if (selectedEventType != null) {
                    attributeDependencyManager = new AttributeDependencyManager(selectedEventType);
                    attributeInputs.clear();
                    listview.removeAll();
                    target.add(listContainer);
                }
            }
        });
        layoutForm.add(dropDown);
    }

    /**
     * Creates a model used for retrieving the description text for the user input from the properties file.
     *
     * @param attributeInput the model should be created for
     * @return a StringResourceModel fitting the given attributeInput
     */
    private StringResourceModel getAttributeInputDescription(AttributeInput attributeInput) {
        StringResourceModel inputDescriptionModel = new StringResourceModel("description.${type}", this,
                new Model<TypeTreeNode>(attributeInput.getAttribute()));
        if (attributeInput.hasDifferentMethods()) {
            IModel<AttributeInput> attributeInputIModel = new Model<>(attributeInput);
            inputDescriptionModel = new StringResourceModel("description.${attributeType}.${selectedMethod}", this,
                    attributeInputIModel
            );
        }
        return inputDescriptionModel;
    }

    /**
     * Add a button to submit the form.
     */
    private void addSubmitButton() {
        final Button submitButton = new Button("submitButton");
        layoutForm.add(submitButton);
    }
}
