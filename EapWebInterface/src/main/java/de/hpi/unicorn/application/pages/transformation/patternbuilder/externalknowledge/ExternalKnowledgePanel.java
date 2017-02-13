/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.externalknowledge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.table.SelectEntryPanel;
import de.hpi.unicorn.application.pages.input.model.EventAttributeProvider;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.AttributeSelectionPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.CriteriaValuePanel;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpression;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

public class ExternalKnowledgePanel extends Panel {

	private static final long serialVersionUID = -1960020152680528731L;
	private final WarnOnExitForm layoutForm;
	private TypeTreeNode attributeToFill;
	private TransformationPatternTree patternTree;
	private ListView<ExternalKnowledgeExpression> criteriaAttributesAndValuesListView;
	private WebMarkupContainer container;
	private String attributeIdentifier;
	private Map<String, String> attributeIdentifiersAndExpressions;
	private Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge;
	private List<ExternalKnowledgeExpression> coalesceExpressions;
	private TextField<String> defaultValueInput;
	private ExternalKnowledgeExpressionSet externalKnowledgeExpressionSet;
	private AttributeSelectionPanel parentPanel;

	public ExternalKnowledgePanel(final String id) {
		super(id);
		this.patternTree = new TransformationPatternTree();
		this.attributeToFill = new TypeTreeNode("default", AttributeTypeEnum.STRING);
		this.attributeIdentifier = this.attributeToFill.getAttributeExpression();

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.coalesceExpressions = new ArrayList<ExternalKnowledgeExpression>();
		this.coalesceExpressions.add(new ExternalKnowledgeExpression());

		this.buildHeader();
		this.buildCriteriaAttributesAndValuesListView();
		this.buildFooter();
	}

	private void buildHeader() {

		final Label attributeToFillLabel = new Label("attributeToFillLabel", "Attribute: "
				+ this.attributeToFill.getAttributeExpression() + " (" + this.attributeToFill.getType() + ")");
		this.layoutForm.addOrReplace(attributeToFillLabel);

		final AjaxButton addCriteriaAttributesAndValuesButton = new AjaxButton("addCriteriaAttributesAndValuesButton",
				this.layoutForm) {
			private static final long serialVersionUID = 6456362459418575615L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				ExternalKnowledgePanel.this.coalesceExpressions.add(new ExternalKnowledgeExpression());
				ExternalKnowledgePanel.this.criteriaAttributesAndValuesListView
						.setList(ExternalKnowledgePanel.this.coalesceExpressions);
				target.add(ExternalKnowledgePanel.this.container);
			}
		};
		this.layoutForm.addOrReplace(addCriteriaAttributesAndValuesButton);
	}

	private void buildCriteriaAttributesAndValuesListView() {
		this.criteriaAttributesAndValuesListView = new ListView<ExternalKnowledgeExpression>(
				"criteriaAttributesAndValuesListView", this.coalesceExpressions) {

			private static final long serialVersionUID = -553434279906525757L;
			private DropDownChoice<EapEventType> eventTypeDropDownChoice;
			private DropDownChoice<TypeTreeNode> desiredAttributeDropDownChoice;
			private Component criteriaAttributesAndValuesTable;
			private ArrayList<IColumn<TypeTreeNode, String>> criteriaAttributeAndValueColumns;

			@Override
			protected void populateItem(final ListItem<ExternalKnowledgeExpression> item) {
				final ExternalKnowledgeExpression expression = item.getModelObject();
				this.buildComponents(item, expression);
			}

			private void buildComponents(final ListItem<ExternalKnowledgeExpression> item,
					final ExternalKnowledgeExpression expression) {
				final List<EapEventType> eventTypes = EapEventType.findAll();
				final EventAttributeProvider eventAttributeProvider;
				final Map<String, String> criteriaAttributesAndValues;
				if (expression.getCriteriaAttributesAndValues() == null
						|| expression.getCriteriaAttributesAndValues().isEmpty()) {
					criteriaAttributesAndValues = new HashMap<String, String>();
				} else {
					criteriaAttributesAndValues = expression.getCriteriaAttributesAndValues();
				}

				this.eventTypeDropDownChoice = new DropDownChoice<EapEventType>("eventTypeDropDownChoice",
						new Model<EapEventType>(), eventTypes);
				this.eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
					private static final long serialVersionUID = 1L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {

						if (eventTypeDropDownChoice.getModelObject() != null) {
							final List<TypeTreeNode> relevantAttributes = new ArrayList<TypeTreeNode>();
							for (final TypeTreeNode attribute : eventTypeDropDownChoice.getModelObject()
									.getValueTypes()) {
								if (ExternalKnowledgePanel.this.attributeToFill.getType() == attribute.getType()) {
									relevantAttributes.add(attribute);
								}
							}
							desiredAttributeDropDownChoice.setChoices(relevantAttributes);
						} else {
							desiredAttributeDropDownChoice.setChoices(new ArrayList<TypeTreeNode>());
						}
						target.add(desiredAttributeDropDownChoice);

						final EventAttributeProvider eventAttributeProvider = new EventAttributeProvider(
								new ArrayList<TypeTreeNode>());
						renderOrUpdateTable(item, eventAttributeProvider);
						target.add(criteriaAttributesAndValuesTable);
					}
				});
				this.eventTypeDropDownChoice.setModelObject(expression.getEventType());
				item.add(this.eventTypeDropDownChoice);

				this.desiredAttributeDropDownChoice = new DropDownChoice<TypeTreeNode>(
						"desiredAttributeDropDownChoice", new Model<TypeTreeNode>(), new ArrayList<TypeTreeNode>(),
						new ChoiceRenderer<TypeTreeNode>() {
							private static final long serialVersionUID = -1940950340293620814L;

							@Override
							public Object getDisplayValue(final TypeTreeNode element) {
								return element.getAttributeExpression();
							}
						});
				this.desiredAttributeDropDownChoice.setOutputMarkupId(true);
				this.desiredAttributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {

					private static final long serialVersionUID = -6864036894506127410L;

					@Override
					protected void onUpdate(final AjaxRequestTarget target) {
						final List<TypeTreeNode> attributes = eventTypeDropDownChoice.getModelObject().getValueTypes();
						attributes.remove(desiredAttributeDropDownChoice.getModelObject());
						final EventAttributeProvider eventAttributeProvider = new EventAttributeProvider(attributes);
						renderOrUpdateTable(item, eventAttributeProvider);
						target.add(criteriaAttributesAndValuesTable);
						for (final TypeTreeNode expression : attributes) {
							criteriaAttributesAndValues.put(expression.getAttributeExpression(), null);
						}
					}
				});
				if (this.eventTypeDropDownChoice.getModelObject() != null) {
					this.desiredAttributeDropDownChoice.setChoices(this.eventTypeDropDownChoice.getModelObject()
							.getValueTypes());
					this.desiredAttributeDropDownChoice.setModelObject(expression.getDesiredAttribute());
				}
				item.add(this.desiredAttributeDropDownChoice);

				if (this.eventTypeDropDownChoice.getModelObject() == null) {
					eventAttributeProvider = new EventAttributeProvider(new ArrayList<TypeTreeNode>());
				} else {
					final List<TypeTreeNode> attributes = this.eventTypeDropDownChoice.getModelObject().getValueTypes();
					attributes.remove(this.desiredAttributeDropDownChoice.getModelObject());
					final ArrayList<TypeTreeNode> selectedAttributes = new ArrayList<TypeTreeNode>();
					for (final TypeTreeNode attribute : attributes) {
						if (expression.getCriteriaAttributesAndValues().get(attribute.getAttributeExpression()) != null) {
							selectedAttributes.add(attribute);
						}
					}
					eventAttributeProvider = new EventAttributeProvider(attributes, selectedAttributes);
				}

				this.criteriaAttributeAndValueColumns = new ArrayList<IColumn<TypeTreeNode, String>>();
				this.criteriaAttributeAndValueColumns.add(new AbstractColumn<TypeTreeNode, String>(
						new Model<String>("")) {
					private static final long serialVersionUID = -9120188492434788547L;

					@Override
					public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
						final int entryId = ((TypeTreeNode) rowModel.getObject()).getID();
						cellItem.add(new SelectEntryPanel(componentId, entryId, eventAttributeProvider));
					}
				});
				this.criteriaAttributeAndValueColumns.add(new PropertyColumn<TypeTreeNode, String>(Model
						.of("Attribute"), "attributeExpression"));
				this.criteriaAttributeAndValueColumns.add(new AbstractColumn<TypeTreeNode, String>(new Model<String>(
						"Value")) {
					private static final long serialVersionUID = -5994858051827872697L;

					@Override
					public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
						final String attributeExpression = ((TypeTreeNode) rowModel.getObject())
								.getAttributeExpression();
						cellItem.add(new CriteriaValuePanel(componentId, attributeExpression,
								criteriaAttributesAndValues, ExternalKnowledgePanel.this.patternTree));
					}
				});

				this.renderOrUpdateTable(item, eventAttributeProvider);

				final AjaxButton saveCoalesceExpressionButton = new AjaxButton("saveCoalesceExpressionButton",
						ExternalKnowledgePanel.this.layoutForm) {
					private static final long serialVersionUID = 6456362459418575615L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						final Map<String, String> newCriteriaAttributesAndValues = new HashMap<String, String>();
						for (final String criteriaAttribute : criteriaAttributesAndValues.keySet()) {
							if (eventAttributeProvider.getSelectedAttributeExpressions().contains(criteriaAttribute)) {
								newCriteriaAttributesAndValues.put(criteriaAttribute,
										criteriaAttributesAndValues.get(criteriaAttribute));
							}
						}
						ExternalKnowledgePanel.this.coalesceExpressions.remove(item.getModelObject());
						ExternalKnowledgePanel.this.coalesceExpressions.add(new ExternalKnowledgeExpression(
								eventTypeDropDownChoice.getModelObject(), desiredAttributeDropDownChoice
										.getModelObject(), newCriteriaAttributesAndValues));
						ExternalKnowledgePanel.this.criteriaAttributesAndValuesListView
								.setList(ExternalKnowledgePanel.this.coalesceExpressions);
						target.add(ExternalKnowledgePanel.this.container);
					}
				};
				item.add(saveCoalesceExpressionButton);

				final AjaxButton removeCoalesceExpressionButton = new AjaxButton("removeCoalesceExpressionButton",
						ExternalKnowledgePanel.this.layoutForm) {
					private static final long serialVersionUID = 6456362459418575615L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
						ExternalKnowledgePanel.this.coalesceExpressions.remove(item.getModelObject());
						ExternalKnowledgePanel.this.criteriaAttributesAndValuesListView
								.setList(ExternalKnowledgePanel.this.coalesceExpressions);
						target.add(ExternalKnowledgePanel.this.container);
					}
				};
				item.add(removeCoalesceExpressionButton);
			}

			private void renderOrUpdateTable(final ListItem<ExternalKnowledgeExpression> item,
					final EventAttributeProvider eventAttributeProvider) {
				this.criteriaAttributesAndValuesTable = new DefaultDataTable<TypeTreeNode, String>(
						"criteriaAttributesAndValuesTable", this.criteriaAttributeAndValueColumns,
						eventAttributeProvider, 20);
				this.criteriaAttributesAndValuesTable.setOutputMarkupId(true);

				item.addOrReplace(this.criteriaAttributesAndValuesTable);
			}
		};
		this.criteriaAttributesAndValuesListView.setOutputMarkupId(true);

		this.container = new WebMarkupContainer("criteriaAttributesAndValuesContainer");
		this.container.addOrReplace(this.criteriaAttributesAndValuesListView);
		this.container.setOutputMarkupId(true);

		this.layoutForm.addOrReplace(this.container);
	}

	private void buildFooter() {
		this.defaultValueInput = new TextField<String>("defaultValueInput", new Model<String>());
		this.defaultValueInput.setOutputMarkupId(true);
		if (this.externalKnowledgeExpressionSet != null) {
			this.defaultValueInput.setModelObject(this.externalKnowledgeExpressionSet.getDefaultValue());
		} else {
			this.defaultValueInput.setModelObject("");
		}
		this.layoutForm.addOrReplace(this.defaultValueInput);

		final Label feedbackLabel = new Label("feedbackLabel", new Model<String>());
		feedbackLabel.setEscapeModelStrings(false);
		feedbackLabel.setOutputMarkupId(true);
		this.layoutForm.addOrReplace(feedbackLabel);

		final AjaxButton removeExpressionButton = new AjaxButton("removeExpressionButton", this.layoutForm) {
			private static final long serialVersionUID = 6456362459418575615L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				ExternalKnowledgePanel.this.attributeIdentifiersWithExternalKnowledge
						.remove(ExternalKnowledgePanel.this.attributeIdentifier);
				ExternalKnowledgePanel.this.parentPanel.enableAllComponents(target);
			}
		};
		this.layoutForm.addOrReplace(removeExpressionButton);

		final AjaxButton saveExpressionButton = new AjaxButton("saveExpressionButton", this.layoutForm) {
			private static final long serialVersionUID = 6456362459418575615L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				if (ExternalKnowledgePanel.this.coalesceExpressions.isEmpty()) {
					feedbackLabel
							.setDefaultModelObject("<font color=\"red\">Please provide external knowledge.</font>");
					target.add(feedbackLabel);
					return;
				} else {
					for (final ExternalKnowledgeExpression expression : ExternalKnowledgePanel.this.coalesceExpressions) {
						if (expression.getEventType() == null) {
							feedbackLabel
									.setDefaultModelObject("<font color=\"red\">Please choose an event type.</font>");
							target.add(feedbackLabel);
							return;
						} else if (expression.getDesiredAttribute() == null) {
							feedbackLabel
									.setDefaultModelObject("<font color=\"red\">Please choose a desired attribute.</font>");
							target.add(feedbackLabel);
							return;
						} else if (expression.getCriteriaAttributesAndValues().isEmpty()) {
							feedbackLabel
									.setDefaultModelObject("<font color=\"red\">Please provide at least one attribute with a value.</font>");
							target.add(feedbackLabel);
							return;
						}
					}
				}
				if (ExternalKnowledgePanel.this.externalKnowledgeExpressionSet == null) {
					ExternalKnowledgePanel.this.externalKnowledgeExpressionSet = new ExternalKnowledgeExpressionSet(
							ExternalKnowledgePanel.this.attributeToFill.getType(),
							ExternalKnowledgePanel.this.attributeIdentifier);
				}
				ExternalKnowledgePanel.this.externalKnowledgeExpressionSet
						.setDefaultValue(ExternalKnowledgePanel.this.defaultValueInput.getModelObject());
				ExternalKnowledgePanel.this.externalKnowledgeExpressionSet
						.setResultingType(ExternalKnowledgePanel.this.attributeToFill.getType());
				ExternalKnowledgePanel.this.externalKnowledgeExpressionSet
						.setExternalKnowledgeExpressions(ExternalKnowledgePanel.this.coalesceExpressions);
				ExternalKnowledgePanel.this.attributeIdentifiersWithExternalKnowledge.put(
						ExternalKnowledgePanel.this.attributeIdentifier,
						ExternalKnowledgePanel.this.externalKnowledgeExpressionSet);
				ExternalKnowledgePanel.this.attributeIdentifiersAndExpressions.put(
						ExternalKnowledgePanel.this.attributeIdentifier, null);
				ExternalKnowledgePanel.this.parentPanel.disableAllComponents(target);
				feedbackLabel
						.setDefaultModelObject("<font color=\"green\">Information about external knowledge saved.</font>");

				target.add(feedbackLabel);
			}
		};
		this.layoutForm.addOrReplace(saveExpressionButton);
	}

	public TypeTreeNode getAttributeToFill() {
		return this.attributeToFill;
	}

	public void setAttributeToFill(final TypeTreeNode attributeToFill) {
		this.attributeToFill = attributeToFill;
		this.attributeIdentifier = attributeToFill.getAttributeExpression();
	}

	public Map<String, String> getAttributeIdentifiersAndExpressions() {
		return this.attributeIdentifiersAndExpressions;
	}

	public void setAttributeIdentifiersAndExpressions(final Map<String, String> attributeIdentifiersAndExpressions) {
		this.attributeIdentifiersAndExpressions = attributeIdentifiersAndExpressions;
	}

	public Map<String, ExternalKnowledgeExpressionSet> getAttributeIdentifiersWithExternalKnowledge() {
		return this.attributeIdentifiersWithExternalKnowledge;
	}

	public void setAttributeIdentifiersWithExternalKnowledge(
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge) {
		this.attributeIdentifiersWithExternalKnowledge = attributeIdentifiersWithExternalKnowledge;
		this.externalKnowledgeExpressionSet = attributeIdentifiersWithExternalKnowledge.get(this.attributeIdentifier);
		if (this.externalKnowledgeExpressionSet == null) {
			this.coalesceExpressions = new ArrayList<ExternalKnowledgeExpression>();
			this.coalesceExpressions.add(new ExternalKnowledgeExpression());
		} else {
			this.coalesceExpressions = this.externalKnowledgeExpressionSet.getExternalKnowledgeExpressions();
		}
	}

	public TransformationPatternTree getPatternTree() {
		return this.patternTree;
	}

	public void setPatternTree(final TransformationPatternTree patternTree) {
		this.patternTree = patternTree;
	}

	public AttributeSelectionPanel getParentPanel() {
		return this.parentPanel;
	}

	public void setParentPanel(final AttributeSelectionPanel parentPanel) {
		this.parentPanel = parentPanel;
	}

	public void update(final AjaxRequestTarget target) {
		this.buildHeader();
		this.buildCriteriaAttributesAndValuesListView();
		this.buildFooter();
		target.add(this.container);
		target.add(this.defaultValueInput);
	}

}
