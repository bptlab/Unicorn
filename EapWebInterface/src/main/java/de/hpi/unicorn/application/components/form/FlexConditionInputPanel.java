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
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.EventCondition;

/*
 * This Component can be used to let the user define a condition.
 * It contains an AttributeSelect and a ValueSelect. At the moment only "=" is possible, but this could be extended.
 * Correspond with this Component by setting the attribute "selectedEventTypes". This will update the attributes and values.
 */

public class FlexConditionInputPanel extends Panel {

	private DropDownChoice<String> conditionAttributeSelect;
	private TextField conditionValueInput;
	private String selectedConditionAttribute;
	private List<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
	private String selectedConditionValue;

	public FlexConditionInputPanel(final String id) {
		super(id);

		this.conditionAttributeSelect = new DropDownChoice<String>("conditionAttributeSelect",
				new PropertyModel<String>(this, "selectedConditionAttribute"), new ArrayList<String>());
		this.conditionAttributeSelect.setOutputMarkupId(true);
		this.conditionAttributeSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// collect all attributes
				final Set<Serializable> attributes = new HashSet<Serializable>();
				for (final EapEventType eventType : FlexConditionInputPanel.this.selectedEventTypes) {
					attributes.addAll(eventType
							.findAttributeValues(FlexConditionInputPanel.this.selectedConditionAttribute));
				}
			}
		});
		this.add(this.conditionAttributeSelect);

		this.conditionValueInput = new TextField("conditionValueInput", new PropertyModel<String>(this,
				"selectedConditionValue"));
		this.conditionValueInput.setOutputMarkupId(true);
		this.add(this.conditionValueInput);
	}

	public DropDownChoice<String> getConditionAttributeSelect() {
		return this.conditionAttributeSelect;
	}

	public void setConditionAttributeSelect(final DropDownChoice<String> conditionAttributeSelect) {
		this.conditionAttributeSelect = conditionAttributeSelect;
	}

	public TextField getConditionValueSelect() {
		return this.conditionValueInput;
	}

	public void setConditionValueSelect(final TextField conditionValueInput) {
		this.conditionValueInput = conditionValueInput;
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
		return new EventCondition(this.selectedConditionAttribute, this.selectedConditionValue);
	}

	public void updateAttributesValues() {
		final Set<String> attributeValues = new HashSet();
		for (final EapEventType eventType : this.selectedEventTypes) {
			attributeValues.addAll(eventType.getAttributeExpressionsWithoutTimestampName());
		}
		this.conditionAttributeSelect.setChoices(new ArrayList(attributeValues));
	}
}
