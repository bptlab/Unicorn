/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.table.SelectEntryPanel;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableElement;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableExpansion;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableExpansionModel;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableToModelConverter;
import de.hpi.unicorn.bpmn.decomposition.ANDComponent;
import de.hpi.unicorn.bpmn.decomposition.IPattern;
import de.hpi.unicorn.bpmn.decomposition.LoopComponent;
import de.hpi.unicorn.bpmn.decomposition.SequenceComponent;
import de.hpi.unicorn.bpmn.decomposition.XORComponent;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.event.collection.EventTree;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.simulation.DerivationType;
import de.hpi.unicorn.simulation.SimulationUtils;
import de.hpi.unicorn.simulation.Simulator;
import de.hpi.unicorn.utils.Tuple;

/**
 * Panel representing the content panel for the first tab.
 */
public class SimpleSimulationPanel extends SimulationPanel {

	private static final long serialVersionUID = -7896431319431474548L;
	private DropDownChoice<String> processSelect;
	private DropDownChoice<String> eventTypeSelect;
	private List<String> processNameList;
	private final List<String> eventTypeAndPatternList = new ArrayList<String>();
	private CorrelationProcess selectedProcess;
	private LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable;
	private UnexpectedEventPanel unexpectedEventPanel;

	public SimpleSimulationPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id, abstractEapPage);

		this.createProcessList();
		this.createEventTypeList(this.selectedProcess);

		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.layoutForm = new WarnOnExitForm("layoutForm");

		this.addProcessSelect(this.layoutForm);

		this.addEventTypeSelect(this.layoutForm);

		this.addButtons(this.layoutForm);

		this.createTreeTable(this.layoutForm);

		this.instanceNumberInput = new TextField<String>("instanceNumberInput", Model.of(""));
		this.layoutForm.add(this.instanceNumberInput);

		this.daysNumberInput = new TextField<String>("daysNumberInput", Model.of(""));
		this.layoutForm.add(this.daysNumberInput);

		this.add(this.layoutForm);

		this.addTabs();
	}

	private void addButtons(final Form<Void> layoutForm) {
		final AjaxButton addButton = new AjaxButton("addButton", layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				if (SimpleSimulationPanel.this.treeTableProvider.getSelectedTreeTableElements().size() > 1) {
					SimpleSimulationPanel.this.abstractEapPage.getFeedbackPanel().error(
							"Please select only one value to add elements!");
					SimpleSimulationPanel.this.abstractEapPage.getFeedbackPanel().setVisible(true);
					target.add(SimpleSimulationPanel.this.abstractEapPage.getFeedbackPanel());
				} else if (!SimpleSimulationPanel.this.eventTypeSelect.getValue().isEmpty()) {
					// TODO: Element im TreeTableProvider hinzufügen
					final String eventTypeSelectValue = SimpleSimulationPanel.this.eventTypeSelect.getChoices().get(
							Integer.parseInt(SimpleSimulationPanel.this.eventTypeSelect.getValue()));
					SimulationTreeTableElement<Object> treeTableElement;
					if (IPattern.contains(eventTypeSelectValue)) {
						AbstractBPMNElement component = null;
						if (eventTypeSelectValue.equals(IPattern.AND.value)) {
							component = new ANDComponent(null, null, null, null);
						} else if (eventTypeSelectValue.equals(IPattern.XOR.value)) {
							component = new XORComponent(null, null, null, null);
						} else if (eventTypeSelectValue.equals(IPattern.SEQUENCE.value)) {
							component = new SequenceComponent(null, null, null, null);
						} else if (eventTypeSelectValue.equals(IPattern.LOOP.value)) {
							component = new LoopComponent(null, null, null, null);
						}
						treeTableElement = new SimulationTreeTableElement<Object>(
								SimpleSimulationPanel.this.treeTableProvider.getNextID(), component);
						SimpleSimulationPanel.this.treeTableProvider.addTreeTableElement(treeTableElement);
					} else {
						final EapEventType eventType = EapEventType.findByTypeName(eventTypeSelectValue);
						if (eventType != null) {
							treeTableElement = new SimulationTreeTableElement<Object>(
									SimpleSimulationPanel.this.treeTableProvider.getNextID(), eventType);
							SimpleSimulationPanel.this.treeTableProvider.addTreeTableElement(treeTableElement);
							SimulationTreeTableElement<Object> childTreeTableElement;
							for (final TypeTreeNode attribute : eventType.getValueTypes()) {
								childTreeTableElement = new SimulationTreeTableElement<Object>(
										SimpleSimulationPanel.this.treeTableProvider.getNextID(), attribute);
								SimpleSimulationPanel.this.treeTableProvider.addTreeTableElementWithParent(
										childTreeTableElement, treeTableElement);
							}
						}
					}
					SimpleSimulationPanel.this.advancedValuesPanel.refreshAttributeChoice();
					target.add(SimpleSimulationPanel.this.advancedValuesPanel);
					SimpleSimulationPanel.this.unexpectedEventPanel.refreshUsedEventTypes();
					target.add(SimpleSimulationPanel.this.unexpectedEventPanel);
					target.add(SimpleSimulationPanel.this.treeTable);
				}
			}
		};
		layoutForm.add(addButton);

		final AjaxButton editButton = new AjaxButton("editButton", layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
			}
		};
		layoutForm.add(editButton);

		final AjaxButton deleteButton = new AjaxButton("deleteButton", layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				SimpleSimulationPanel.this.treeTableProvider.deleteSelectedEntries();
				target.add(SimpleSimulationPanel.this.treeTable);
			}
		};
		layoutForm.add(deleteButton);

		final AjaxButton simulateButton = new BlockingAjaxButton("simulateButton", layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				final String instanceNumber = SimpleSimulationPanel.this.instanceNumberInput.getValue();
				int numberOfInstances;
				if (instanceNumber != null && !instanceNumber.isEmpty()) {
					numberOfInstances = Integer.parseInt(SimpleSimulationPanel.this.instanceNumberInput.getValue());
				} else {
					numberOfInstances = 1;
				}

				final String daysNumber = SimpleSimulationPanel.this.daysNumberInput.getValue();
				int numberOfDays;
				if (daysNumber != null && !instanceNumber.isEmpty()) {
					numberOfDays = Integer.parseInt(SimpleSimulationPanel.this.daysNumberInput.getValue());
				} else {
					numberOfDays = 1;
				}

				final EventTree<Object> modelTree = SimpleSimulationPanel.this.treeTableProvider.getModelAsTree();

				final Map<TypeTreeNode, List<Serializable>> attributeValues = SimpleSimulationPanel.this.treeTableProvider
						.getAttributeValuesFromModel();
				final BPMNProcess model = new SimulationTreeTableToModelConverter().convertTreeToModel(modelTree);

				// TODO: wahrscheinlichkeiten auslesen
				final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> xorSplitsWithSuccessorProbabilityStrings = this
						.getProbabilityForXorSuccessors(model);
				// System.out.println(xorSplitsWithSuccessorProbabilityStrings);
				final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> xorSplitsWithSuccessorProbabilities = SimulationUtils
						.convertProbabilityStrings(xorSplitsWithSuccessorProbabilityStrings);
				// System.out.println(xorSplitsWithSuccessorProbabilities);
				final Map<EapEventType, String> eventTypesDurationStrings = SimpleSimulationPanel.this.treeTableProvider
						.getEventTypesWithDuration();
				final Map<AbstractBPMNElement, String> tasksDurationString = SimulationUtils
						.getBPMNElementsFromEventTypes(eventTypesDurationStrings, model);

				final Map<EapEventType, String> eventTypesDerivationStrings = SimpleSimulationPanel.this.treeTableProvider
						.getEventTypesWithDuration();
				final Map<AbstractBPMNElement, String> tasksDerivationString = SimulationUtils
						.getBPMNElementsFromEventTypes(eventTypesDerivationStrings, model);

				final Map<EapEventType, DerivationType> eventTypesDerivationTypes = SimpleSimulationPanel.this.treeTableProvider
						.getEventTypesWithDerivationType();
				final Map<AbstractBPMNElement, DerivationType> tasksDerivationTypes = SimulationUtils
						.getBPMNElementsFromEventTypes2(eventTypesDerivationTypes, model);

				final CorrelationProcess process = CorrelationProcess.findByName(
						SimpleSimulationPanel.this.processSelect.getModelObject()).get(0);
				final Simulator simulator = new Simulator(process, model, attributeValues, tasksDurationString,
						tasksDerivationString, tasksDerivationTypes, xorSplitsWithSuccessorProbabilities);
				simulator.addAdvancedValueRules(SimpleSimulationPanel.this.advancedValuesPanel.getValueRules());
				simulator.simulate(numberOfInstances, numberOfDays);
			}

			private Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> getProbabilityForXorSuccessors(
					final BPMNProcess model) {
				final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, EapEventType>>> xorPathProbabilityEvents = SimulationUtils
						.getXORSplitsWithFollowingEventTypes(model);
				// System.out.println(xorPathProbabilityEvents);
				final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> xorSuccessorsProbability = new HashMap<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>>();
				// TODO: eventTypes finden --> elternelement besuchen bis xor
				// gefunden -> probability auslesen
				final List<SimulationTreeTableElement<Object>> eventTypeElements = SimpleSimulationPanel.this.treeTableProvider
						.getEventTypeElements();
				// Paare von Nachfolgeelementen des XOR-Splits und 1. folgendem
				// eventTyp durchlaufen
				for (final BPMNXORGateway xorGateway : xorPathProbabilityEvents.keySet()) {
					final List<Tuple<AbstractBPMNElement, EapEventType>> listOfTuples = xorPathProbabilityEvents
							.get(xorGateway);
					final List<Tuple<AbstractBPMNElement, String>> oneXorSuccessors = new ArrayList<Tuple<AbstractBPMNElement, String>>();
					for (final Tuple<AbstractBPMNElement, EapEventType> tuple : listOfTuples) {
						if (tuple.x != null) {
							SimulationTreeTableElement<Object> targetElement = SimpleSimulationPanel.this
									.findEventTypeElementWithEventType(eventTypeElements, tuple);
							while (!(targetElement.getParent().getContent() instanceof XORComponent)) {
								targetElement = targetElement.getParent();
							}
							final Tuple<AbstractBPMNElement, String> successorProbability = new Tuple<AbstractBPMNElement, String>(
									tuple.x, targetElement.getProbability());
							oneXorSuccessors.add(successorProbability);
							xorSuccessorsProbability.put(xorGateway, oneXorSuccessors);
						} else {
							// leerer Pfad
						}
					}
				}
				return xorSuccessorsProbability;
			}
		};
		layoutForm.add(simulateButton);
	}

	private void createTreeTable(final Form<Void> layoutForm) {
		final List<IColumn<SimulationTreeTableElement<Object>, String>> columns = this.createColumns();

		this.treeTable = new LabelTreeTable<SimulationTreeTableElement<Object>, String>("sequenceTree", columns,
				this.treeTableProvider, Integer.MAX_VALUE, new SimulationTreeTableExpansionModel<Object>());

		this.treeTable.setOutputMarkupId(true);

		this.treeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.treeTable.getTable(), this.treeTableProvider));

		SimulationTreeTableExpansion.get().expandAll();

		layoutForm.add(this.treeTable);
	}

	private List<IColumn<SimulationTreeTableElement<Object>, String>> createColumns() {
		final List<IColumn<SimulationTreeTableElement<Object>, String>> columns = new ArrayList<IColumn<SimulationTreeTableElement<Object>, String>>();

		columns.add(new PropertyColumn<SimulationTreeTableElement<Object>, String>(Model.of("ID"), "ID"));
		columns.add(new TreeColumn<SimulationTreeTableElement<Object>, String>(Model.of("Sequence"), "content"));

		columns.add(new AbstractColumn(new Model("Probability")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).hasParent()
						&& ((SimulationTreeTableElement<Object>) rowModel.getObject()).getParent().getContent() instanceof XORComponent) {
					final ProbabilityEntryPanel probabilityEntryPanel = new ProbabilityEntryPanel(componentId, entryId,
							SimpleSimulationPanel.this.treeTableProvider);
					cellItem.add(probabilityEntryPanel);
					probabilityEntryPanel.setTable(SimpleSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, SimpleSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn<SimulationTreeTableElement<Object>, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).canHaveSubElements()) {
					cellItem.add(new SelectEntryPanel(componentId, entryId,
							SimpleSimulationPanel.this.treeTableProvider));
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, SimpleSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn<SimulationTreeTableElement<Object>, String>(new Model("Value")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).editableColumnsVisible()) {
					final TextFieldEntryPanel textFieldEntryPanel = new TextFieldEntryPanel(componentId, entryId,
							SimpleSimulationPanel.this.treeTableProvider);
					cellItem.add(textFieldEntryPanel);
					textFieldEntryPanel.setTable(SimpleSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, SimpleSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn(new Model("Derivation-Type")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).getContent() instanceof EapEventType) {
					final DerivationTypeDropDownChoicePanel derivationChoicePanel = new DerivationTypeDropDownChoicePanel(
							componentId, entryId, SimpleSimulationPanel.this.treeTableProvider);
					cellItem.add(derivationChoicePanel);
					derivationChoicePanel.setTable(SimpleSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, SimpleSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn(new Model("Duration / Time difference to previous")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).getContent() instanceof EapEventType) {
					final DurationEntryPanel textFieldEntryPanel = new DurationEntryPanel(componentId, entryId,
							SimpleSimulationPanel.this.treeTableProvider);
					cellItem.add(textFieldEntryPanel);
					textFieldEntryPanel.setTable(SimpleSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, SimpleSimulationPanel.this.treeTableProvider));
				}
			}
		});

		return columns;
	}

	private void addProcessSelect(final Form<Void> layoutForm) {
		this.processSelect = new DropDownChoice<String>("processSelect", new Model<String>(), this.processNameList);
		this.processSelect.setOutputMarkupId(true);
		this.processSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				final String processValue = SimpleSimulationPanel.this.processSelect.getValue();
				if (processValue != null && !processValue.isEmpty()) {
					final List<CorrelationProcess> processList = CorrelationProcess
							.findByName(SimpleSimulationPanel.this.processSelect.getChoices().get(
									Integer.parseInt(SimpleSimulationPanel.this.processSelect.getValue())));
					if (processList.size() > 0) {
						SimpleSimulationPanel.this.selectedProcess = processList.get(0);

						SimpleSimulationPanel.this.createEventTypeList(SimpleSimulationPanel.this.selectedProcess);
						target.add(SimpleSimulationPanel.this.eventTypeSelect);
					}
				}
				SimpleSimulationPanel.this.treeTableProvider
						.setCorrelationAttributes(SimpleSimulationPanel.this.selectedProcess.getCorrelationAttributes());
			}
		});

		layoutForm.add(this.processSelect);
	}

	private void addEventTypeSelect(final Form<Void> layoutForm) {
		this.eventTypeSelect = new DropDownChoice<String>("eventTypeSelect", new Model<String>(),
				this.eventTypeAndPatternList);
		this.eventTypeSelect.setOutputMarkupId(true);
		layoutForm.add(this.eventTypeSelect);
	}

	private void createProcessList() {
		this.processNameList = new ArrayList<String>();
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNameList.add(process.getName());
		}
	}

	private void createEventTypeList(final CorrelationProcess selectedProcess) {
		this.eventTypeAndPatternList.clear();
		if (selectedProcess != null) {
			for (final EapEventType eventType : selectedProcess.getEventTypes()) {
				this.eventTypeAndPatternList.add(eventType.getTypeName());
			}
			this.eventTypeAndPatternList.addAll(IPattern.getValues());
		}
	}

	private SimulationTreeTableElement<Object> findEventTypeElementWithEventType(
			final List<SimulationTreeTableElement<Object>> eventTypeElements,
			final Tuple<AbstractBPMNElement, EapEventType> tuple) {
		for (final SimulationTreeTableElement<Object> eventTypeElement : eventTypeElements) {
			if (eventTypeElement.getContent() == tuple.y) {
				return eventTypeElement;
			}
		}
		return null;
	}

	@Override
	protected void addUnexpectedEventPanel(final List<ITab> tabs) {

		tabs.add(new AbstractTab(new Model<String>("Unexpected Events (instance-dependent)")) {

			@Override
			public Panel getPanel(final String panelId) {
				SimpleSimulationPanel.this.unexpectedEventPanel = new UnexpectedEventPanel(panelId,
						SimpleSimulationPanel.this.simulationPanel);
				return SimpleSimulationPanel.this.unexpectedEventPanel;
			}
		});

	}
};
