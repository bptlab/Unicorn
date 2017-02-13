/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.correlation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.hpi.unicorn.application.components.form.ConditionInputPanel;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.correlation.TimeCondition;
import de.hpi.unicorn.event.EapEventType;

/**
 * Panel representing the content panel for the first tab.
 */
public class AdvancedCorrelationPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<String> timeCorrelationRadioValues = new ArrayList<String>(Arrays.asList("after", "before"));
	private String selectedTimeRadioOption = this.timeCorrelationRadioValues.get(0);
	private final AjaxCheckBox timeCorrelationCheckBox;
	private boolean timeCorrelationSelected = false;
	private String timeCorrelationMinutes = new String();
	private EapEventType selectedEventType;
	private final TextField<String> timeCorrelationMinutesInput;
	private final DropDownChoice<EapEventType> timeCorrelationEventTypeSelect;
	private final RadioChoice<String> timeCorrelationAfterOrBeforeType;
	private ConditionInputPanel conditionPanel;
	private TextField<String> multipleConditionsTextField;
	private static final ResourceReference Label_CSS = new PackageResourceReference(AdvancedCorrelationPanel.class,
			"label.css");

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(AdvancedCorrelationPanel.Label_CSS));
	}

	public AdvancedCorrelationPanel(final String id) {
		super(id);

		final Form<Void> advancedCorrelationForm = new WarnOnExitForm("advancedCorrelationForm");
		this.add(advancedCorrelationForm);

		this.timeCorrelationCheckBox = new AjaxCheckBox("timeCheckBox", Model.of(Boolean.FALSE)) {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				AdvancedCorrelationPanel.this.timeCorrelationSelected = AdvancedCorrelationPanel.this.timeCorrelationSelected ? false
						: true;
			}
		};
		this.timeCorrelationCheckBox.setOutputMarkupId(true);
		advancedCorrelationForm.add(this.timeCorrelationCheckBox);

		this.timeCorrelationEventTypeSelect = new DropDownChoice<EapEventType>("timeEventTypeSelect",
				new PropertyModel<EapEventType>(this, "selectedEventType"), new ArrayList<EapEventType>());
		this.timeCorrelationEventTypeSelect.setOutputMarkupId(true);
		this.timeCorrelationEventTypeSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AdvancedCorrelationPanel.this.selectedEventType != null) {
					AdvancedCorrelationPanel.this.conditionPanel.setSelectedEventTypes(Arrays
							.asList((AdvancedCorrelationPanel.this.selectedEventType)));
					AdvancedCorrelationPanel.this.conditionPanel.updateAttributesValues();
				} else {
					AdvancedCorrelationPanel.this.conditionPanel.getConditionAttributeSelect().setChoices(
							new ArrayList<String>());
					AdvancedCorrelationPanel.this.conditionPanel.getConditionValueSelect().setChoices(
							new ArrayList<Serializable>());
				}
				target.add(AdvancedCorrelationPanel.this.conditionPanel.getConditionAttributeSelect());
				target.add(AdvancedCorrelationPanel.this.conditionPanel.getConditionValueSelect());
			}
		});
		advancedCorrelationForm.add(this.timeCorrelationEventTypeSelect);

		this.timeCorrelationMinutesInput = new TextField<String>("timeMinutesInput", Model.of(""));
		this.timeCorrelationMinutesInput.setOutputMarkupId(true);
		this.timeCorrelationMinutesInput.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				AdvancedCorrelationPanel.this.timeCorrelationMinutes = AdvancedCorrelationPanel.this.timeCorrelationMinutesInput
						.getValue();
			}
		});
		advancedCorrelationForm.add(this.timeCorrelationMinutesInput);

		this.timeCorrelationAfterOrBeforeType = new RadioChoice<String>("afterOrBeforeRadioGroup",
				new PropertyModel<String>(this, "selectedTimeRadioOption"), this.timeCorrelationRadioValues) {
			@Override
			public String getSuffix() {
				return "&nbsp;&nbsp;&nbsp;";
			}
		};
		advancedCorrelationForm.add(this.timeCorrelationAfterOrBeforeType);

		this.conditionPanel = new ConditionInputPanel("conditionInput", true);
		advancedCorrelationForm.add(this.conditionPanel);

		this.multipleConditionsTextField = new TextField<String>("multipleConditionsTextField", new Model<String>());
		this.multipleConditionsTextField.setOutputMarkupId(true);
		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -5737941362786901904L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AdvancedCorrelationPanel.this.isMultipleConditionsTextFieldFilled()) {
					AdvancedCorrelationPanel.this.conditionPanel.disableAllComponents(target);
				} else {
					AdvancedCorrelationPanel.this.conditionPanel.enableAllComponents(target);
				}
			}
		};
		this.multipleConditionsTextField.add(onChangeAjaxBehavior);
		advancedCorrelationForm.add(this.multipleConditionsTextField);
	}

	private boolean isMultipleConditionsTextFieldFilled() {
		return this.multipleConditionsTextField.getModelObject() != null
				&& !this.multipleConditionsTextField.getModelObject().isEmpty();
	}

	public String getSelectedTimeRadioOption() {
		return this.selectedTimeRadioOption;
	}

	public void setSelectedTimeRadioOption(final String selectedTimeRadioOption) {
		this.selectedTimeRadioOption = selectedTimeRadioOption;
	}

	public TextField<String> getMultipleConditionsTextField() {
		return this.multipleConditionsTextField;
	}

	public void setMultipleConditionsTextField(final TextField<String> multipleConditionsTextField) {
		this.multipleConditionsTextField = multipleConditionsTextField;
	}

	public CheckBox getTimeCorrelationCheckBox() {
		return this.timeCorrelationCheckBox;
	}

	public TextField<String> getTimeCorrelationMinutesInput() {
		return this.timeCorrelationMinutesInput;
	}

	public DropDownChoice<EapEventType> getTimeCorrelationEventTypeSelect() {
		return this.timeCorrelationEventTypeSelect;
	}

	public RadioChoice<String> getTimeCorrelationAfterOrBeforeType() {
		return this.timeCorrelationAfterOrBeforeType;
	}

	public TimeCondition getTimeCondition() {
		int minutes;
		String conditionString;
		if (!this.timeCorrelationMinutes.isEmpty()) {
			minutes = Integer.valueOf(this.timeCorrelationMinutes);
		} else {
			minutes = 0;
		}

		final boolean isAfter = this.selectedTimeRadioOption.equals("after") ? true : false;
		if (this.isMultipleConditionsTextFieldFilled()) {
			conditionString = this.multipleConditionsTextField.getModelObject();
		} else {
			conditionString = this.conditionPanel.getCondition().getConditionString();
		}
		// //System.out.println(conditionString);
		final TimeCondition timeCondition = new TimeCondition(this.selectedEventType, minutes, isAfter, conditionString);
		return timeCondition;
	}

	public boolean isTimeCorrelationSelected() {
		return this.timeCorrelationSelected;
	}

	public DropDownChoice<String> getTimeCorrelationConditionAttributeSelect() {
		return this.conditionPanel.getConditionAttributeSelect();
	}

	public DropDownChoice<Serializable> getTimeCorrelationConditionValueSelect() {
		return this.conditionPanel.getConditionValueSelect();
	}

};
