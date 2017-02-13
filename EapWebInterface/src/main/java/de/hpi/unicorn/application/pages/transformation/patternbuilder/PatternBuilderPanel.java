/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation.patternbuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.TreeExpansion;
import de.hpi.unicorn.application.components.tree.TreeExpansionModel;
import de.hpi.unicorn.application.components.tree.TreeTableProvider;
import de.hpi.unicorn.application.pages.transformation.AdvancedTransformationRuleEditorPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.ElementOptionsPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.EventTypeAliasPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.EventTypeElementOptionsPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.EveryDistinctPatternOperatorPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.FilterExpressionPanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.PatternElementTreeTable;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.RepeatPatternOperatorRangePanel;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.model.UntilPatternOperatorRangePanel;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.collection.EventTreeElement;
import de.hpi.unicorn.transformation.collection.TransformationPatternTree;
import de.hpi.unicorn.transformation.element.EventTypeElement;
import de.hpi.unicorn.transformation.element.FilterExpressionConnectorElement;
import de.hpi.unicorn.transformation.element.FilterExpressionConnectorEnum;
import de.hpi.unicorn.transformation.element.FilterExpressionElement;
import de.hpi.unicorn.transformation.element.PatternOperatorElement;
import de.hpi.unicorn.transformation.element.PatternOperatorEnum;

public class PatternBuilderPanel extends Panel {

	private static final long serialVersionUID = -3517674159437927655L;
	private EapEventType selectedEventType;
	private final Form<Void> layoutForm;
	private DropDownChoice<PatternOperatorEnum> patternOperatorDropDownChoice;
	private PatternOperatorEnum selectedPatternOperator;
	private DropDownChoice<EapEventType> eventTypeDropDownChoice;
	private final TreeTableProvider<Serializable> patternTreeTableProvider;
	private PatternElementTreeTable patternTreeTable;

	private TransformationPatternTree patternTree;
	private final PatternBuilderPanel patternBuilderPanel;
	private final AdvancedTransformationRuleEditorPanel advancedRuleEditorPanel;
	private DropDownChoice<FilterExpressionConnectorEnum> filterExpressionConnectorDropDownChoice;
	private FilterExpressionConnectorEnum filterExpressionConnector;

	public PatternBuilderPanel(final String id, final AdvancedTransformationRuleEditorPanel advancedRuleEditorPanel) {
		super(id);

		this.advancedRuleEditorPanel = advancedRuleEditorPanel;
		this.patternBuilderPanel = this;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.patternTree = new TransformationPatternTree();
		this.patternTreeTableProvider = new TreeTableProvider<Serializable>(this.patternTree.getRoots());

		this.buildEventTypeDropDownChoice();
		this.buildPatternOperatorDropDownChoice();
		this.buildFilterExpressionConnectorDropDownChoice();
		this.buildButtons();
		this.buildPatternTreeTable();
	}

	private void buildEventTypeDropDownChoice() {

		final List<EapEventType> eventTypes = EapEventType.findAll();
		this.eventTypeDropDownChoice = new DropDownChoice<EapEventType>("eventTypeDropDownChoice",
				new PropertyModel<EapEventType>(this, "selectedEventType"), eventTypes);
		this.eventTypeDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.eventTypeDropDownChoice);
	}

	private void buildPatternOperatorDropDownChoice() {
		this.patternOperatorDropDownChoice = new DropDownChoice<PatternOperatorEnum>("patternOperatorDropDownChoice",
				new PropertyModel<PatternOperatorEnum>(this, "selectedPatternOperator"),
				Arrays.asList(PatternOperatorEnum.values()));
		this.patternOperatorDropDownChoice.setOutputMarkupId(true);
		this.patternOperatorDropDownChoice.setEnabled(false);
		this.layoutForm.add(this.patternOperatorDropDownChoice);
	}

	private void buildFilterExpressionConnectorDropDownChoice() {
		this.filterExpressionConnectorDropDownChoice = new DropDownChoice<FilterExpressionConnectorEnum>(
				"filterExpressionConnectorDropDownChoice", new PropertyModel<FilterExpressionConnectorEnum>(this,
						"filterExpressionConnector"), Arrays.asList(FilterExpressionConnectorEnum.values()));
		this.filterExpressionConnectorDropDownChoice.setOutputMarkupId(true);
		this.filterExpressionConnectorDropDownChoice.setEnabled(false);
		this.layoutForm.add(this.filterExpressionConnectorDropDownChoice);
	}

	private void buildButtons() {

		final AjaxButton addButton = new AjaxButton("addButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				EventTreeElement<Serializable> newTreeElement;
				Iterator<EventTreeElement<Serializable>> iterator;
				if (PatternBuilderPanel.this.eventTypeDropDownChoice.isEnabled()
						&& PatternBuilderPanel.this.selectedEventType != null) {
					newTreeElement = new EventTypeElement(
							PatternBuilderPanel.this.patternTreeTableProvider.getNextID(),
							PatternBuilderPanel.this.selectedEventType);
					if (PatternBuilderPanel.this.patternTreeTable.getSelectedElements().isEmpty()) {
						PatternBuilderPanel.this.patternTree.addElement(newTreeElement);
						PatternBuilderPanel.this.patternTreeTableProvider
								.setRootElements(PatternBuilderPanel.this.patternTree.getRoots());
						target.add(PatternBuilderPanel.this.advancedRuleEditorPanel.getAttributeTreePanel()
								.getAttributeTreeTable());
					}
				} else {
					if (PatternBuilderPanel.this.patternOperatorDropDownChoice.isEnabled()
							&& PatternBuilderPanel.this.selectedPatternOperator != null) {
						newTreeElement = new PatternOperatorElement(
								PatternBuilderPanel.this.patternTreeTableProvider.getNextID(),
								PatternBuilderPanel.this.selectedPatternOperator);
					} else if (PatternBuilderPanel.this.filterExpressionConnectorDropDownChoice.isEnabled()
							&& PatternBuilderPanel.this.filterExpressionConnector != null) {
						newTreeElement = new FilterExpressionConnectorElement(
								PatternBuilderPanel.this.patternTreeTableProvider.getNextID(),
								PatternBuilderPanel.this.filterExpressionConnector);
					} else {
						return;
					}
					iterator = PatternBuilderPanel.this.patternTreeTable.getSelectedElements().iterator();
					final EventTreeElement<Serializable> parentOfSelectedElements = iterator.next().getParent();
					iterator = PatternBuilderPanel.this.patternTreeTable.getSelectedElements().iterator();
					while (iterator.hasNext()) {
						iterator.next().setParent(newTreeElement);
					}
					if (parentOfSelectedElements != null) {
						newTreeElement.setParent(parentOfSelectedElements);
						iterator = PatternBuilderPanel.this.patternTreeTable.getSelectedElements().iterator();
						while (iterator.hasNext()) {
							parentOfSelectedElements.removeChild(iterator.next());
						}
					}
					PatternBuilderPanel.this.patternTree.addElement(newTreeElement);
					PatternBuilderPanel.this.patternTreeTableProvider
							.setRootElements(PatternBuilderPanel.this.patternTree.getRoots());
				}

				PatternBuilderPanel.this.patternTreeTable.getSelectedElements().clear();
				target.add(PatternBuilderPanel.this.patternTreeTable);
				PatternBuilderPanel.this.updateOnTreeElementSelection(target);
			}
		};
		this.layoutForm.add(addButton);
	}

	public DropDownChoice<EapEventType> getEventTypeDropDownChoice() {
		return this.eventTypeDropDownChoice;
	}

	public DropDownChoice<PatternOperatorEnum> getPatternOperatorDropDownChoice() {
		return this.patternOperatorDropDownChoice;
	}

	public DropDownChoice<FilterExpressionConnectorEnum> getFilterExpressionConnectorDropDownChoice() {
		return this.filterExpressionConnectorDropDownChoice;
	}

	public TreeTableProvider<Serializable> getPatternTreeTableProvider() {
		return this.patternTreeTableProvider;
	}

	public PatternElementTreeTable getPatternTreeTable() {
		return this.patternTreeTable;
	}

	public TransformationPatternTree getPatternTree() {
		return this.patternTree;
	}

	public void setPatternTree(final TransformationPatternTree patternTree) {
		this.patternTree = patternTree;
	}

	public AdvancedTransformationRuleEditorPanel getAdvancedRuleEditorPanel() {
		return this.advancedRuleEditorPanel;
	}

	private void buildPatternTreeTable() {

		final List<IColumn<EventTreeElement<Serializable>, String>> columns = this.createColumns();

		this.patternTreeTable = new PatternElementTreeTable("patternTreeTable", columns, this.patternTreeTableProvider,
				Integer.MAX_VALUE, new TreeExpansionModel<Serializable>(), this);

		this.patternTreeTable.setOutputMarkupId(true);
		TreeExpansion.get().expandAll();
		this.patternTreeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.patternTreeTable.getTable(), this.patternTreeTableProvider));

		this.layoutForm.addOrReplace(this.patternTreeTable);
	}

	private List<IColumn<EventTreeElement<Serializable>, String>> createColumns() {
		final List<IColumn<EventTreeElement<Serializable>, String>> columns = new ArrayList<IColumn<EventTreeElement<Serializable>, String>>();

		columns.add(new PropertyColumn<EventTreeElement<Serializable>, String>(Model.of("ID"), "ID"));
		columns.add(new TreeColumn<EventTreeElement<Serializable>, String>(Model.of("Elements")));

		columns.add(new AbstractColumn<EventTreeElement<Serializable>, String>(new Model("")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final Object treeTableElement = rowModel.getObject();
				if (treeTableElement instanceof EventTypeElement) {
					final EventTypeElement eventTypeElement = (EventTypeElement) treeTableElement;
					cellItem.add(new EventTypeAliasPanel(componentId, eventTypeElement,
							PatternBuilderPanel.this.advancedRuleEditorPanel));
				} else {
					cellItem.add(new Label(componentId));
				}
			}
		});

		columns.add(new AbstractColumn<EventTreeElement<Serializable>, String>(new Model("")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final Object treeTableElement = rowModel.getObject();
				if (treeTableElement instanceof EventTypeElement) {
					final EventTypeElement eventTypeElement = (EventTypeElement) treeTableElement;
					cellItem.add(new EventTypeElementOptionsPanel(componentId, eventTypeElement,
							PatternBuilderPanel.this.patternTree, PatternBuilderPanel.this.patternTreeTableProvider,
							PatternBuilderPanel.this.patternTreeTable, PatternBuilderPanel.this.patternBuilderPanel));
				} else if (treeTableElement instanceof FilterExpressionElement) {
					final FilterExpressionElement filterExpressionElement = (FilterExpressionElement) treeTableElement;
					cellItem.add(new FilterExpressionPanel(componentId, filterExpressionElement,
							PatternBuilderPanel.this.patternBuilderPanel));
				} else if (treeTableElement instanceof PatternOperatorElement) {
					final PatternOperatorElement poElement = (PatternOperatorElement) treeTableElement;
					if (poElement.getValue() == PatternOperatorEnum.UNTIL) {
						cellItem.add(new UntilPatternOperatorRangePanel(componentId, poElement));
					} else if (poElement.getValue() == PatternOperatorEnum.REPEAT) {
						cellItem.add(new RepeatPatternOperatorRangePanel(componentId, poElement,
								PatternBuilderPanel.this.advancedRuleEditorPanel));
					} else if (poElement.getValue() == PatternOperatorEnum.EVERY_DISTINCT) {
						cellItem.add(new EveryDistinctPatternOperatorPanel(componentId, poElement,
								PatternBuilderPanel.this.advancedRuleEditorPanel));
					} else {
						cellItem.add(new Label(componentId));
					}
				} else {
					cellItem.add(new Label(componentId));
				}
			}
		});

		columns.add(new AbstractColumn<EventTreeElement<Serializable>, String>(new Model("")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final EventTreeElement<Serializable> treeTableElement = (EventTreeElement<Serializable>) rowModel
						.getObject();
				cellItem.add(new ElementOptionsPanel(componentId, treeTableElement,
						PatternBuilderPanel.this.patternBuilderPanel));
			}
		});

		return columns;
	}

	public void updatePatternTreeTable(final AjaxRequestTarget target) {
		this.patternTreeTableProvider.setRootElements(this.patternTree.getRoots());
		this.patternTreeTable.getSelectedElements().clear();
		target.add(this.patternTreeTable);
		this.updateOnTreeElementSelection(target);
	}

	public void updateOnTreeElementSelection(final AjaxRequestTarget target) {
		final int numberOfSelectedElements = this.patternTreeTable.numberOfSelectedElements();
		List<PatternOperatorEnum> operators;
		List<FilterExpressionConnectorEnum> connectors;
		EventTreeElement<Serializable> firstSelectedElement;
		if (numberOfSelectedElements == 0) {
			this.eventTypeDropDownChoice.setEnabled(true);
			this.patternOperatorDropDownChoice.setEnabled(false);
			this.filterExpressionConnectorDropDownChoice.setEnabled(false);
		} else if (numberOfSelectedElements == 1) {
			firstSelectedElement = this.patternTreeTable.getSelectedElements().iterator().next();
			if (firstSelectedElement instanceof FilterExpressionElement
					|| firstSelectedElement instanceof FilterExpressionConnectorElement) {
				connectors = FilterExpressionConnectorEnum.getUnaryOperators();
				this.filterExpressionConnectorDropDownChoice.setChoices(connectors);
				this.filterExpressionConnectorDropDownChoice.setEnabled(true);
				this.patternOperatorDropDownChoice.setEnabled(false);
			} else {
				operators = PatternOperatorEnum.getUnaryOperators();
				this.patternOperatorDropDownChoice.setChoices(operators);
				this.filterExpressionConnectorDropDownChoice.setEnabled(false);
				this.patternOperatorDropDownChoice.setEnabled(true);
			}
			this.eventTypeDropDownChoice.setEnabled(false);
		} else if (numberOfSelectedElements == 2) {
			firstSelectedElement = this.patternTreeTable.getSelectedElements().iterator().next();
			if (firstSelectedElement instanceof FilterExpressionElement
					|| firstSelectedElement instanceof FilterExpressionConnectorElement) {
				connectors = FilterExpressionConnectorEnum.getBinaryOperators();
				this.filterExpressionConnectorDropDownChoice.setChoices(connectors);
				this.filterExpressionConnectorDropDownChoice.setEnabled(true);
				this.patternOperatorDropDownChoice.setEnabled(false);
			} else {
				operators = PatternOperatorEnum.getBinaryOperators();
				this.patternOperatorDropDownChoice.setChoices(operators);
				this.filterExpressionConnectorDropDownChoice.setEnabled(false);
				this.patternOperatorDropDownChoice.setEnabled(true);
			}
			this.eventTypeDropDownChoice.setEnabled(false);
		} else {
			this.patternOperatorDropDownChoice.setEnabled(false);
			this.eventTypeDropDownChoice.setEnabled(false);
		}
		target.add(this.filterExpressionConnectorDropDownChoice);
		target.add(this.patternOperatorDropDownChoice);
		target.add(this.eventTypeDropDownChoice);
	}

	public void clear(final AjaxRequestTarget target) {
		this.eventTypeDropDownChoice.setEnabled(true);
		this.patternOperatorDropDownChoice.setEnabled(false);
		this.filterExpressionConnectorDropDownChoice.setEnabled(false);
		target.add(this.eventTypeDropDownChoice);
		target.add(this.patternOperatorDropDownChoice);
		target.add(this.filterExpressionConnectorDropDownChoice);
		this.patternTree = new TransformationPatternTree();
		this.patternTreeTableProvider.setRootElements(this.patternTree.getRoots());
		target.add(this.patternTreeTable);
	}
}
