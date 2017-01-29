/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import org.apache.log4j.Logger;
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

public class GeneratePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private ReplayerPage page;
    private GeneratePanel panel;
    protected Integer eventCount = 10;
    private Form layoutForm;
    protected String eventTypeName;
    private static final Logger logger = Logger.getLogger(EventGenerator.class);
    private EapEventType selectedEventType = new EapEventType("test" );
    private ListView listview;
    private HashMap<TypeTreeNode, String> attributeInput = new HashMap<TypeTreeNode, String>();
    WebMarkupContainer listContainer;

    public GeneratePanel(String id, final ReplayerPage page) {
        super(id);
        this.page = page;
        this.panel = this;

        layoutForm = new Form("layoutForm") {

            @Override
            public void onSubmit() {
                EventGenerator eventGenerator = new EventGenerator();
                eventGenerator.generateEvents(eventCount, selectedEventType, attributeInput);
                success("Event(s) successfully created");
            }
        };
        this.add(layoutForm);

        addEventCountField();
        addEventTypeDropDown();
        addSubmitButton();
   }

    private void addEventCountField() {
        final TextField<Integer> eventCountField = new TextField<Integer>("eventCountField", new PropertyModel<Integer>(this, "eventCount"));
        layoutForm.add(eventCountField);
    }

    private void addEventTypeDropDown() {
        final List<EapEventType> eventTypes = EapEventType.findAll();

        LoadableDetachableModel list =  new LoadableDetachableModel()
        {
            protected Object load() {
                return selectedEventType.getValueTypes();
            }
        };
        listview = new ListView("listview", list) {
            protected void populateItem(ListItem item) {
                final TypeTreeNode attribute = (TypeTreeNode) item.getModelObject();
                item.add(new Label("attribute", attribute.getName()));
                if(attribute.getType() == null) {
                    item.add(new Label("attributeType", "UNDEFINED"));
                } else {
                    item.add(new Label("attributeType", attribute.getType().getName()));
                    item.add(new Label("attributeInputDescription", new StringResourceModel("description.${type}", this, new Model(attribute))));
                }
                IModel attributeInputModel = new Model<String>() {
                    @Override
                    public String getObject() {
                        return attributeInput.get(attribute);
                    }
                    @Override
                    public void setObject(String inputValue) {
                        attributeInput.put(attribute, inputValue);
                    }
                };

                TextField<String> inputField = new TextField<String>("attributeInput", attributeInputModel);
                switch (attribute.getType()) {
                    case INTEGER:
                        //TODO: Replace by own validator
                        inputField.add(new IntegerRangeValidator());
                        break;
                    case STRING:
                        //inputField.add(new PatternValidator("\\w+(?:;\\w+)*"));
                        inputField.add(new PatternValidator("\\w+(?:(?:\\s|\\-)\\w+)*(?:;\\w+(?:(?:\\s|\\-)\\w+)*)*"));
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
                item.add(inputField);
            }
        };
        listview.setReuseItems(true);

        listContainer = new WebMarkupContainer("theContainer");
        listContainer.add(listview);
        layoutForm.add(listContainer);
        listContainer.setOutputMarkupId(true);

        DropDownChoice dropDown = new DropDownChoice("eventTypeField", new PropertyModel( this, "selectedEventType" ), eventTypes);
        // Add Ajax Behaviour...
        dropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                if(selectedEventType != null) {
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

    public class IntegerRangeValidator implements IValidator<String> {

        private final String INTEGER_RANGE_PATTERN = "(?:\\d+(?:;\\d+)*|\\d+\\-\\d+)";
        private final Pattern pattern;

        IntegerRangeValidator() {
            pattern = Pattern.compile(INTEGER_RANGE_PATTERN);
        }

        @Override
        public void validate(IValidatable<String> validatable) {
            //get input from attached component
            final String input = validatable.getValue();
            if (pattern.matcher(input).matches() == true) {
                String[] splits;
                if (input.contains("-")) {
                    splits = input.split("-");
                    if(Integer.parseInt(splits[0]) > Integer.parseInt(splits[1])) error(validatable, "endBeforeStart");
                }
            } else {
                error(validatable,"noIntegerRange");
            }
        }

        private void error(IValidatable<String> validatable, String errorKey) {
            ValidationError error = new ValidationError();
            error.addKey(getClass().getSimpleName() + "." + errorKey);
            validatable.error(error);
        }
    }

    public class DateRangeValidator implements IValidator<String> {

        private final String DATE = "(\\d{4}\\/\\d{2}\\/\\d{2}T\\d{2}\\:\\d{2})";
        private final String DATE_RANGE_PATTERN = DATE + "||" + DATE + "-" + DATE;
        private final Pattern pattern;

        DateRangeValidator() {
            pattern = Pattern.compile(DATE_RANGE_PATTERN);
        }

        @Override
        public void validate(IValidatable<String> validatable) {
            //get input from attached component
            final String input = validatable.getValue();
            if (pattern.matcher(input).matches() == true) {
                String[] splits;
                if (input.contains("-")) {
                    splits = input.split("-");
                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd'T'HH:mm");
                    try {
                        if (formatter.parse(splits[0]).after(formatter.parse(splits[1])))
                            error(validatable, "endBeforeStart");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                error(validatable,"noIntegerRange");
            }
        }

        private void error(IValidatable<String> validatable, String errorKey) {
            ValidationError error = new ValidationError();
            error.addKey(getClass().getSimpleName() + "." + errorKey);
            validatable.error(error);
        }
    }
}
