/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansion;
import de.hpi.unicorn.application.components.tree.AttributeTreeExpansionModel;
import de.hpi.unicorn.application.components.tree.AttributeTreeProvider;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.transformation.AdvancedTransformationRuleEditorPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.AttributeSelectionPanel;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.transformation.element.externalknowledge.ExternalKnowledgeExpressionSet;

public class AttributeTreePanel extends Panel {

	private static final long serialVersionUID = -3517674159437927655L;
	private final Form<Void> layoutForm;
	private DropDownChoice<EapEventType> eventTypeDropDownChoice;
	private AttributeTreeProvider attributeTreeTableProvider;
	private LabelTreeTable<TypeTreeNode, String> attributeTreeTable;
	private final AdvancedTransformationRuleEditorPanel advancedRuleEditorPanel;
	private Map<String, String> attributeIdentifiersAndExpressions;
	private Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge;

	public AttributeTreePanel(final String id, final AdvancedTransformationRuleEditorPanel advancedRuleEditorPanel) {
		super(id);

		this.advancedRuleEditorPanel = advancedRuleEditorPanel;
		this.attributeIdentifiersAndExpressions = new HashMap<String, String>();
		this.attributeIdentifiersWithExternalKnowledge = new HashMap<String, ExternalKnowledgeExpressionSet>();

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.buildEventTypeDropDownChoice();
		this.buildAttributeTreeTable();
	}

	private void buildEventTypeDropDownChoice() {

		final List<EapEventType> eventTypes = EapEventType.findAll();
		this.eventTypeDropDownChoice = new DropDownChoice<EapEventType>("eventTypeDropDownChoice",
				new Model<EapEventType>(), eventTypes);
		this.eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				if (AttributeTreePanel.this.eventTypeDropDownChoice.getModelObject() != null) {
					final List<TypeTreeNode> rootAttributes = new ArrayList<TypeTreeNode>();
					final TypeTreeNode timestampAttribute = new TypeTreeNode(
							AttributeTreePanel.this.eventTypeDropDownChoice.getModelObject().getTimestampName(),
							AttributeTypeEnum.DATE);
					timestampAttribute.setTimestamp(true);
					rootAttributes.add(timestampAttribute);
					rootAttributes.addAll(AttributeTreePanel.this.eventTypeDropDownChoice.getModelObject()
							.getRootLevelValueTypes());
					AttributeTreePanel.this.attributeTreeTableProvider = new AttributeTreeProvider(rootAttributes);
					AttributeTreePanel.this.attributeIdentifiersAndExpressions.keySet().clear();
					AttributeTreePanel.this.attributeIdentifiersAndExpressions.put("Timestamp", "");
					for (final String attributeIdentifier : AttributeTreePanel.this.eventTypeDropDownChoice
							.getModelObject().getAttributeExpressionsWithoutTimestampName()) {
						AttributeTreePanel.this.attributeIdentifiersAndExpressions.put(attributeIdentifier, "");
					}
					AttributeTreePanel.this.renderOrUpdateAttributeTreeTable();
					target.add(AttributeTreePanel.this.attributeTreeTable);
				} else {
					AttributeTreePanel.this.attributeTreeTableProvider = new AttributeTreeProvider();
					AttributeTreePanel.this.renderOrUpdateAttributeTreeTable();
					target.add(AttributeTreePanel.this.attributeTreeTable);
				}
			}
		});
		this.eventTypeDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.eventTypeDropDownChoice);
	}

	private void buildAttributeTreeTable() {
		this.attributeTreeTableProvider = new AttributeTreeProvider();
		this.renderOrUpdateAttributeTreeTable();
	}

	public DropDownChoice<EapEventType> getEventTypeDropDownChoice() {
		return this.eventTypeDropDownChoice;
	}

	public EapEventType getSelectedEventType() {
		return this.eventTypeDropDownChoice.getModelObject();
	}

	public void setSelectedEventType(final EapEventType selectedEventType) {
		this.eventTypeDropDownChoice.setChoices(EapEventType.findAll());
		this.eventTypeDropDownChoice.setModelObject(selectedEventType);
	}

	public Map<String, String> getAttributeIdentifiersAndExpressions() {
		return this.attributeIdentifiersAndExpressions;
	}

	public LabelTreeTable<TypeTreeNode, String> getAttributeTreeTable() {
		return this.attributeTreeTable;
	}

	public AttributeTreeProvider getAttributeTreeTableProvider() {
		return this.attributeTreeTableProvider;
	}

	public void setAttributeTreeTableProvider(final AttributeTreeProvider attributeTreeTableProvider) {
		this.attributeTreeTableProvider = attributeTreeTableProvider;
	}

	public Map<String, ExternalKnowledgeExpressionSet> getAttributeIdentifiersWithExternalKnowledge() {
		return this.attributeIdentifiersWithExternalKnowledge;
	}

	public void setAttributeIdentifiersWithExternalKnowledge(
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge) {
		this.attributeIdentifiersWithExternalKnowledge = attributeIdentifiersWithExternalKnowledge;
	}

	private void renderOrUpdateAttributeTreeTable() {

		final List<IColumn<TypeTreeNode, String>> columns = this.createColumns();

		this.attributeTreeTable = new LabelTreeTable<TypeTreeNode, String>("attributeTreeTable", columns,
				this.attributeTreeTableProvider, Integer.MAX_VALUE, new AttributeTreeExpansionModel());

		this.attributeTreeTable.setOutputMarkupId(true);
		AttributeTreeExpansion.get().expandAll();
		this.attributeTreeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.attributeTreeTable.getTable(), this.attributeTreeTableProvider));

		this.layoutForm.addOrReplace(this.attributeTreeTable);
	}

	public void updateAttributeTreeTable(final AjaxRequestTarget target,
			final Map<String, String> attributeIdentifiersAndExpressions,
			final Map<String, ExternalKnowledgeExpressionSet> attributeIdentifiersWithExternalKnowledge) {
		final List<TypeTreeNode> rootAttributes = new ArrayList<TypeTreeNode>();
		final TypeTreeNode timestampAttribute = new TypeTreeNode(this.eventTypeDropDownChoice.getModelObject()
				.getTimestampName(), AttributeTypeEnum.DATE);
		timestampAttribute.setTimestamp(true);
		rootAttributes.add(timestampAttribute);
		rootAttributes.addAll(this.eventTypeDropDownChoice.getModelObject().getRootLevelValueTypes());
		this.attributeTreeTableProvider = new AttributeTreeProvider(rootAttributes);
		this.attributeIdentifiersAndExpressions = attributeIdentifiersAndExpressions;
		this.attributeIdentifiersWithExternalKnowledge = attributeIdentifiersWithExternalKnowledge;
		this.renderOrUpdateAttributeTreeTable();
		target.add(this.attributeTreeTable);
	}

	private List<IColumn<TypeTreeNode, String>> createColumns() {
		final List<IColumn<TypeTreeNode, String>> columns = new ArrayList<IColumn<TypeTreeNode, String>>();

		columns.add(new PropertyColumn<TypeTreeNode, String>(Model.of("ID"), "ID"));
		columns.add(new TreeColumn<TypeTreeNode, String>(Model.of("Attributes")));

		columns.add(new AbstractColumn<TypeTreeNode, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final TypeTreeNode attribute = ((TypeTreeNode) rowModel.getObject());
				cellItem.add(new AttributeSelectionPanel(componentId, attribute,
						AttributeTreePanel.this.attributeIdentifiersAndExpressions,
						AttributeTreePanel.this.attributeIdentifiersWithExternalKnowledge,
						AttributeTreePanel.this.advancedRuleEditorPanel.getPatternBuilderPanel()));

			}
		});

		return columns;
	}

	public void clear(final AjaxRequestTarget target) {
		this.eventTypeDropDownChoice.setModelObject(null);
		target.add(this.eventTypeDropDownChoice);

		this.attributeTreeTableProvider = new AttributeTreeProvider(new ArrayList<TypeTreeNode>());
		this.attributeIdentifiersAndExpressions.keySet().clear();
		this.renderOrUpdateAttributeTreeTable();
		target.add(this.attributeTreeTable);
	}
}
