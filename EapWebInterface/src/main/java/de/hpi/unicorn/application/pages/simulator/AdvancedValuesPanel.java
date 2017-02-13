/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.simulation.ValueRule;
import de.hpi.unicorn.simulation.ValueRuleType;
import de.hpi.unicorn.utils.Tuple;

public class AdvancedValuesPanel extends Panel {

	private final SimulationPanel simulationPanel;
	// private DropDownChoice<TypeTreeNode> attributeSelect;
	// private DropDownChoice<String> valueSelect;
	private final List<TypeTreeNode> attributeList;
	private final WebMarkupContainer valueRuleMarkupContainer;
	private final List<ValueRule> valueRules;
	private final ListView<ValueRule> valueRuleListView;
	private final List<ValueRuleType> valueOptionList = new ArrayList<ValueRuleType>(Arrays.asList(ValueRuleType.EQUAL,
			ValueRuleType.UNEQUAL));

	public AdvancedValuesPanel(final String id, final SimulationPanel simulationPanel) {
		super(id);
		this.simulationPanel = simulationPanel;
		this.attributeList = new ArrayList<TypeTreeNode>();
		this.setOutputMarkupId(true);
		final Form<Void> form = new Form<Void>("form");
		this.valueRules = new ArrayList<ValueRule>();

		// attributeSelect = new DropDownChoice<TypeTreeNode>("attributeSelect",
		// new Model<TypeTreeNode>(), attributeList);
		// attributeSelect.setOutputMarkupId(true);
		// form.add(attributeSelect);

		// valueSelect = new DropDownChoice<String>("valueSelect", new
		// Model<String>(), valueOptionList);
		// form.add(valueSelect);

		final AjaxButton addButton = new AjaxButton("addButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				AdvancedValuesPanel.this.valueRules.add(new ValueRule());
				target.add(AdvancedValuesPanel.this.valueRuleMarkupContainer);
			}
		};
		form.add(addButton);

		this.valueRuleListView = new ListView<ValueRule>("valueRuleListView", this.valueRules) {

			@Override
			protected void populateItem(final ListItem<ValueRule> item) {

				final ValueRule valueRule = item.getModelObject();

				final DropDownChoice<TypeTreeNode> attributeSelect = new DropDownChoice<TypeTreeNode>(
						"attributeSelect", new Model<TypeTreeNode>(), AdvancedValuesPanel.this.attributeList);
				attributeSelect.setOutputMarkupId(true);
				item.add(attributeSelect);
				attributeSelect.setModelObject(valueRule.getAttribute());

				attributeSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						valueRule.setAttribute(attributeSelect.getModelObject());
						target.add(AdvancedValuesPanel.this.valueRuleMarkupContainer);
					}
				});

				final DropDownChoice<ValueRuleType> valueSelect = new DropDownChoice<ValueRuleType>("valueSelect",
						new Model<ValueRuleType>(), AdvancedValuesPanel.this.valueOptionList);
				valueSelect.setOutputMarkupId(true);
				item.add(valueSelect);

				valueSelect.setModelObject(valueRule.getRuleType());

				valueSelect.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						valueRule.setRuleType(valueSelect.getModelObject());
						target.add(AdvancedValuesPanel.this.valueRuleMarkupContainer);
					}
				});
			}

		};

		this.valueRuleMarkupContainer = new WebMarkupContainer("valueRuleMarkupContainer");
		this.valueRuleMarkupContainer.add(this.valueRuleListView);
		this.valueRuleMarkupContainer.setOutputMarkupId(true);
		form.addOrReplace(this.valueRuleMarkupContainer);
		this.add(form);
	}

	public List<Tuple<TypeTreeNode, String>> getAdvancedValueRules() {
		final List<Tuple<TypeTreeNode, String>> advancedValueRules = new ArrayList<Tuple<TypeTreeNode, String>>();

		return advancedValueRules;
	}

	public void refreshAttributeChoice() {
		this.attributeList.clear();
		this.attributeList.addAll(this.simulationPanel.getAttributesFromTable());
	}

	public List<ValueRule> getValueRules() {
		return this.valueRules;
	}
}
