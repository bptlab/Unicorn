/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de.
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.TempFolderUtil;
import org.apache.log4j.Logger;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.IModel;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PropertyListView;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.WebMarkupContainer;

import de.hpi.unicorn.application.pages.input.replayer.ReplayFileBean.FileType;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.TempFolderUtil;

public class GeneratePanel extends Panel {

    private static final long serialVersionUID = 1L;
    private ReplayerPage page;
    private GeneratePanel panel;
    protected String eventCount;
    private Form layoutForm;
    protected String eventTypeName;
    private static final Logger logger = Logger.getLogger(EventGenerator.class);
    private EapEventType temp = new EapEventType("blah");
    private EapEventType temp2;
    private ListView listview;

    public GeneratePanel(String id, final ReplayerPage page) {
        super(id);
        this.page = page;
        this.panel = this;

        layoutForm = new Form("layoutForm") {

            @Override
            public void onSubmit() {
                try {
                    int eventCountInt = Integer.parseInt(eventCount);
                    EventGenerator eventGenerator = new EventGenerator();
                    eventGenerator.generateEvents(eventCountInt);
                } catch (NumberFormatException e) {
                    page.getFeedbackPanel().error("Event Count needs to be an integer.");
                }
            }
        };
        this.add(layoutForm);

        addEventCountField();
        //ListView listView = addEmptyListView();
        //WebMarkupContainer listContainer = addEmptyListContainer(listView);
        addEventTypeField();
        addSubmitButton();
    }

    private void addEventCountField() {
        final TextField<String> eventCountField = new TextField<String>("eventCountField", new Model<String>());
        eventCountField.setOutputMarkupId(true);
        eventCountField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                eventCount = eventCountField.getModelObject();
            }
        });
        layoutForm.add(eventCountField);
    }

    private void addEventTypeField() {
       /* List<EapEventType> eventTypes = EapEventType.findAll();
        IModel listModel=new LoadableDetachableModel() {
            protected Object load() {
                return temp.getEventAttributes();
            }
        };
        layoutForm.add(new ListView("listview", listModel) {
            protected void populateItem(ListItem item) {
                logger.info("Processing attribute: " + item);
                item.add(new Label("attribute", item.getModel()));
            }
        });


        DropDownChoice dropDown = new DropDownChoice("eventTypeField", new PropertyModel( this, "temp" ), eventTypes);
        layoutForm.add(dropDown);

*/

        final List<EapEventType> eventTypes = EapEventType.findAll();

        LoadableDetachableModel list =  new LoadableDetachableModel()
        {
            protected Object load() {
                return temp.getEventAttributes();
            }
        };
        logger.info("Creating new viewList with attributes: " + temp.getEventAttributes() );
        listview = new ListView("listview", list) {
            protected void populateItem(ListItem item) {
                logger.info("Processing attribute: " + item);
                item.add(new Label("attribute", item.getModel()));
            }
        };
        layoutForm.add(listview);

        DropDownChoice dropDown = new DropDownChoice("eventTypeField", new PropertyModel( this, "temp" ), eventTypes);
        // Add Ajax Behaviour...
        dropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            protected void onUpdate(AjaxRequestTarget target) {
                if(temp != null) {

                    //List<String> list = temp.getEventAttributes();


                   /* WebMarkupContainer listContainer = new WebMarkupContainer("theContainer");
                    listContainer.setOutputMarkupId(true);
                    //listContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
                    listContainer.add(listview);
                    //generate a markup-id so the contents can be updated through an AJAX call
                    listContainer.setOutputMarkupId(true);
                    //listContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
                    // add the list view to the container
                    listContainer.add(listview);
                    // finally add the container to the page*/
                    listview.setOutputMarkupId(true);
                    target.add(listview);
                }
            }
        });
        layoutForm.add(dropDown);
    }

    private void addSubmitButton() {
        final Button submitButton = new Button("submitButton");
        layoutForm.add(submitButton);
    }
    private ListView addEmptyListView() {
        List<String> empty = new ArrayList<String>();
        ListView listview = new ListView("listview", empty) {
            protected void populateItem(ListItem item) {
                item.add(new Label("attribute", item.getModel()));
            }
        };
        return listview;
    }

    private WebMarkupContainer addEmptyListContainer(ListView listView) {

        WebMarkupContainer listContainer = new WebMarkupContainer("theContainer");
        listContainer.setOutputMarkupId(true);
        //listContainer.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)));
        listContainer.add(listView);
        layoutForm.add(listContainer);
        //layoutForm.add(listview);
        return listContainer;
    }
}
