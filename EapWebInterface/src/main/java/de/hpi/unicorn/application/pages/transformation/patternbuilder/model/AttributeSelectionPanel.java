/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.externalknowledge.ExternalKnowledgeModal;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.PatternOperatorEnum;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

public class AttributeSelectionPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final Form<Void> layoutForm;
	private final TransformationPatternTree tree;
	private DropDownChoice<EventTypeElement> eventTypeElementDropDownChoice;
	private DropDownChoice<TypeTreeNode> attributeDropDownChoice;
	private AttributeExpressionTextField expressionInput;
	private String userDefinedExpression;
	private final Map<String, String> attributeIdentifiersAndExpressions;
	private final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge;
	private final TypeTreeNode attributeToFill;
	protected String expressionFromDropDownChoices;
	private String attributeIdentifier;
	private final PatternBuilderPanel patternBuilderPanel;
	private AjaxCheckBox currentDateUsedCheckbox;
	private final Boolean currentDateUsed;
	private Boolean allComponentsEnabled;
	private TextField<Integer> arrayElementIndexInput;
	private Label arrayElementIndexLabel;
	private Integer arrayElementIndex;
	private final AttributeSelectionPanel panel;
	private Label currentDateUsedLabel;

	public AttributeSelectionPanel(final String id, final TypeTreeNode attributeToFill,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge,
			final PatternBuilderPanel patternBuilderPanel) {
		super(id);

		this.panel = this;
		this.currentDateUsed = false;
		this.userDefinedExpression = new String();
		this.expressionFromDropDownChoices = new String();
		this.patternBuilderPanel = patternBuilderPanel;
		this.tree = patternBuilderPanel.getPatternTree();
		this.attributeIdentifiersAndExpressions = attributeIdentifiersAndExpressions;
		this.attributeIdentifiersWithExternalKnowledge = attributeIdentifiersWithExternalKnowledge;
		this.attributeToFill = attributeToFill;
		if (attributeToFill.isTimestamp()) {
			this.attributeIdentifier = "Timestamp";
		} else {
			this.attributeIdentifier = attributeToFill.getAttributeExpression();
		}
		if (attributeToFill.getType() == null
				|| attributeIdentifiersWithExternalKnowledge.get(this.attributeIdentifier) != null) {
			this.allComponentsEnabled = false;
		} else {
			this.allComponentsEnabled = true;
		}
		if (attributeIdentifiersAndExpressions.containsKey(this.attributeIdentifier)) {
			this.userDefinedExpression = attributeIdentifiersAndExpressions.get(this.attributeIdentifier);
		}
		this.layoutForm = new Form<Void>("layoutForm");
		this.buildUseCurrentDateCheckbox();
		this.buildEventTypeDropDownChoice();
		this.buildArrayElementIndexComponents();
		this.buildAttributeDropDownChoice();
		this.buildExpressionInput();
		this.buildUseExternalKnowledgeButton();

		this.add(this.layoutForm);
	}

	private void buildUseExternalKnowledgeButton() {
		final AjaxButton useExternalKnowledgeButton = new AjaxButton("useExternalKnowledgeButton", this.layoutForm) {
			private static final long serialVersionUID = -2611608162033482853L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				final ExternalKnowledgeModal modal = AttributeSelectionPanel.this.patternBuilderPanel
						.getAdvancedRuleEditorPanel().getTransformationPage().getExternalKnowledgeModal();
				modal.getPanel().setAttributeIdentifiersAndExpressions(
						AttributeSelectionPanel.this.attributeIdentifiersAndExpressions);
				modal.getPanel().setAttributeIdentifiersWithExternalKnowledge(
						AttributeSelectionPanel.this.attributeIdentifiersWithExternalKnowledge);
				modal.getPanel().setPatternTree(AttributeSelectionPanel.this.patternBuilderPanel.getPatternTree());
				modal.getPanel().setAttributeToFill(AttributeSelectionPanel.this.attributeToFill);
				modal.getPanel().setParentPanel(AttributeSelectionPanel.this.panel);
				modal.getPanel().detach();
				modal.getPanel().update(target);
				target.add(modal.getPanel());
				modal.show(target);
				// eventRepository.getEventViewModal().getPanel().setEvent(rowModel.getObject());
				// eventRepository.getEventViewModal().getPanel().detach();
				// target.add(eventRepository.getEventViewModal().getPanel());
				// eventRepository.getEventViewModal().show(target);
			}
		};
		this.layoutForm.add(useExternalKnowledgeButton);
	}

	private void buildUseCurrentDateCheckbox() {

		this.currentDateUsedLabel = new Label("currentDateUsedLabel", "Time of transformation") {
			private static final long serialVersionUID = 7258389748479790432L;

			@Override
			public boolean isVisible() {
				return AttributeSelectionPanel.this.attributeToFill.getType() == AttributeTypeEnum.DATE;
			}
		};
		this.layoutForm.add(this.currentDateUsedLabel);

		this.currentDateUsedCheckbox = new AjaxCheckBox("currentDateUsedCheckbox", new PropertyModel<Boolean>(this,
				"currentDateUsed")) {
			private static final long serialVersionUID = -8207035371422899809L;

			@Override
			public boolean isEnabled() {
				return AttributeSelectionPanel.this.allComponentsEnabled;
			}

			@Override
			public boolean isVisible() {
				return AttributeSelectionPanel.this.attributeToFill.getType() == AttributeTypeEnum.DATE;
			}

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AttributeSelectionPanel.this.currentDateUsed) {
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier, "currentDate()");
				} else {
					if (AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() != null) {
						AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
								AttributeSelectionPanel.this.attributeIdentifier,
								AttributeSelectionPanel.this.expressionFromDropDownChoices);
					} else {
						AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
								AttributeSelectionPanel.this.attributeIdentifier,
								AttributeSelectionPanel.this.userDefinedExpression);
					}
				}
				target.add(AttributeSelectionPanel.this.eventTypeElementDropDownChoice);
				target.add(AttributeSelectionPanel.this.attributeDropDownChoice);
				target.add(AttributeSelectionPanel.this.expressionInput);
			}
		};
		this.currentDateUsedCheckbox.setOutputMarkupId(true);
		this.layoutForm.add(this.currentDateUsedCheckbox);
	}

	private void buildEventTypeDropDownChoice() {
		final List<EventTypeElement> eventTypeElements = new ArrayList<EventTypeElement>();
		for (final EventTreeElement<Serializable> element : this.tree.getElements()) {
			if (element instanceof EventTypeElement) {
				final EventTypeElement eventTypeElement = (EventTypeElement) element;
				eventTypeElements.add(eventTypeElement);
			}
		}
		this.eventTypeElementDropDownChoice = new DropDownChoice<EventTypeElement>("eventTypeElementDropDownChoice",
				new Model<EventTypeElement>(), eventTypeElements, new ChoiceRenderer<EventTypeElement>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final EventTypeElement element) {
						final StringBuffer sb = new StringBuffer();
						if (element.getAlias() == null || element.getAlias().isEmpty()) {
							sb.append("No alias");
						} else {
							sb.append(element.getAlias());
						}
						sb.append(" (" + ((EapEventType) element.getValue()).getTypeName() + ")");
						return sb.toString();
					}
				}) {
			private static final long serialVersionUID = -6808132238575181809L;

			@Override
			public boolean isEnabled() {
				return AttributeSelectionPanel.this.allComponentsEnabled
						&& !AttributeSelectionPanel.this.currentDateUsed;
			}

			@Override
			public boolean isDisabled(final EventTypeElement element, final int index, final String selected) {
				if (element.getAlias() == null) {
					return true;
				}
				return element.getAlias().isEmpty();
			}
		};
		this.eventTypeElementDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() != null) {
					final EapEventType eventType = (EapEventType) AttributeSelectionPanel.this.eventTypeElementDropDownChoice
							.getModelObject().getValue();
					final ArrayList<TypeTreeNode> potentialAttributes = new ArrayList<TypeTreeNode>();
					if (AttributeSelectionPanel.this.attributeToFill.getType() == AttributeTypeEnum.DATE) {
						potentialAttributes.add(new TypeTreeNode("Timestamp", AttributeTypeEnum.DATE));
					} else if (AttributeSelectionPanel.this.attributeToFill.getType() == AttributeTypeEnum.FLOAT) {
						potentialAttributes.add(new TypeTreeNode("Timestamp", AttributeTypeEnum.FLOAT));
					}

					for (final TypeTreeNode currentAttribute : eventType.getValueTypes()) {
						if (AttributeSelectionPanel.this.attributeToFill.getType() == currentAttribute.getType()) {
							potentialAttributes.add(currentAttribute);
						}
					}
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier,
							AttributeSelectionPanel.this.expressionFromDropDownChoices);
					AttributeSelectionPanel.this.attributeDropDownChoice.setChoices(potentialAttributes);
					AttributeSelectionPanel.this.updateExpressionFromDropDownChoice();
				} else {
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier,
							AttributeSelectionPanel.this.userDefinedExpression);
					AttributeSelectionPanel.this.attributeDropDownChoice.setChoices(new ArrayList<TypeTreeNode>());
				}
				target.add(AttributeSelectionPanel.this.arrayElementIndexInput);
				target.add(AttributeSelectionPanel.this.arrayElementIndexLabel);
				AttributeSelectionPanel.this.updateAllComponents(target);
			}
		});
		this.eventTypeElementDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.eventTypeElementDropDownChoice);
	}

	/**
	 * for event types with pattern operator REPEAT as parent
	 */
	private void buildArrayElementIndexComponents() {
		this.arrayElementIndexInput = new TextField<Integer>("arrayElementIndexInput", new PropertyModel<Integer>(this,
				"arrayElementIndex")) {
			private static final long serialVersionUID = 7106359506546529349L;

			@Override
			public boolean isVisible() {
				return AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() != null
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject().hasParent()
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject().getParent()
								.getValue() == PatternOperatorEnum.REPEAT;
			}
		};
		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -5737941362786901904L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				AttributeSelectionPanel.this.updateExpressionFromDropDownChoice();
				AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
						AttributeSelectionPanel.this.attributeIdentifier,
						AttributeSelectionPanel.this.expressionFromDropDownChoices);
			}
		};
		this.arrayElementIndexInput.add(onChangeAjaxBehavior);
		// if (eventTypeElementDropDownChoice.getModelObject() != null &&
		// eventTypeElementDropDownChoice.getModelObject().hasParent() &&
		// eventTypeElementDropDownChoice.getModelObject().getParent().getValue()
		// == PatternOperatorEnum.REPEAT) {
		// int matchCount = ((PatternOperatorElement)
		// eventTypeElementDropDownChoice.getModelObject().getParent()).getRangeElement().getLeftEndpoint();
		// RangeValidator<Integer> rangeValidator = new
		// RangeValidator<Integer>(0, matchCount);
		// arrayElementIndexInput.add(rangeValidator);
		// }
		this.arrayElementIndexInput.setOutputMarkupPlaceholderTag(true);
		this.arrayElementIndexInput.setOutputMarkupId(true);
		this.layoutForm.add(this.arrayElementIndexInput);

		this.arrayElementIndexLabel = new Label("arrayElementIndexLabel", "Element #") {
			private static final long serialVersionUID = 7890155448902992129L;

			@Override
			public boolean isVisible() {
				return AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() != null
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject().hasParent()
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject().getParent()
								.getValue() == PatternOperatorEnum.REPEAT;
			}
		};
		this.arrayElementIndexLabel.setOutputMarkupPlaceholderTag(true);
		this.arrayElementIndexLabel.setOutputMarkupId(true);
		this.layoutForm.add(this.arrayElementIndexLabel);
	}

	private void buildAttributeDropDownChoice() {
		this.attributeDropDownChoice = new DropDownChoice<TypeTreeNode>("attributeDropDownChoice",
				new Model<TypeTreeNode>(), new ArrayList<TypeTreeNode>(), new ChoiceRenderer<TypeTreeNode>() {
					private static final long serialVersionUID = 1L;

					@Override
					public Object getDisplayValue(final TypeTreeNode attribute) {
						return attribute.getAttributeExpression();
					}
				}) {
			private static final long serialVersionUID = 474559809405809953L;

			@Override
			public boolean isEnabled() {
				return AttributeSelectionPanel.this.allComponentsEnabled
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() != null
						&& !AttributeSelectionPanel.this.currentDateUsed;
			}
		};
		this.attributeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AttributeSelectionPanel.this.attributeDropDownChoice.getModelObject() != null) {
					AttributeSelectionPanel.this.updateExpressionFromDropDownChoice();
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier,
							AttributeSelectionPanel.this.expressionFromDropDownChoices);
				}
			}
		});
		this.attributeDropDownChoice.setEnabled(false);
		this.attributeDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.attributeDropDownChoice);
	}

	private void buildExpressionInput() {
		this.expressionInput = new AttributeExpressionTextField("expressionInput", new PropertyModel<String>(this,
				"userDefinedExpression"), this.patternBuilderPanel.getPatternTree()) {
			private static final long serialVersionUID = -5212591175918436633L;

			@Override
			public boolean isEnabled() {
				return AttributeSelectionPanel.this.allComponentsEnabled
						&& AttributeSelectionPanel.this.eventTypeElementDropDownChoice.getModelObject() == null
						&& !AttributeSelectionPanel.this.currentDateUsed;
			}
		};
		this.expressionInput.setOutputMarkupId(true);

		final OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {
			private static final long serialVersionUID = -5737941362786901904L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (AttributeSelectionPanel.this.userDefinedExpression == null) {
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier, "");
				} else {
					AttributeSelectionPanel.this.attributeIdentifiersAndExpressions.put(
							AttributeSelectionPanel.this.attributeIdentifier,
							AttributeSelectionPanel.this.userDefinedExpression);
				}
			}
		};
		this.expressionInput.add(onChangeAjaxBehavior);
		this.layoutForm.add(this.expressionInput);
	}

	private void updateExpressionFromDropDownChoice() {
		if (this.attributeDropDownChoice.getModelObject() != null) {
			final StringBuffer sb = new StringBuffer();
			sb.append(this.eventTypeElementDropDownChoice.getModelObject().getAlias());
			if (this.arrayElementIndexInput.isVisible()) {
				sb.append("[" + String.valueOf(this.arrayElementIndex) + "]");
			}
			sb.append("." + this.attributeDropDownChoice.getModelObject().getAttributeExpression());
			this.expressionFromDropDownChoices = sb.toString();
		}
	}

	public void enableAllComponents(final AjaxRequestTarget target) {
		this.allComponentsEnabled = true;
		this.updateAllComponents(target);
		if (this.eventTypeElementDropDownChoice.getModelObject() != null) {
			this.attributeIdentifiersAndExpressions.put(this.attributeIdentifier, this.expressionFromDropDownChoices);
		} else {
			if (this.userDefinedExpression == null) {
				this.attributeIdentifiersAndExpressions.put(this.attributeIdentifier, "");
			} else {
				this.attributeIdentifiersAndExpressions.put(this.attributeIdentifier, this.userDefinedExpression);
			}
		}
	}

	public void disableAllComponents(final AjaxRequestTarget target) {
		this.allComponentsEnabled = false;
		this.updateAllComponents(target);
	}

	public void updateAllComponents(final AjaxRequestTarget target) {
		target.add(this.currentDateUsedCheckbox);
		target.add(this.eventTypeElementDropDownChoice);
		target.add(this.attributeDropDownChoice);
		target.add(this.expressionInput);
	}
}
