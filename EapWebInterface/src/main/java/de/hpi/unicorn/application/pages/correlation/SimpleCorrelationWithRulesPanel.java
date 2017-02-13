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
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.correlation.CorrelationRule;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * Panel representing the content panel for the first tab.
 */
public class SimpleCorrelationWithRulesPanel extends Panel {

	private static final long serialVersionUID = -4523105587173220532L;
	private List<CorrelationRule> correlationRules = new ArrayList<CorrelationRule>();
	private ListView<CorrelationRule> correlationRuleListView;
	private WebMarkupContainer correlationRuleMarkupContainer;
	private CorrelationPage correlationPage;
	private AjaxButton addCorrelationRuleButton;
	private final Set<EapEventType> correlationEventTypes = new HashSet<EapEventType>();

	public SimpleCorrelationWithRulesPanel(final String id, final CorrelationPage correlationPage) {
		super(id);
		this.correlationPage = correlationPage;
		final List<EapEventType> eventTypes = EapEventType.findAll();

		final Form<Void> layoutForm = new WarnOnExitForm("simpleCorrelationWithRulesForm");
		this.add(layoutForm);

		this.correlationRules.add(new CorrelationRule());

		this.addCorrelationRuleButton = new AjaxButton("addCorrelationRuleButton", layoutForm) {
			private static final long serialVersionUID = -118988274959205111L;

			@Override
			public boolean isEnabled() {
				return correlationPage.isSimpleCorrelationWithRules();
			}

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				SimpleCorrelationWithRulesPanel.this.correlationRules.add(new CorrelationRule());
				target.add(SimpleCorrelationWithRulesPanel.this.correlationRuleMarkupContainer);
			}
		};
		this.addCorrelationRuleButton.setOutputMarkupId(true);
		layoutForm.add(this.addCorrelationRuleButton);

		this.correlationRuleListView = new ListView<CorrelationRule>("correlationRuleListView", this.correlationRules) {
			private static final long serialVersionUID = 4168798264053898499L;

			@Override
			public boolean isEnabled() {
				return correlationPage.isSimpleCorrelationWithRules();
			}

			@Override
			protected void populateItem(final ListItem<CorrelationRule> item) {

				final CorrelationRule correlationRule = item.getModelObject();

				final DropDownChoice<TypeTreeNode> firstAttributeDropDownChoice = new DropDownChoice<TypeTreeNode>(
						"firstAttributeDropDownChoice", new Model<TypeTreeNode>(), new ArrayList<TypeTreeNode>());
				firstAttributeDropDownChoice.setOutputMarkupId(true);
				firstAttributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						correlationRule.setFirstAttribute(firstAttributeDropDownChoice.getModelObject());
						target.add(SimpleCorrelationWithRulesPanel.this.correlationRuleMarkupContainer);
					}
				});
				if (correlationRule.getEventTypeOfFirstAttribute() != null) {
					firstAttributeDropDownChoice.setChoices(correlationRule.getEventTypeOfFirstAttribute()
							.getValueTypes());
				}
				firstAttributeDropDownChoice.setModelObject(correlationRule.getFirstAttribute());
				item.add(firstAttributeDropDownChoice);

				final DropDownChoice<EapEventType> eventTypeOfFirstAttributeDropDownChoice = new DropDownChoice<EapEventType>(
						"eventTypeOfFirstAttributeDropDownChoice", new Model<EapEventType>(), eventTypes);
				eventTypeOfFirstAttributeDropDownChoice.setOutputMarkupId(true);
				eventTypeOfFirstAttributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						final EapEventType selectedEventType = eventTypeOfFirstAttributeDropDownChoice.getModelObject();
						correlationRule.setEventTypeOfFirstAttribute(selectedEventType);
						target.add(SimpleCorrelationWithRulesPanel.this.correlationRuleMarkupContainer);
						SimpleCorrelationWithRulesPanel.this.updateAdvancedCorrelationPanel(target);
					}
				});
				eventTypeOfFirstAttributeDropDownChoice.setModelObject(correlationRule.getEventTypeOfFirstAttribute());
				correlationRule.setEventTypeOfFirstAttribute(eventTypeOfFirstAttributeDropDownChoice.getModelObject());

				item.add(eventTypeOfFirstAttributeDropDownChoice);

				final DropDownChoice<TypeTreeNode> secondAttributeDropDownChoice = new DropDownChoice<TypeTreeNode>(
						"secondAttributeDropDownChoice", new Model<TypeTreeNode>(), new ArrayList<TypeTreeNode>()) {
					private static final long serialVersionUID = 7107102900826509015L;

					@Override
					public boolean isEnabled() {
						return firstAttributeDropDownChoice.getModelObject() != null;
					}
				};
				secondAttributeDropDownChoice.setOutputMarkupId(true);
				secondAttributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						correlationRule.setSecondAttribute(secondAttributeDropDownChoice.getModelObject());
					}
				});
				if (correlationRule.getEventTypeOfSecondAttribute() != null) {
					final List<TypeTreeNode> possibleSecondAttributes = new ArrayList<TypeTreeNode>();
					for (final TypeTreeNode attribute : correlationRule.getEventTypeOfSecondAttribute().getValueTypes()) {
						if (attribute.getType() == firstAttributeDropDownChoice.getModelObject().getType()) {
							possibleSecondAttributes.add(attribute);
						}
					}
					secondAttributeDropDownChoice.setChoices(possibleSecondAttributes);
				}
				secondAttributeDropDownChoice.setModelObject(correlationRule.getSecondAttribute());
				item.add(secondAttributeDropDownChoice);

				final DropDownChoice<EapEventType> eventTypeOfSecondAttributeDropDownChoice = new DropDownChoice<EapEventType>(
						"eventTypeOfSecondAttributeDropDownChoice", new Model<EapEventType>(), eventTypes) {
					private static final long serialVersionUID = 3720572018390164569L;

					@Override
					public boolean isEnabled() {
						return firstAttributeDropDownChoice.getModelObject() != null;
					}
				};
				eventTypeOfSecondAttributeDropDownChoice.setOutputMarkupId(true);
				eventTypeOfSecondAttributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -4107411122913362658L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						final EapEventType selectedEventType = eventTypeOfSecondAttributeDropDownChoice
								.getModelObject();
						correlationRule.setEventTypeOfSecondAttribute(selectedEventType);
						target.add(SimpleCorrelationWithRulesPanel.this.correlationRuleMarkupContainer);
						SimpleCorrelationWithRulesPanel.this.updateAdvancedCorrelationPanel(target);
					}
				});
				// if (correlationRule.getSecondAttribute() != null) {
				// eventTypeOfSecondAttributeDropDownChoice.setModelObject(correlationRule.getSecondAttribute().getEventType());
				// }
				eventTypeOfSecondAttributeDropDownChoice
						.setModelObject(correlationRule.getEventTypeOfSecondAttribute());
				correlationRule
						.setEventTypeOfSecondAttribute(eventTypeOfSecondAttributeDropDownChoice.getModelObject());
				item.add(eventTypeOfSecondAttributeDropDownChoice);

				final AjaxButton removeCorrelationRuleButton = new AjaxButton("removeCorrelationRuleButton", layoutForm) {
					private static final long serialVersionUID = -4244320500409194238L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						SimpleCorrelationWithRulesPanel.this.correlationRules.remove(item.getModelObject());
						target.add(SimpleCorrelationWithRulesPanel.this.correlationRuleMarkupContainer);
						SimpleCorrelationWithRulesPanel.this.updateAdvancedCorrelationPanel(target);
					}
				};
				item.add(removeCorrelationRuleButton);
			}
		};
		this.correlationRuleListView.setEnabled(!correlationPage.isSimpleCorrelationWithRules());

		this.correlationRuleMarkupContainer = new WebMarkupContainer("correlationRuleMarkupContainer");
		this.correlationRuleMarkupContainer.add(this.correlationRuleListView);
		this.correlationRuleMarkupContainer.setOutputMarkupId(true);
		layoutForm.add(this.correlationRuleMarkupContainer);
	}

	public void clearCorrelationAttributesListView(final AjaxRequestTarget target) {
		this.correlationRules.clear();
		target.add(this.correlationRuleMarkupContainer);
	}

	public List<CorrelationRule> getCorrelationRules() {
		return this.correlationRules;
	}

	public void setCorrelationRules(final ArrayList<CorrelationRule> correlationRules) {
		this.correlationRules = correlationRules;
	}

	public AjaxButton getAddCorrelationRuleButton() {
		return this.addCorrelationRuleButton;
	}

	public void setAddCorrelationRuleButton(final AjaxButton addCorrelationRuleButton) {
		this.addCorrelationRuleButton = addCorrelationRuleButton;
	}

	public WebMarkupContainer getCorrelationRuleMarkupContainer() {
		return this.correlationRuleMarkupContainer;
	}

	public void setCorrelationRuleMarkupContainer(final WebMarkupContainer correlationRuleMarkupContainer) {
		this.correlationRuleMarkupContainer = correlationRuleMarkupContainer;
	}

	private void updateAdvancedCorrelationPanel(final AjaxRequestTarget target) {
		this.correlationEventTypes.clear();
		for (final CorrelationRule correlationRule : this.correlationRules) {
			if (correlationRule.getEventTypeOfFirstAttribute() != null) {
				this.correlationEventTypes.add(correlationRule.getEventTypeOfFirstAttribute());
			}
			if (correlationRule.getEventTypeOfSecondAttribute() != null) {
				this.correlationEventTypes.add(correlationRule.getEventTypeOfSecondAttribute());
			}
		}
		this.correlationPage.setValuesOfAdvancedCorrelationPanelComponents(new ArrayList<EapEventType>(
				this.correlationEventTypes));
		this.correlationPage.updateAdvancedCorrelationPanelComponents(target);
	}

	public Set<EapEventType> getCorrelationEventTypes() {
		return this.correlationEventTypes;
	}

};
