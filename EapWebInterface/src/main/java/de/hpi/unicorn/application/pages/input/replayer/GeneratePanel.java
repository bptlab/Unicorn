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

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
        final List<String> eventTypes = EapEventType.getAllTypeNames();
        if (!eventTypes.isEmpty()) {
            eventTypeName = eventTypes.get(0);
        }
        final DropDownChoice<String> eventTypeDropDownChoice = new DropDownChoice<String>("eventTypeField", new Model<String>(), eventTypes);
        eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
            private static final long serialVersionUID = 1L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                eventTypeName = eventTypeDropDownChoice.getModelObject();
            }

            protected boolean wantOnSelectionChangedNotifications() {
                return true;
            }

            protected void onSelectionChanged(final Object selectedEventType) {
                EapEventType eventType = (EapEventType) selectedEventType;
                logger.info("EVENT TYPE: " + eventType.getTypeName());
            }
        });
        if (!eventTypes.isEmpty()) {
            eventTypeDropDownChoice.setModelObject(eventTypes.get(0));
        }
        eventTypeDropDownChoice.setOutputMarkupId(true);
        layoutForm.add(eventTypeDropDownChoice);


    }

    private void addSubmitButton() {
        final Button submitButton = new Button("submitButton");
        layoutForm.add(submitButton);
    }
}
