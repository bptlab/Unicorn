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
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;

import java.util.*;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;

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
                }
                else {
                    item.add(new Label("attributeType", attribute.getType().getName()));
                }
                IModel attributeInputModel = new Model<String>() {
                    @Override
                    public String getObject() {
                        return attributeInput.get(attribute);
                    };
                    @Override
                    public void setObject(String inputValue) {
                        attributeInput.put(attribute, inputValue);
                    };
                };
                item.add(new TextField<String>("attributeInput", attributeInputModel));
            }
        };

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
}
