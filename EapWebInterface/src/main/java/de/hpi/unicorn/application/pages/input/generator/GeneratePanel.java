/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.hpi.unicorn.attributeDependency.AttributeDependencyManager;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.*;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.validator.PatternValidator;
import org.apache.wicket.validation.ValidationError;
import org.apache.wicket.validation.validator.RangeValidator;

/**
 * {@link Panel}, which allows the generation of events.
 */
public class GeneratePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private GeneratorPage page;
    private GeneratePanel panel;
    private Integer eventCount = 10;
    private Integer scaleFactor = 10000;
    private String eventTimestamps;
    private Form layoutForm;
    protected String eventTypeName;
    private EapEventType selectedEventType = new EapEventType("test" );
    private ListView<TypeTreeNode> listview;
    private HashMap<TypeTreeNode, String> attributeInput = new HashMap<>();
    private WebMarkupContainer listContainer;
    private AttributeDependencyManager attributeDependencyManager;
    private final List<EapEventType> eventTypes = EapEventType.findAll();

    /**
     * Constructor for the generate panel. The page is initialized in this method,
     * including the event type dropdown and the according list of input fields.
     *
     * @param id
     * @param page
     */
    GeneratePanel(String id, final GeneratorPage page) {
        super(id);
        this.page = page;
        this.panel = this;

        layoutForm = new Form("layoutForm") {

            @Override
            public void onSubmit() {
                EventGenerator eventGenerator = new EventGenerator();
                if(eventTimestamps == null) {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInput);
                }
                else {
                    eventGenerator.generateEvents(eventCount, scaleFactor, selectedEventType, attributeInput, eventTimestamps);
                }
                success("Event(s) successfully created");
            }
        };
        this.add(layoutForm);

        if(eventTypes.isEmpty()) {
            selectedEventType = new EapEventType("test");
        }
        else {
            selectedEventType = eventTypes.get(0);
        }
        attributeDependencyManager = new AttributeDependencyManager(selectedEventType);

        addEventCountField();
        addScaleFactorField();
        addTimestampField();
        addEventTypeDropDown();
        addSubmitButton();
   }

    private void addEventCountField() {
        final TextField<Integer> eventCountField = new TextField<>("eventCountField", new PropertyModel<Integer>(this, "eventCount"));
        eventCountField.setRequired(true);
        eventCountField.add(new RangeValidator<Integer>(1,100));
        layoutForm.add(eventCountField);
    }

    private void addScaleFactorField() {
        final TextField<Integer> scaleFactorField = new TextField<>("scaleFactorField", new PropertyModel<Integer>(this, "scaleFactor"));
        scaleFactorField.setRequired(true);
        layoutForm.add(scaleFactorField);
    }

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
        LoadableDetachableModel list =  new LoadableDetachableModel()
        {
            @Override
            protected Object load() {
                return selectedEventType.getValueTypes();
            }
        };
        listview = new ListView<TypeTreeNode>("listview", list) {
            @Override
            protected void populateItem(ListItem item) {
                final TypeTreeNode attribute = (TypeTreeNode) item.getModelObject();
                item.add(new Label("attribute", attribute.getName()));
                if(attribute.getType() == null) {
                    attribute.setType(AttributeTypeEnum.STRING);
                    item.add(new Label("attributeType", "UNDEFINED"));
                    item.add(new Label("attributeInputDescription", getString("description.Undefined")));
                } else {
                    item.add(new Label("attributeType", attribute.getType().getName()));
                    item.add(new Label("attributeInputDescription", new StringResourceModel("description.${type}", this, new Model<TypeTreeNode>(attribute))));
                }
                IModel<String> attributeInputModel = new Model<String>() {
                    @Override
                    public String getObject() {
                        return attributeInput.get(attribute);
                    }
                    @Override
                    public void setObject(String inputValue) {
                        attributeInput.put(attribute, inputValue);
                    }
                };

                TextField<String> inputField = new TextField<>("attributeInput", attributeInputModel);
                switch (attribute.getType()) {
                    case INTEGER:
                        inputField.add(new IntegerRangeValidator());
                        break;
                    case STRING:
                        inputField.add(new PatternValidator("\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*(?:;\\w+(?:(?:\\s|\\-|\\,\\s)\\w+)*)*"));
                        break;
                    case FLOAT:
                        inputField.add(new PatternValidator("\\d+(?:\\.\\d+)?(?:;\\d+(?:\\.\\d+)?)*"));
                        break;
                    case DATE:
                        inputField.add(new DateRangeValidator());
                        break;
                    default:
                        inputField.add(new PatternValidator(""));
                        break;
                }
                inputField.setLabel(new Model<String>(attribute.getName()));
                inputField.setRequired(true);
                item.add(inputField);
                item.add(new Label("attributeInputWarning", "").setVisible(attributeDependencyManager.isDependentAttributeInDependency(attribute)));
            }
        };
        listview.setReuseItems(true);

        listContainer = new WebMarkupContainer("theContainer");
        listContainer.add(listview);
        layoutForm.add(listContainer);
        listContainer.setOutputMarkupId(true);

        DropDownChoice<EapEventType> dropDown = new DropDownChoice<>("eventTypeField", new PropertyModel<EapEventType>( this, "selectedEventType" ),
                eventTypes);
        dropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            @Override
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
                    attributeDependencyManager = new AttributeDependencyManager(selectedEventType);
                    listview.removeAll();
                    target.add(listContainer);
                }
            }
        });
        layoutForm.add(dropDown);
    }

    private void addSubmitButton() {
        final Button submitButton = new Button("submitButton");
        layoutForm.add(submitButton);
    }
}
