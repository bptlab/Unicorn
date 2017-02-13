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
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.input.bpmn.BPMNProcessUploadPanel;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableElement;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableExpansion;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableExpansionModel;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableProvider;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNXORGateway;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPoint;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.simulation.DerivationType;
import de.hpi.unicorn.simulation.SimulationUtils;
import de.hpi.unicorn.simulation.Simulator;
import de.hpi.unicorn.utils.SetUtil;
import de.hpi.unicorn.utils.Tuple;

/**
 * Panel representing the content panel for the first tab.
 */
public class BPMNSimulationPanel extends SimulationPanel {

	private static final long serialVersionUID = -7896431319431474548L;
	// private AbstractEapPage abstractEapPage;
	// private TextField<String> instanceNumberInput;
	// private TextField<String> daysNumberInput;
	// private Form<Void> layoutForm;
	private final BPMNProcessUploadPanel processUploadPanel;
	private final SimulationTreeTableProvider<Object> treeTableProvider;
	private LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable;
	private BPMNSimUnexpectedEventPanel unexpectedEventPanel;

	public BPMNSimulationPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id, abstractEapPage);

		this.layoutForm = new Form("outerLayoutForm");

		this.processUploadPanel = new BPMNProcessUploadPanel("bpmnProcessUploadPanel", abstractEapPage);
		this.layoutForm.add(this.processUploadPanel);

		this.treeTableProvider = new SimulationTreeTableProvider<Object>();
		this.createTreeTable(this.layoutForm);

		this.addTabs();

		this.instanceNumberInput = new TextField<String>("instanceNumberInput", Model.of(""));
		this.layoutForm.add(this.instanceNumberInput);

		this.daysNumberInput = new TextField<String>("daysNumberInput", Model.of(""));
		this.layoutForm.add(this.daysNumberInput);

		final AjaxButton simulateButton = new BlockingAjaxButton("simulateButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				final String instanceNumber = BPMNSimulationPanel.this.instanceNumberInput.getValue();
				int numberOfInstances;
				if (instanceNumber != null && !instanceNumber.isEmpty()) {
					numberOfInstances = Integer.parseInt(BPMNSimulationPanel.this.instanceNumberInput.getValue());
				} else {
					numberOfInstances = 1;
				}
				final CorrelationProcess process = CorrelationProcess.findByName(
						BPMNSimulationPanel.this.processUploadPanel.getSelectedProcessName()).get(0);
				final BPMNProcess model = process.getBpmnProcess();
				if (model == null) {
					abstractEapPage.getFeedbackPanel().error("No BPMN model found.");
					target.add(abstractEapPage.getFeedbackPanel());
				} else {
					if (!BPMNSimulationPanel.this.treeTableProvider.hasEmptyFields()) {
						final Map<TypeTreeNode, List<Serializable>> attributeValues = BPMNSimulationPanel.this.treeTableProvider
								.getAttributeValuesFromModel();
						final Map<AbstractBPMNElement, String> elementDurationStrings = BPMNSimulationPanel.this.treeTableProvider
								.getBPMNElementWithDuration();
						final Map<AbstractBPMNElement, DerivationType> elementDerivationTypes = BPMNSimulationPanel.this.treeTableProvider
								.getBPMNElementWithDerivationType();
						final Map<AbstractBPMNElement, String> elementDerivations = BPMNSimulationPanel.this.treeTableProvider
								.getBPMNElementWithDerivation();
						final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, String>>> xorSplitsWithSuccessorProbabilityStrings = BPMNSimulationPanel.this.treeTableProvider
								.getXorSuccessorsWithProbability();
						final Map<BPMNXORGateway, List<Tuple<AbstractBPMNElement, Integer>>> xorSplitsWithSuccessorProbabilities = SimulationUtils
								.convertProbabilityStrings(xorSplitsWithSuccessorProbabilityStrings);

						final Simulator simulator = new Simulator(process, model, attributeValues,
								elementDurationStrings, elementDerivations, elementDerivationTypes,
								xorSplitsWithSuccessorProbabilities);
						simulator.simulate(numberOfInstances);
						abstractEapPage.getFeedbackPanel().success(
								numberOfInstances + " have been simulated over "
										+ BPMNSimulationPanel.this.daysNumberInput.getValue() + " days.");
						target.add(abstractEapPage.getFeedbackPanel());
					} else {
						abstractEapPage.getFeedbackPanel().error("Please fill all attribute fields.");
						target.add(abstractEapPage.getFeedbackPanel());
					}

				}

			}
		};
		this.layoutForm.add(simulateButton);

		this.add(this.layoutForm);
		this.processUploadPanel.setSimulationPanel(this);
	}

	public void updateMonitoringPoints(final AjaxRequestTarget target) {
		// TODO: Werteeingaben für EventTypes der Monitoringpoints ermöglichen
		final BPMNProcess bpmnProcess = this.processUploadPanel.getProcessModel();
		final List<AbstractBPMNElement> elementsWithMonitorinPoints = bpmnProcess.getBPMNElementsWithOutSequenceFlows();
		SimulationTreeTableElement<Object> taskTreeTableElement;
		SimulationTreeTableElement<Object> monitoringPointTreeTableElement;
		SimulationTreeTableElement<Object> eventTypeTreeTableElement;
		SimulationTreeTableElement<Object> attributeTreeTableElement;
		this.treeTableProvider.deleteAllEntries();
		for (final AbstractBPMNElement bpmnElement : elementsWithMonitorinPoints) {
			taskTreeTableElement = new SimulationTreeTableElement<Object>(this.treeTableProvider.getNextID(),
					bpmnElement);
			this.treeTableProvider.addTreeTableElement(taskTreeTableElement);
			for (final MonitoringPoint monitoringPoint : bpmnElement.getMonitoringPoints()) {
				if (monitoringPoint.getEventType() != null) {
					monitoringPointTreeTableElement = new SimulationTreeTableElement<Object>(
							this.treeTableProvider.getNextID(), monitoringPoint);
					this.treeTableProvider.addTreeTableElementWithParent(monitoringPointTreeTableElement,
							taskTreeTableElement);
					eventTypeTreeTableElement = new SimulationTreeTableElement<Object>(
							this.treeTableProvider.getNextID(), monitoringPoint.getEventType());
					this.treeTableProvider.addTreeTableElementWithParent(eventTypeTreeTableElement,
							monitoringPointTreeTableElement);
					for (final TypeTreeNode attribute : monitoringPoint.getEventType().getValueTypes()) {
						attributeTreeTableElement = new SimulationTreeTableElement<Object>(
								this.treeTableProvider.getNextID(), attribute);
						this.treeTableProvider.addTreeTableElementWithParent(attributeTreeTableElement,
								eventTypeTreeTableElement);
					}
				} else {
					this.treeTableProvider.deleteAllEntries();
					this.abstractEapPage.getFeedbackPanel().error("Monitoring point with no matching event type!");
					if (target != null) {
						target.add(this.treeTable);
					}
					return;
				}

			}
		}
		this.treeTableProvider
				.setCorrelationAttributes(this.processUploadPanel.getProcess().getCorrelationAttributes());
		if (target != null) {
			target.add(this.abstractEapPage.getFeedbackPanel());
		}
	}

	private void createTreeTable(final Form<Void> layoutForm) {
		final List<IColumn<SimulationTreeTableElement<Object>, String>> columns = this.createColumns();

		this.treeTable = new LabelTreeTable<SimulationTreeTableElement<Object>, String>("monitoringPointTree", columns,
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
				final Object content = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getContent();
				if (content instanceof AbstractBPMNElement
						&& SetUtil.containsXorSplit(((AbstractBPMNElement) content).getPredecessors())) {

					final ProbabilityEntryPanel probabilityEntryPanel = new ProbabilityEntryPanel(componentId, entryId,
							BPMNSimulationPanel.this.treeTableProvider);
					cellItem.add(probabilityEntryPanel);
					probabilityEntryPanel.setTable(BPMNSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, BPMNSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn(new Model("Value")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).editableColumnsVisible()) {
					final TextFieldEntryPanel textFieldEntryPanel = new TextFieldEntryPanel(componentId, entryId,
							BPMNSimulationPanel.this.treeTableProvider);
					cellItem.add(textFieldEntryPanel);
					textFieldEntryPanel.setTable(BPMNSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, BPMNSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn(new Model("Derivation-Type")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).getContent() instanceof AbstractBPMNElement) {
					final DerivationTypeDropDownChoicePanel derivationChoicePanel = new DerivationTypeDropDownChoicePanel(
							componentId, entryId, BPMNSimulationPanel.this.treeTableProvider);
					cellItem.add(derivationChoicePanel);
					derivationChoicePanel.setTable(BPMNSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, BPMNSimulationPanel.this.treeTableProvider));
				}
			}
		});

		columns.add(new AbstractColumn(new Model("Duration")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final int entryId = ((SimulationTreeTableElement<Object>) rowModel.getObject()).getID();
				if (((SimulationTreeTableElement<Object>) rowModel.getObject()).getContent() instanceof AbstractBPMNElement) {
					final DurationEntryPanel durationEntryPanel = new DurationEntryPanel(componentId, entryId,
							BPMNSimulationPanel.this.treeTableProvider);
					cellItem.add(durationEntryPanel);
					durationEntryPanel.setTable(BPMNSimulationPanel.this.treeTable);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, BPMNSimulationPanel.this.treeTableProvider));
				}
			}
		});

		return columns;
	}

	public Component getMonitoringPointTable() {
		return this.treeTable;
	}

	@Override
	protected void addUnexpectedEventPanel(final List<ITab> tabs) {
		tabs.add(new AbstractTab(new Model<String>("Unexpected Events (instance-dependent)")) {

			@Override
			public Panel getPanel(final String panelId) {
				BPMNSimulationPanel.this.unexpectedEventPanel = new BPMNSimUnexpectedEventPanel(panelId,
						BPMNSimulationPanel.this.simulationPanel);
				return BPMNSimulationPanel.this.unexpectedEventPanel;
			}
		});

	}
};
