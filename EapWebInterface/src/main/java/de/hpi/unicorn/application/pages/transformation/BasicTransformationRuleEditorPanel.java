/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

import com.espertech.esper.client.EPException;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.SelectTree;
import de.hpi.unicorn.application.components.tree.TreeExpansion;
import de.hpi.unicorn.application.components.tree.TreeExpansionModel;
import de.hpi.unicorn.application.components.tree.TreeProvider;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.transformation.TransformationRule;

public class BasicTransformationRuleEditorPanel extends Panel {

	private static final long serialVersionUID = -3517674159437927655L;
	private String selectedEventTypeName;
	private EapEventType selectedEventType;
	private String transformationRule;
	private TextField<String> transformationRuleNameTextField;
	private String transformationRuleName;
	private final Form<Void> layoutForm;
	private TextArea<String> transformationRuleTextArea;
	private SelectTree<EventTreeElement<String>> transformationRuleTree;
	private EventTree<String> transformationRuleTreeStructure;
	private DropDownChoice<String> eventTypeDropDownChoice;
	private final TransformationPage transformationPage;
	// private CheckBoxMultipleChoice<EapEventType>
	// eventTypesCheckBoxMultipleChoice;
	private List<EapEventType> eventTypesOfIncomingEvents = new ArrayList<EapEventType>();

	public BasicTransformationRuleEditorPanel(final String id, final TransformationPage transformationPage) {
		super(id);

		this.transformationPage = transformationPage;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.buildTextFields();
		this.buildTransformationRuleTree();
		this.buildButtons();
		this.buildEventTypeCheckBoxMultipleChoice();
	}

	private void buildTextFields() {
		this.transformationRuleNameTextField = new TextField<String>("transformationRuleNameTextField",
				new PropertyModel<String>(this, "transformationRuleName"));
		this.transformationRuleNameTextField.setOutputMarkupId(true);
		this.layoutForm.add(this.transformationRuleNameTextField);

		final List<String> eventTypes = EapEventType.getAllTypeNames();
		this.eventTypeDropDownChoice = new DropDownChoice<String>("eventTypeDropDownChoice", new PropertyModel<String>(
				this, "selectedEventTypeName"), eventTypes);
		this.eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				BasicTransformationRuleEditorPanel.this.updateOnChangeOfDropDownChoice(target);
			}
		});
		this.layoutForm.add(this.eventTypeDropDownChoice);

		this.transformationRuleTextArea = new TextArea<String>("transformationRuleTextArea", new PropertyModel<String>(
				this, "transformationRule"));
		this.transformationRuleTextArea.setOutputMarkupId(true);
		this.layoutForm.add(this.transformationRuleTextArea);
	}

	protected void updateOnChangeOfDropDownChoice(final AjaxRequestTarget target) {
		this.selectedEventType = EapEventType.findByTypeName(this.selectedEventTypeName);

		if (this.selectedEventType != null) {

			// inform user about rule schema

			// List<TypeTreeNode> selectedEventTypeAttributes =
			// selectedEventType
			// .getValueTypes();

			final StringBuffer transformationSuggestionBuffer = new StringBuffer();
			transformationSuggestionBuffer.append("Your transformation rule may start with: SELECT ");
			final Iterator<String> iterator = this.selectedEventType.getAttributeExpressions().iterator();
			while (iterator.hasNext()) {
				transformationSuggestionBuffer.append("[...] AS " + iterator.next());
				if (!iterator.hasNext()) {
					transformationSuggestionBuffer.append(" ");
				} else {
					transformationSuggestionBuffer.append(", ");
				}
			}
			// if (selectedEventType.getTimestampName() != null) {
			// transformationSuggestionBuffer.append("[...] AS Timestamp, ");
			//
			// }
			//
			// for (int i = 0; i < selectedEventTypeAttributes.size(); i++) {
			// transformationSuggestionBuffer.append("[...] AS "
			// + selectedEventTypeAttributes.get(i)
			// .getAttributeExpression());
			// if (i == selectedEventTypeAttributes.size() - 1) {
			// transformationSuggestionBuffer.append(" ");
			// } else {
			// transformationSuggestionBuffer.append(", ");
			// }
			// }
			this.transformationPage.getFeedbackPanel().info(transformationSuggestionBuffer.toString());
			target.add(this.transformationPage.getFeedbackPanel());
		} else {
			target.add(this.transformationPage.getFeedbackPanel());
		}
	}

	private void buildButtons() {

		final AjaxButton editButton = new AjaxButton("editButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {

				if (BasicTransformationRuleEditorPanel.this.transformationRuleTree.getSelectedElement() != null) {
					BasicTransformationRuleEditorPanel.this.selectedEventTypeName = BasicTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getParent().getValue().toString();
					BasicTransformationRuleEditorPanel.this.transformationRuleName = BasicTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getValue().toString();
					final TransformationRule rule = TransformationRule.findByEventTypeAndTitle(
							BasicTransformationRuleEditorPanel.this.selectedEventTypeName,
							BasicTransformationRuleEditorPanel.this.transformationRuleName);
					BasicTransformationRuleEditorPanel.this.transformationRule = rule.getQuery();
					BasicTransformationRuleEditorPanel.this.eventTypesOfIncomingEvents = rule
							.getEventTypesOfIncomingEvents();
					target.add(BasicTransformationRuleEditorPanel.this.transformationRuleNameTextField);
					target.add(BasicTransformationRuleEditorPanel.this.eventTypeDropDownChoice);
					target.add(BasicTransformationRuleEditorPanel.this.transformationRuleTextArea);
					// TODO: CheckBoxMultipleChoice doesn't update - replace it
					// with CheckGroup
					// target.add(eventTypesCheckBoxMultipleChoice);
					BasicTransformationRuleEditorPanel.this.updateOnChangeOfDropDownChoice(target);
				}
			}
		};
		this.layoutForm.add(editButton);

		final AjaxButton deleteButton = new AjaxButton("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				if (BasicTransformationRuleEditorPanel.this.transformationRuleTree.getSelectedElement() != null) {
					final String eventTypeNameFromTree = BasicTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getParent().getValue().toString();
					final String transformationRuleNameFromTree = BasicTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getValue().toString();
					BasicTransformationRuleEditorPanel.this.removeTransformationRule(eventTypeNameFromTree,
							transformationRuleNameFromTree);
					BasicTransformationRuleEditorPanel.this.renderOrUpdateTransformationRuleTree();
					target.add(BasicTransformationRuleEditorPanel.this.transformationRuleTree);
					BasicTransformationRuleEditorPanel.this.clearFields(target);
				}
			}
		};
		this.layoutForm.add(deleteButton);

		final AjaxButton saveButton = new AjaxButton("saveButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				try {
					if (BasicTransformationRuleEditorPanel.this.transformationRuleName == null
							|| BasicTransformationRuleEditorPanel.this.transformationRuleName.isEmpty()) {
						BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel().error(
								"Please enter an transformation rule name!");
						target.add(BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
					} else if (BasicTransformationRuleEditorPanel.this.selectedEventType == null) {
						BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel().error(
								"Please select an event type!");
						target.add(BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
					} else if (BasicTransformationRuleEditorPanel.this.transformationRule == null
							|| BasicTransformationRuleEditorPanel.this.transformationRule.isEmpty()) {
						BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel().error(
								"Please enter an transformation rule!");
						target.add(BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
					} else {
						BasicTransformationRuleEditorPanel.this.saveTransformationRule(target,
								BasicTransformationRuleEditorPanel.this.selectedEventType,
								BasicTransformationRuleEditorPanel.this.eventTypesOfIncomingEvents,
								BasicTransformationRuleEditorPanel.this.transformationRuleName,
								BasicTransformationRuleEditorPanel.this.transformationRule);
					}
				} catch (final EPException e) {
					BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel().error(e.getMessage());
					target.add(BasicTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
				}
			}
		};
		this.layoutForm.add(saveButton);
	}

	private void clearFields(final AjaxRequestTarget target) {
		this.transformationRuleName = "";
		target.add(this.transformationRuleNameTextField);
		this.selectedEventTypeName = null;
		target.add(this.eventTypeDropDownChoice);
		this.transformationRule = "";
		target.add(this.transformationRuleTextArea);
	}

	private void saveTransformationRule(final AjaxRequestTarget target, final EapEventType selectedEventType,
			final List<EapEventType> eventTypesOfIncomingEvents, final String transformationRuleName,
			final String transformationRule) {
		if (TransformationRule.findByEventTypeAndTitle(this.selectedEventTypeName, transformationRuleName) != null) {
			this.removeTransformationRule(this.selectedEventTypeName, transformationRuleName);
		}
		this.addTransformationRule(selectedEventType, eventTypesOfIncomingEvents, transformationRuleName,
				transformationRule);
		this.renderOrUpdateTransformationRuleTree();
		target.add(this.transformationRuleTree);
		this.transformationPage.getFeedbackPanel().success(
				"Saved transformation rule '" + transformationRuleName + "' for event type '"
						+ this.selectedEventTypeName + "'.");
		target.add(this.transformationPage.getFeedbackPanel());
	}

	protected void addTransformationRule(final EapEventType selectedEventType,
			final List<EapEventType> eventTypesOfIncomingEvents, final String transformationRuleName,
			final String transformationQuery) throws EPException {
		final TransformationRule transformationRule = new TransformationRule(selectedEventType,
				eventTypesOfIncomingEvents, transformationRuleName, transformationQuery);
		Broker.getInstance().register(transformationRule);
		transformationRule.save();
		final String eventTypeName = selectedEventType.getTypeName();
		this.addTransformationRuleToTreeStructure(eventTypeName, transformationRuleName);
	}

	protected void removeTransformationRule(final String eventTypeName, final String title) {
		this.transformationRuleTreeStructure.removeChild(eventTypeName, title);
		final TransformationRule transformationRule = TransformationRule.findByEventTypeAndTitle(eventTypeName, title);
		Broker.getInstance().remove(transformationRule);
	}

	private void addTransformationRuleToTreeStructure(final String eventTypeName, final String transformationRuleName) {
		if (!this.transformationRuleTreeStructure.containsRootElement(eventTypeName)) {
			this.transformationRuleTreeStructure.addRootElement(eventTypeName);
		}
		this.transformationRuleTreeStructure.addChild(eventTypeName, transformationRuleName);
	}

	private void buildTransformationRuleTree() {
		this.transformationRuleTreeStructure = new EventTree<String>();
		final List<TransformationRule> transformationRules = TransformationRule.findAll();
		for (final TransformationRule transformationRule : transformationRules) {
			this.addTransformationRuleToTreeStructure(transformationRule.getEventType().getTypeName(),
					transformationRule.getTitle());
		}
		this.renderOrUpdateTransformationRuleTree();
	}

	private void renderOrUpdateTransformationRuleTree() {

		this.transformationRuleTree = new SelectTree<EventTreeElement<String>>("transformationRuleTree",
				new TreeProvider<String>(this.generateNodesOfTransformationRuleTree()),
				new TreeExpansionModel<String>()) {

			private static final long serialVersionUID = 1L;

			@Override
			protected void select(final EventTreeElement<String> element,
					final AbstractTree<EventTreeElement<String>> tree, final AjaxRequestTarget target) {
				// only the transformation rules are selectable
				if (element.hasParent()) {
					super.select(element, tree, target);
				}
			}
		};
		TreeExpansion.get().expandAll();
		this.transformationRuleTree.setOutputMarkupId(true);
		this.layoutForm.addOrReplace(this.transformationRuleTree);
	}

	private ArrayList<EventTreeElement<String>> generateNodesOfTransformationRuleTree() {
		final ArrayList<EventTreeElement<String>> treeElements = new ArrayList<EventTreeElement<String>>();
		final List<String> eventTypes = this.transformationRuleTreeStructure.getRootElements();
		for (final String eventType : eventTypes) {
			final EventTreeElement<String> rootElement = new EventTreeElement<String>(eventType);
			treeElements.add(rootElement);
			if (this.transformationRuleTreeStructure.hasChildren(eventType)) {
				this.fillTreeLevel(rootElement, this.transformationRuleTreeStructure.getChildren(eventType),
						this.transformationRuleTreeStructure);
			}
		}
		return treeElements;
	}

	private void fillTreeLevel(final EventTreeElement<String> parent, final List<String> children,
			final EventTree<String> transformationRuleTreeStructure) {
		for (final String newValue : children) {
			new EventTreeElement<String>(parent, newValue.toString());
			// EventTreeElement<String> newElement = new
			// EventTreeElement<String>(parent, newValue.toString());
			// root level contains event types and first level contains
			// transformation rules, no need to add child nodes for
			// transformation rules
			// if (transformationRuleTreeStructure.hasChildren(newValue)) {
			// fillTreeLevel(newElement,
			// transformationRuleTreeStructure.getChildren(newValue),
			// transformationRuleTreeStructure);
			// }
		}
	}

	private void buildEventTypeCheckBoxMultipleChoice() {
		// List<EapEventType> eventTypes = EapEventType.findAll();
		// eventTypesCheckBoxMultipleChoice = new
		// CheckBoxMultipleChoice<EapEventType>(
		// "eventTypesCheckBoxMultipleChoice",
		// new PropertyModel<List<EapEventType>>(this,
		// "eventTypesOfIncomingEvents"), eventTypes);
		// eventTypesCheckBoxMultipleChoice.setOutputMarkupId(true);
		// layoutForm.add(eventTypesCheckBoxMultipleChoice);
	}
}
