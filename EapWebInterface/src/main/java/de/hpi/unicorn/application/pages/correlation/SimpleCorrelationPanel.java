/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.correlation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * Panel representing the content panel for the first tab.
 */
public class SimpleCorrelationPanel extends Panel {

	private static final long serialVersionUID = 573672364803879784L;
	private ListMultipleChoice<TypeTreeNode> correlationAttributesSelect;
	private ArrayList<TypeTreeNode> correlationAttributes = new ArrayList<TypeTreeNode>();
	private final ArrayList<TypeTreeNode> selectedCorrelationAttributes = new ArrayList<TypeTreeNode>();
	private CheckBoxMultipleChoice<EapEventType> eventTypesCheckBoxMultipleChoice;
	private final ArrayList<EapEventType> selectedEventTypes = new ArrayList<EapEventType>();
	private final ArrayList<TypeTreeNode> commonCorrelationAttributes = new ArrayList<TypeTreeNode>();
	private CorrelationPage correlationPage;

	public SimpleCorrelationPanel(final String id, final CorrelationPage correlationPage) {
		super(id);

		this.correlationPage = correlationPage;

		final Form<Void> form = new WarnOnExitForm("simpleCorrelationForm");
		this.add(form);

		this.addEventTypeCheckBoxMultipleChoice(form);

		this.correlationAttributesSelect = new ListMultipleChoice<TypeTreeNode>("correlationEventTypesSelect",
				new Model<ArrayList<TypeTreeNode>>(this.selectedCorrelationAttributes), this.correlationAttributes) {
			private static final long serialVersionUID = 1353243674818396947L;

			@Override
			public boolean isEnabled() {
				return !correlationPage.isSimpleCorrelationWithRules();
			}
		};
		this.correlationAttributesSelect.setOutputMarkupId(true);

		this.correlationAttributesSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = -6739995621796236402L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				// System.out.println(selectedCorrelationAttributes);
			}
		});

		form.add(this.correlationAttributesSelect);
	}

	private void addEventTypeCheckBoxMultipleChoice(final Form<Void> layoutForm) {

		final List<EapEventType> eventTypes = EapEventType.findAll();

		this.eventTypesCheckBoxMultipleChoice = new CheckBoxMultipleChoice<EapEventType>(
				"eventTypesCheckBoxMultipleChoice", new PropertyModel<ArrayList<EapEventType>>(this,
						"selectedEventTypes"), eventTypes) {
			private static final long serialVersionUID = 5379816935206577577L;

			@Override
			protected boolean isDisabled(final EapEventType eventType, final int index, final String selected) {
				if (!SimpleCorrelationPanel.this.correlationPage.isSimpleCorrelationWithRules()) {
					// true for event types without matching attributes
					if (SimpleCorrelationPanel.this.selectedEventTypes.isEmpty()) {
						return false;
					} else {
						for (final TypeTreeNode commonAttribute : SimpleCorrelationPanel.this.commonCorrelationAttributes) {
							/*
							 * eventType.getValueTypes().contains(commonAttribute
							 * ) is not sufficient because equality does not
							 * consider attribute type
							 */
							for (final TypeTreeNode attributeOfEventType : eventType.getValueTypes()) {
								if (attributeOfEventType.getName().equals(commonAttribute.getName())
										&& (attributeOfEventType.getType() == commonAttribute.getType())) {
									return false;
								}
							}
						}
						return true;
					}
				} else {
					return true;
				}
			}
		};
		this.eventTypesCheckBoxMultipleChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				SimpleCorrelationPanel.this.commonCorrelationAttributes.clear();
				SimpleCorrelationPanel.this.correlationPage.clearAdvancedCorrelationPanelComponents();
				SimpleCorrelationPanel.this.correlationPage.getSimpleCorrelationPanel()
						.getCorrelationAttributesSelect().setChoices(new ArrayList<TypeTreeNode>());
				if (!SimpleCorrelationPanel.this.selectedEventTypes.isEmpty()) {
					// simple correlation
					SimpleCorrelationPanel.this.commonCorrelationAttributes
							.addAll(SimpleCorrelationPanel.this.selectedEventTypes.get(0).getValueTypes());
					for (final EapEventType actualEventType : SimpleCorrelationPanel.this.selectedEventTypes) {
						SimpleCorrelationPanel.this.commonCorrelationAttributes.retainAll(actualEventType
								.getValueTypes());
					}
					SimpleCorrelationPanel.this.correlationAttributesSelect
							.setChoices(SimpleCorrelationPanel.this.commonCorrelationAttributes);
					// advanced correlation - time
					SimpleCorrelationPanel.this.correlationPage
							.setValuesOfAdvancedCorrelationPanelComponents(SimpleCorrelationPanel.this.selectedEventTypes);
				}
				SimpleCorrelationPanel.this.correlationPage.updateAdvancedCorrelationPanelComponents(target);
				target.add(SimpleCorrelationPanel.this.correlationAttributesSelect);
				target.add(SimpleCorrelationPanel.this.eventTypesCheckBoxMultipleChoice);
			}
		});
		this.eventTypesCheckBoxMultipleChoice.setOutputMarkupId(true);
		layoutForm.add(this.eventTypesCheckBoxMultipleChoice);
	}

	public ListMultipleChoice<TypeTreeNode> getCorrelationAttributesSelect() {
		return this.correlationAttributesSelect;
	}

	public void setCorrelationAttributesSelect(final ListMultipleChoice<TypeTreeNode> correlationAttributesSelect) {
		this.correlationAttributesSelect = correlationAttributesSelect;
	}

	public CheckBoxMultipleChoice<EapEventType> getEventTypesCheckBoxMultipleChoice() {
		return this.eventTypesCheckBoxMultipleChoice;
	}

	public void setEventTypesCheckBoxMultipleChoice(
			final CheckBoxMultipleChoice<EapEventType> eventTypesCheckBoxMultipleChoice) {
		this.eventTypesCheckBoxMultipleChoice = eventTypesCheckBoxMultipleChoice;
	}

	public ArrayList<TypeTreeNode> getSelectedCorrelationAttributes() {
		return this.selectedCorrelationAttributes;
	}

	public Set<EapEventType> getCorrelationEventTypes() {
		return new HashSet<EapEventType>(this.selectedEventTypes);
	}

	public void setCorrelationAttributes(final ArrayList<TypeTreeNode> correlationAttributes) {
		this.correlationAttributes = correlationAttributes;
	}

};
