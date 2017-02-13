/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.tree.AbstractTree;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import com.espertech.esper.client.EPException;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.Collapsible;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.SelectTree;
import de.hpi.unicorn.application.components.tree.TreeExpansion;
import de.hpi.unicorn.application.components.tree.TreeExpansionModel;
import de.hpi.unicorn.application.components.tree.TreeProvider;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.AttributeTreePanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.PatternBuilderPanel;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.transformation.TransformationRule;
import de.hpi.unicorn.transformation.TransformationRuleLogic;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

public class AdvancedTransformationRuleEditorPanel extends Panel {

	private static final long serialVersionUID = -3517674159437927655L;
	private TextField<String> transformationRuleNameTextField;
	private String transformationRuleNameFromTree;
	private final Form<Void> layoutForm;
	private SelectTree<EventTreeElement<String>> transformationRuleTree;
	private EventTree<String> transformationRuleTreeStructure;
	private final TransformationPage transformationPage;
	private final AdvancedTransformationRuleEditorPanel advancedRuleEditorPanel;
	protected AttributeTreePanel attributeTreePanel;
	protected PatternBuilderPanel patternBuilderPanel;

	public AdvancedTransformationRuleEditorPanel(final String id, final TransformationPage transformationPage) {
		super(id);

		this.transformationPage = transformationPage;
		this.advancedRuleEditorPanel = this;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.addTabs();
		this.buildTextFields();
		this.buildTransformationRuleTree();
		this.buildButtons();
	}

	private void buildTextFields() {
		this.transformationRuleNameTextField = new TextField<String>("transformationRuleNameTextField",
				new PropertyModel<String>(this, "transformationRuleNameFromTree"));
		this.transformationRuleNameTextField.setOutputMarkupId(true);
		this.layoutForm.add(this.transformationRuleNameTextField);
	}

	private void buildButtons() {

		final AjaxButton editButton = new AjaxButton("editButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {

				if (AdvancedTransformationRuleEditorPanel.this.transformationRuleTree.getSelectedElement() != null) {
					final String eventTypeNameFromTree = AdvancedTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getParent().getValue().toString();
					AdvancedTransformationRuleEditorPanel.this.transformationRuleNameFromTree = AdvancedTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getValue().toString();
					final TransformationRule transformationRule = TransformationRule.findByEventTypeAndTitle(
							eventTypeNameFromTree,
							AdvancedTransformationRuleEditorPanel.this.transformationRuleNameFromTree);
					AdvancedTransformationRuleEditorPanel.this.patternBuilderPanel
							.setPatternTree(new TransformationPatternTree(transformationRule.getPatternTree()
									.getElements()));
					AdvancedTransformationRuleEditorPanel.this.patternBuilderPanel.updatePatternTreeTable(target);
					target.add(AdvancedTransformationRuleEditorPanel.this.transformationRuleNameTextField);
					final EapEventType selectedEventType = transformationRule.getEventType();
					AdvancedTransformationRuleEditorPanel.this.attributeTreePanel
							.setSelectedEventType(selectedEventType);
					target.add(AdvancedTransformationRuleEditorPanel.this.attributeTreePanel
							.getEventTypeDropDownChoice());
					AdvancedTransformationRuleEditorPanel.this.attributeTreePanel.updateAttributeTreeTable(target,
							transformationRule.getAttributeIdentifiersAndExpressions(),
							transformationRule.getAttributeIdentifiersWithExternalKnowledge());
				}
			}
		};
		this.layoutForm.add(editButton);
		//
		final AjaxButton deleteButton = new AjaxButton("deleteButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				if (AdvancedTransformationRuleEditorPanel.this.transformationRuleTree.getSelectedElement() != null) {
					final String eventTypeNameFromTree = AdvancedTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getParent().getValue().toString();
					final String transformationRuleNameFromTree = AdvancedTransformationRuleEditorPanel.this.transformationRuleTree
							.getSelectedElement().getValue().toString();
					AdvancedTransformationRuleEditorPanel.this.removeTransformationRule(eventTypeNameFromTree,
							transformationRuleNameFromTree);
					AdvancedTransformationRuleEditorPanel.this.renderOrUpdateTransformationRuleTree();
					target.add(AdvancedTransformationRuleEditorPanel.this.transformationRuleTree);
					AdvancedTransformationRuleEditorPanel.this.clearFields(target);
				}
			}
		};
		this.layoutForm.add(deleteButton);

		final AjaxButton saveButton = new AjaxButton("saveButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final EapEventType selectedEventType = AdvancedTransformationRuleEditorPanel.this.attributeTreePanel
						.getSelectedEventType();
				final Map<String, String> attributeIdentifiersAndExpressions = AdvancedTransformationRuleEditorPanel.this.attributeTreePanel
						.getAttributeIdentifiersAndExpressions();
				final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge = AdvancedTransformationRuleEditorPanel.this.attributeTreePanel
						.getAttributeIdentifiersWithExternalKnowledge();
				final TransformationPatternTree patternTree = AdvancedTransformationRuleEditorPanel.this.patternBuilderPanel
						.getPatternTree();
				try {
					TransformationRuleLogic.getInstance().checkForValidity(selectedEventType,
							AdvancedTransformationRuleEditorPanel.this.transformationRuleNameFromTree,
							attributeIdentifiersAndExpressions, attributeIdentifiersWithExternalKnowledge, patternTree);
					AdvancedTransformationRuleEditorPanel.this.saveTransformationRule(target, selectedEventType,
							AdvancedTransformationRuleEditorPanel.this.transformationRuleNameFromTree,
							attributeIdentifiersAndExpressions, attributeIdentifiersWithExternalKnowledge, patternTree);
					AdvancedTransformationRuleEditorPanel.this.clearFields(target);
				} catch (final EPException e) {
					AdvancedTransformationRuleEditorPanel.this.transformationPage
							.getFeedbackPanel()
							.error("Transformation rule could not be saved. Please check if you have provided the correct attributes for the pattern and in the attribute selection. Full error message: "
									+ e.getMessage());
					target.add(AdvancedTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
				} catch (final RuntimeException e) {
					AdvancedTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel().error(
							e.getMessage());
					target.add(AdvancedTransformationRuleEditorPanel.this.transformationPage.getFeedbackPanel());
				}
			}
		};
		this.layoutForm.add(saveButton);
	}

	private void clearFields(final AjaxRequestTarget target) {
		this.transformationRuleNameFromTree = "";
		target.add(this.transformationRuleNameTextField);

		this.patternBuilderPanel.clear(target);
		this.attributeTreePanel.clear(target);
	}

	private void saveTransformationRule(final AjaxRequestTarget target, final EapEventType selectedEventType,
			final String transformationRuleName, final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge,
			final TransformationPatternTree patternTree) {
		if (TransformationRule.findByEventTypeAndTitle(selectedEventType.getTypeName(), transformationRuleName) != null) {
			this.removeTransformationRule(selectedEventType.getTypeName(), transformationRuleName);
		}
		this.addTransformationRule(selectedEventType, transformationRuleName, attributeIdentifiersAndExpressions,
				attributeIdentifiersWithExternalKnowledge, patternTree);
		this.renderOrUpdateTransformationRuleTree();
		target.add(this.transformationRuleTree);
		this.transformationPage.getFeedbackPanel().success(
				"Saved transformation rule '" + transformationRuleName + "' for event type '"
						+ selectedEventType.getTypeName() + "'.");
		target.add(this.transformationPage.getFeedbackPanel());
	}

	protected void addTransformationRule(final EapEventType selectedEventType, final String transformationRuleName,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge,
			final TransformationPatternTree patternTree) throws EPException {
		final TransformationRule transformationRule = TransformationRuleLogic.getInstance().createTransformationRule(
				selectedEventType, transformationRuleName, patternTree, attributeIdentifiersAndExpressions,
				attributeIdentifiersWithExternalKnowledge);
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
				new TreeProvider(this.generateNodesOfTransformationRuleTree()), new TreeExpansionModel()) {

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
			final EventTreeElement<String> newElement = new EventTreeElement<String>(parent, newValue.toString());
			if (transformationRuleTreeStructure.hasChildren(newValue)) {
				this.fillTreeLevel(newElement, transformationRuleTreeStructure.getChildren(newValue),
						transformationRuleTreeStructure);
			}
		}
	}

	private void addTabs() {
		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Build pattern")) {
			@Override
			public Panel getPanel(final String panelId) {
				AdvancedTransformationRuleEditorPanel.this.patternBuilderPanel = new PatternBuilderPanel(panelId,
						AdvancedTransformationRuleEditorPanel.this.advancedRuleEditorPanel);
				return AdvancedTransformationRuleEditorPanel.this.patternBuilderPanel;
			}
		});
		tabs.add(new AbstractTab(new Model<String>("Select attribute values")) {
			@Override
			public Panel getPanel(final String panelId) {
				AdvancedTransformationRuleEditorPanel.this.attributeTreePanel = new AttributeTreePanel(panelId,
						AdvancedTransformationRuleEditorPanel.this.advancedRuleEditorPanel);
				return AdvancedTransformationRuleEditorPanel.this.attributeTreePanel;
			}
		});
		this.layoutForm.add(new Collapsible("collapsible", tabs, Model.of(-1)));
	}

	public AttributeTreePanel getAttributeTreePanel() {
		return this.attributeTreePanel;
	}

	public PatternBuilderPanel getPatternBuilderPanel() {
		return this.patternBuilderPanel;
	}

	public TransformationPage getTransformationPage() {
		return this.transformationPage;
	}
}
