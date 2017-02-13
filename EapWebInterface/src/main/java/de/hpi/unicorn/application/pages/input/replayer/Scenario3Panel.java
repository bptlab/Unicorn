/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.input.replayer.EventReplayer.TimeMode;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;

/**
 * Panel representing the content panel for the first tab.
 */
public class Scenario3Panel extends Panel {

	private static final long serialVersionUID = 573672364803879784L;
	private CheckBoxMultipleChoice<String> eventsCheckBoxMultipleChoice;
	private FilesPanel panel;
	private List<String> selectedFiles = new ArrayList<String>();
	private TextField<String> scaleFactorInput;
	private String scaleFactor = "100";
	private boolean useCurrentTime = false;

	public Scenario3Panel(final String id, final FilesPanel panel) {
		super(id);

		this.panel = panel;

		final Form<Void> form = new WarnOnExitForm("replayerForm");
		this.add(form);

		this.addEventsCheckBoxMultipleChoice(form);
		this.addCurrentTimeCheckBox(form);
		this.addScaleFactorTextField(form);
		this.addReplayButton(form);
	}

	private void addCurrentTimeCheckBox(Form<Void> layoutForm) {
		final CheckBox checkBox = new CheckBox("useCurrentTimeCheckBox", Model.of(useCurrentTime));
		checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				useCurrentTime = checkBox.getModelObject();
			}
		});
		checkBox.setOutputMarkupId(true);
		layoutForm.add(checkBox);
	}

	private void addReplayButton(Form<Void> form) {
		// confirm button
		final AjaxButton confirmButton = new AjaxButton("replayButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);

				List<EapEvent> events = new ArrayList<EapEvent>();

				EapEventType et = EapEventType.findByTypeName("Shiptrace");

				for (String s : selectedFiles) {
					if (s.equals("shipToEnns")) {
						events.addAll(panel.generateEventsFromCSV(this.getClass().getResource("/shipToEnns.csv")
								.getPath(), et));
					}
					if (s.equals("shipToEnns_additionalKremsToEnns")) {
						events.addAll(panel.generateEventsFromCSV(
								this.getClass().getResource("/shipToEnns_additionalKremsToEnns.txt").getPath(), et));
					}
				}

				panel.replayEvents(events, Integer.parseInt(scaleFactor), "GET Scenario 3",
						new ArrayList<ReplayFileBean>(), TimeMode.UNCHANGED, null, null);
				panel.getParentPage().getFeedbackPanel()
						.success("Replayer started for GET Scenario 3 - files: " + selectedFiles + ".");
			}
		};

		form.add(confirmButton);
	}

	private void addScaleFactorTextField(Form<Void> form) {
		this.scaleFactorInput = new TextField<String>("scaleFactorInput",
				new PropertyModel<String>(this, "scaleFactor"));
		this.scaleFactorInput.setOutputMarkupId(true);
		form.add(this.scaleFactorInput);
	}

	private void addEventsCheckBoxMultipleChoice(final Form<Void> layoutForm) {

		final List<String> fileNames = Arrays.asList("shipToEnns", "shipToEnns_additionalKremsToEnns");

		this.eventsCheckBoxMultipleChoice = new CheckBoxMultipleChoice<String>("eventsCheckBoxMultipleChoice",
				new PropertyModel<List<String>>(this, "selectedFiles"), fileNames);
		this.eventsCheckBoxMultipleChoice.setOutputMarkupId(true);
		layoutForm.add(this.eventsCheckBoxMultipleChoice);
	}

};
