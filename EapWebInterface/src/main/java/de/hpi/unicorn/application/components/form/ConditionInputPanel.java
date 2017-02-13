/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.form;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.EventCondition;

/**
 * This Component can be used to let the user define a condition. It contains an
 * AttributeSelect and a ValueSelect. At the moment only "=" is possible, but
 * this could be extended. Correspond with this Component by setting the
 * attribute "selectedEventTypes". This will update the attributes and values.
 */

public class ConditionInputPanel extends Panel {

	private DropDownChoice<String> conditionAttributeSelect;
	private DropDownChoice<Serializable> conditionValueSelect;
	private String selectedConditionAttribute;
	private List<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
	private String selectedConditionValue;
	private final TextField<String> conditionValueTextField;
	private final Label conditionValueLabel;

	public ConditionInputPanel(final String id, final boolean isConditionValueTextFieldVisible) {
		super(id);

		final Form<Void> layoutForm = new Form<Void>("layoutForm");
		this.add(layoutForm);

		this.conditionAttributeSelect = new DropDownChoice<String>("conditionAttributeSelect",
				new PropertyModel<String>(this, "selectedConditionAttribute"), new ArrayList<String>());
		this.conditionAttributeSelect.setOutputMarkupId(true);
		this.conditionAttributeSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// collect all attributes
				final Set<Serializable> attributes = new HashSet<Serializable>();
				for (final EapEventType eventType : ConditionInputPanel.this.selectedEventTypes) {
					attributes.addAll(eventType
							.findAttributeValues(ConditionInputPanel.this.selectedConditionAttribute));
				}
				final ArrayList<Serializable> choices = new ArrayList<Serializable>();
				// choices.add(null);
				choices.addAll(attributes);
				ConditionInputPanel.this.conditionValueSelect.setChoices(choices);
				target.add(ConditionInputPanel.this.conditionValueSelect);
			}
		});
		layoutForm.add(this.conditionAttributeSelect);

		this.conditionValueSelect = new DropDownChoice<Serializable>("conditionValueSelect",
				new PropertyModel<Serializable>(this, "selectedConditionValue"), new ArrayList<Serializable>());
		this.conditionValueSelect.setOutputMarkupId(true);
		this.conditionValueSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				target.add(ConditionInputPanel.this.conditionValueSelect);
				if (ConditionInputPanel.this.selectedConditionValue != null
						&& !ConditionInputPanel.this.selectedConditionValue.isEmpty()) {
					ConditionInputPanel.this.conditionValueTextField.setEnabled(false);
				} else {
					ConditionInputPanel.this.conditionValueTextField.setEnabled(true);
				}
				target.add(ConditionInputPanel.this.conditionValueTextField);
			}
		});

		layoutForm.add(this.conditionValueSelect);

		this.conditionValueLabel = new Label("conditionValueLabel", "or");
		this.conditionValueLabel.setVisible(isConditionValueTextFieldVisible);
		layoutForm.add(this.conditionValueLabel);

		this.conditionValueTextField = new TextField<String>("conditionValueTextField", new Model<String>());
		this.conditionValueTextField.setOutputMarkupId(true);

		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -5737941362786901904L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (ConditionInputPanel.this.conditionValueTextField.getModelObject() != null
						&& !ConditionInputPanel.this.conditionValueTextField.getModelObject().isEmpty()) {
					ConditionInputPanel.this.conditionValueSelect.setEnabled(false);
				} else {
					ConditionInputPanel.this.conditionValueSelect.setEnabled(true);
				}
				target.add(ConditionInputPanel.this.conditionValueSelect);
			}
		};
		this.conditionValueTextField.add(onChangeAjaxBehavior);
		this.conditionValueTextField.setVisible(isConditionValueTextFieldVisible);
		layoutForm.add(this.conditionValueTextField);
	}

	public DropDownChoice<String> getConditionAttributeSelect() {
		return this.conditionAttributeSelect;
	}

	public void setConditionAttributeSelect(final DropDownChoice<String> conditionAttributeSelect) {
		this.conditionAttributeSelect = conditionAttributeSelect;
	}

	public DropDownChoice<Serializable> getConditionValueSelect() {
		return this.conditionValueSelect;
	}

	public void setConditionValueSelect(final DropDownChoice<Serializable> conditionValueSelect) {
		this.conditionValueSelect = conditionValueSelect;
	}

	public String getSelectedConditionAttribute() {
		return this.selectedConditionAttribute;
	}

	public void setSelectedConditionAttribute(final String selectedConditionAttribute) {
		this.selectedConditionAttribute = selectedConditionAttribute;
	}

	public List<EapEventType> getSelectedEventTypes() {
		return this.selectedEventTypes;
	}

	public void setSelectedEventTypes(final List<EapEventType> selectedEventTypes) {
		this.selectedEventTypes = selectedEventTypes;
		this.updateAttributesValues();
	}

	public void addSelectedEventTypes(final List<EapEventType> selectedEventTypes) {
		this.selectedEventTypes = selectedEventTypes;
		this.updateAttributesValues();
	}

	public void addSelectedEventType(final EapEventType selectedEventType) {
		this.selectedEventTypes.add(selectedEventType);
		this.updateAttributesValues();
	}

	public void clearSelectedEventType() {
		this.selectedEventTypes.clear();
	}

	public String getSelectedConditionValue() {
		return this.selectedConditionValue;
	}

	public void setSelectedConditionValue(final String selectedConditionValue) {
		this.selectedConditionValue = selectedConditionValue;
	}

	public EventCondition getCondition() {
		if (this.conditionValueSelect.isEnabled()) {
			return new EventCondition(this.selectedConditionAttribute, this.selectedConditionValue);
		} else if (this.conditionValueTextField.isEnabled()) {
			return new EventCondition(this.selectedConditionAttribute, this.conditionValueTextField.getModelObject());
		}
		return new EventCondition();
	}

	public void updateAttributesValues() {
		final Set<String> attributes = new HashSet();
		final Set<String> attributeValues = new HashSet();

		for (final EapEventType eventType : this.selectedEventTypes) {
			final List<String> newAttributes = eventType.getValueTypeTree().getAttributesAsExpression();
			// update attributes
			attributes.addAll(newAttributes);
			// update values
			for (final String attribute : newAttributes) {
				for (final Serializable value : eventType.findAttributeValues(attribute)) {
					attributeValues.add(value.toString());
				}
			}
		}
		this.conditionAttributeSelect.setChoices(new ArrayList(attributes));
		this.conditionValueSelect.setChoices(new ArrayList(attributeValues));
	}

	public void enableAllComponents(final AjaxRequestTarget target) {
		this.conditionAttributeSelect.setEnabled(true);
		this.conditionValueSelect.setEnabled(true);
		this.conditionValueTextField.setEnabled(true);
		this.updateAllComponents(target);
	}

	public void disableAllComponents(final AjaxRequestTarget target) {
		this.conditionAttributeSelect.setEnabled(false);
		this.conditionValueSelect.setEnabled(false);
		this.conditionValueTextField.setEnabled(false);
		this.updateAllComponents(target);
	}

	private void updateAllComponents(final AjaxRequestTarget target) {
		target.add(this.conditionAttributeSelect);
		target.add(this.conditionValueSelect);
		target.add(this.conditionValueTextField);
	}
}
