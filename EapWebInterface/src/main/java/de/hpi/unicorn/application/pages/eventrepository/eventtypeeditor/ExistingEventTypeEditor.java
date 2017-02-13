/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.form.palette.Palette;
import org.apache.wicket.extensions.markup.html.form.palette.component.Recorder;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.model.util.ListModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.ConditionInputPanel;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.EventTypeRule;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.notification.EventCondition;

/**
 * This page allows the creation of new {@link EapEventType}s from existing
 * ones.
 * 
 * @author micha
 */
public class ExistingEventTypeEditor extends Panel {

	private static final long serialVersionUID = 1L;
	private ConditionInputPanel conditionInput;
	private TextField<String> eventTypeInput;
	private Palette<TypeTreeNode> relevantEventTypeColumnsPalette;
	private final AbstractEapPage abstractEapPage;
	private CheckBoxMultipleChoice<EapEventType> eventTypesCheckBoxMultipleChoice;
	private final List<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
	private List<TypeTreeNode> selectedEventTypeAttributes = new ArrayList<TypeTreeNode>();
	private final List<TypeTreeNode> commonCorrelationAttributes = new ArrayList<TypeTreeNode>();
	private final List<EapEventType> eventTypes = EapEventType.findAll();

	/**
	 * Constructor for a page to create new {@link EapEventType}s from existing
	 * ones.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public ExistingEventTypeEditor(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.abstractEapPage = abstractEapPage;

		final Form<Void> layoutForm = new WarnOnExitForm("layoutForm");
		this.add(layoutForm);

		layoutForm.add(this.addEventTypeInput());

		layoutForm.add(this.addConditionInput());

		layoutForm.add(this.addExistingEventTypeSelect());

		layoutForm.add(this.addRelevantEventTypeColumnsPalette());

		this.addButtonsToForm(layoutForm);
	}

	private Component addEventTypeInput() {
		this.eventTypeInput = new TextField<String>("eventTypeInput", Model.of(""));
		this.eventTypeInput.setOutputMarkupId(true);
		return this.eventTypeInput;
	}

	private Component addConditionInput() {
		this.conditionInput = new ConditionInputPanel("conditionInput", true);
		this.conditionInput.setOutputMarkupId(true);
		return this.conditionInput;
	}

	private Component addExistingEventTypeSelect() {

		this.eventTypesCheckBoxMultipleChoice = new CheckBoxMultipleChoice<EapEventType>(
				"eventTypesCheckBoxMultipleChoice", new PropertyModel<ArrayList<EapEventType>>(this,
						"selectedEventTypes"), this.eventTypes) {
			@Override
			protected boolean isDisabled(final EapEventType eventType, final int index, final String selected) {
				// true for event types without matching attributes
				if (ExistingEventTypeEditor.this.selectedEventTypes.isEmpty()) {
					return false;
				} else {
					for (final TypeTreeNode commonAttribute : ExistingEventTypeEditor.this.commonCorrelationAttributes) {
						if (eventType.getValueTypes().contains(commonAttribute)) {
							return false;
						}
					}
					return true;
				}
			}
		};
		this.eventTypesCheckBoxMultipleChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ExistingEventTypeEditor.this.commonCorrelationAttributes.clear();
				if (!ExistingEventTypeEditor.this.selectedEventTypes.isEmpty()) {
					ExistingEventTypeEditor.this.commonCorrelationAttributes
							.addAll(ExistingEventTypeEditor.this.selectedEventTypes.get(0).getValueTypes());
					for (final EapEventType actualEventType : ExistingEventTypeEditor.this.selectedEventTypes) {
						ExistingEventTypeEditor.this.commonCorrelationAttributes.retainAll(actualEventType
								.getValueTypes());
					}
				}
				ExistingEventTypeEditor.this.conditionInput
						.setSelectedEventTypes(ExistingEventTypeEditor.this.selectedEventTypes);
				ExistingEventTypeEditor.this.conditionInput.updateAttributesValues();
				target.add(ExistingEventTypeEditor.this.conditionInput.getConditionAttributeSelect());
				target.add(ExistingEventTypeEditor.this.conditionInput.getConditionValueSelect());
				target.add(ExistingEventTypeEditor.this.relevantEventTypeColumnsPalette);
				target.add(ExistingEventTypeEditor.this.eventTypesCheckBoxMultipleChoice);
			}
		});
		this.eventTypesCheckBoxMultipleChoice.setOutputMarkupId(true);
		return this.eventTypesCheckBoxMultipleChoice;
	}

	private Component addRelevantEventTypeColumnsPalette() {
		final IModel<List<? extends TypeTreeNode>> eventTypeAttributeModel = new AbstractReadOnlyModel<List<? extends TypeTreeNode>>() {
			@Override
			public List<TypeTreeNode> getObject() {
				// in the columnsPalette should only be attributes, that are
				// contained in all selected event Types
				final Set<TypeTreeNode> attributes = new HashSet<TypeTreeNode>();
				boolean first = true;
				for (final EapEventType eventType : ExistingEventTypeEditor.this.selectedEventTypes) {
					if (eventType != null) {
						if (first) {
							attributes.addAll(eventType.getValueTypes());
							first = false;
						} else {
							attributes.retainAll(eventType.getValueTypes());
						}
					}
				}
				return new ArrayList<TypeTreeNode>(attributes);
			}
		};

		this.relevantEventTypeColumnsPalette = new Palette<TypeTreeNode>("relevantEventTypePalette",
				new ListModel<TypeTreeNode>(new ArrayList<TypeTreeNode>()), eventTypeAttributeModel,
				new ChoiceRenderer(), 5, false) {

			private static final long serialVersionUID = 1L;

			@Override
			protected Recorder newRecorderComponent() {
				final Recorder recorder = super.newRecorderComponent();
				recorder.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						final Iterator<TypeTreeNode> selectedColumns = getSelectedChoices();
						ExistingEventTypeEditor.this.selectedEventTypeAttributes = new ArrayList<TypeTreeNode>();
						while (selectedColumns.hasNext()) {
							final TypeTreeNode eventTypeAttribute = selectedColumns.next();
							ExistingEventTypeEditor.this.selectedEventTypeAttributes.add(eventTypeAttribute);
						}

					}
				});
				return recorder;
			}
		};
		this.relevantEventTypeColumnsPalette.setOutputMarkupId(true);
		return this.relevantEventTypeColumnsPalette;
	}

	private void addButtonsToForm(final Form layoutForm) {
		final AjaxButton clearButton = new AjaxButton("clearButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				ExistingEventTypeEditor.this.eventTypeInput.setModel(Model.of(""));
				ExistingEventTypeEditor.this.conditionInput.setSelectedEventTypes(new ArrayList());
				target.add(ExistingEventTypeEditor.this.conditionInput);
				target.add(ExistingEventTypeEditor.this.eventTypeInput);
			}
		};

		layoutForm.add(clearButton);

		final AjaxButton deleteButton = new AjaxButton("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				for (final EapEventType selectedEventType : ExistingEventTypeEditor.this.selectedEventTypes) {
					Broker.getEventAdministrator().removeEventType(selectedEventType);
				}
				target.add(ExistingEventTypeEditor.this.eventTypesCheckBoxMultipleChoice);
			}
		};

		layoutForm.add(deleteButton);

		final BlockingAjaxButton createButton = new BlockingAjaxButton("createButton", layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String newEventTypeName = ExistingEventTypeEditor.this.eventTypeInput.getValue();
				if (!newEventTypeName.isEmpty() && EapEventType.findByTypeName(newEventTypeName) == null) {
					// create EventType
					final EapEventType newEventType = new EapEventType(newEventTypeName);
					newEventType.addValueTypes(ExistingEventTypeEditor.this.selectedEventTypeAttributes);
					Broker.getEventAdministrator().importEventType(newEventType);

					// create EventTypeRule
					final Set<EapEventType> usedEventTypes = new HashSet<EapEventType>();
					for (final EapEventType selectedEventType : ExistingEventTypeEditor.this.selectedEventTypes) {
						usedEventTypes.add(selectedEventType);
					}
					final EventCondition condition = ExistingEventTypeEditor.this.conditionInput.getCondition();
					final EventTypeRule newEventTypeRule = new EventTypeRule(
							new ArrayList<EapEventType>(usedEventTypes), condition, newEventType);
					newEventTypeRule.save();

					// execute on existing data
					final ArrayList<EapEvent> newEvents = newEventTypeRule.execute();
					Broker.getEventImporter().importEvents(newEvents);

					target.add(ExistingEventTypeEditor.this.eventTypesCheckBoxMultipleChoice);
					final PageParameters pageParameters = new PageParameters();
					pageParameters.add("successFeedback", newEvents.size() + " events have been added to "
							+ newEventTypeName);
					this.setResponsePage(MainPage.class, pageParameters);
				} else if (EapEventType.getAllTypeNames().contains(newEventTypeName)) {
					target.appendJavaScript("$.unblockUI();");
					ExistingEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error("Event type already exists.");
					target.add(ExistingEventTypeEditor.this.abstractEapPage.getFeedbackPanel());
				} else {
					target.appendJavaScript("$.unblockUI();");
					ExistingEventTypeEditor.this.abstractEapPage.getFeedbackPanel().error(
							"You did not provide correct information.");
					target.add(ExistingEventTypeEditor.this.abstractEapPage.getFeedbackPanel());
				}
			}
		};

		layoutForm.add(createButton);
	}

}
