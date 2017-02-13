/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.BlockingForm;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.querying.bpmn.modal.BPMNQueryEditorHelpModal;
import de.hpi.unicorn.application.pages.querying.bpmn.model.BPMNTreeTableElement;
import de.hpi.unicorn.application.pages.querying.bpmn.model.BPMNTreeTableExpansionModel;
import de.hpi.unicorn.application.pages.querying.bpmn.model.BPMNTreeTableProvider;
import de.hpi.unicorn.application.pages.simulator.EmptyPanel;
import de.hpi.unicorn.bpmn.decomposition.RPSTBuilder;
import de.hpi.unicorn.bpmn.element.BPMNEventType;
import de.hpi.unicorn.bpmn.element.BPMNIntermediateEvent;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.bpmn.element.BPMNStartEvent;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.monitoring.bpmn.BPMNQueryMonitor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.query.PatternQuery;
import de.hpi.unicorn.query.bpmn.PatternQueryGenerator;
import de.hpi.unicorn.query.bpmn.QueryGenerationException;

/**
 * This page facilitates the creation of {@link PatternQuery}s from a
 * {@link BPMNProcess}. The user has to choose a {@link CorrelationProcess} on
 * the page and can associate monitoring points to the BPMN process elements in
 * the {@link MonitoringPointsPanel}.
 * 
 * @author micha
 */
public class BPMNQueryEditor extends AbstractEapPage {

	private static final long serialVersionUID = -7896431319431474548L;
	private Form<Void> layoutForm;
	private ArrayList<String> bpmnProcessNameList;
	private DropDownChoice<String> bpmnProcessSelect;
	private BPMNProcess selectedBPMNProcess;
	private BlockingAjaxButton createQueriesButton;
	private final BPMNQueryEditor page;
	private LabelTreeTable<BPMNTreeTableElement, String> treeTable;
	private final BPMNTreeTableProvider treeTableProvider;
	private BPMNQueryEditorHelpModal helpModal;

	/**
	 * Constructor for a page, which facilitates the creation of
	 * {@link PatternQuery}s from a {@link BPMNProcess}.
	 */
	public BPMNQueryEditor() {
		super();
		this.page = this;
		this.treeTableProvider = new BPMNTreeTableProvider(this.selectedBPMNProcess);
		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.layoutForm = new BlockingForm("layoutForm");

		this.layoutForm.add(this.addBPMNProcessSelect());

		// Add componentTree
		this.createTreeTable();
		this.addCreateQueriesButton();
		this.addHelpModal();

		this.add(this.layoutForm);
	}

	private DropDownChoice<String> addBPMNProcessSelect() {
		this.bpmnProcessNameList = new ArrayList<String>();
		for (final BPMNProcess bpmnProcess : BPMNProcess.findAll()) {
			this.bpmnProcessNameList.add(bpmnProcess.getName());
		}

		this.bpmnProcessSelect = new DropDownChoice<String>("bpmnProcessSelect", new Model<String>(),
				this.bpmnProcessNameList);
		this.bpmnProcessSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				final String processValue = BPMNQueryEditor.this.bpmnProcessSelect.getValue();
				if (processValue != null && !processValue.isEmpty()) {
					final List<BPMNProcess> processList = BPMNProcess.findByName(BPMNQueryEditor.this.bpmnProcessSelect
							.getChoices().get(Integer.parseInt(BPMNQueryEditor.this.bpmnProcessSelect.getValue())));
					if (processList.size() > 0) {
						BPMNQueryEditor.this.selectedBPMNProcess = processList.get(0);

						BPMNQueryEditor.this.treeTableProvider.setProcess(BPMNQueryEditor.this.selectedBPMNProcess);

						BPMNQueryEditor.this.createTreeTable();
						target.add(BPMNQueryEditor.this.treeTable);
					}
				}
			}
		});
		return this.bpmnProcessSelect;
	}

	private void addHelpModal() {
		this.helpModal = new BPMNQueryEditorHelpModal("helpModal");
		this.add(this.helpModal);

		this.layoutForm.add(new AjaxLink<Void>("showHelpModal") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				BPMNQueryEditor.this.helpModal.show(target);
			}
		});

	}

	private void createTreeTable() {
		final List<IColumn<BPMNTreeTableElement, String>> columns = this.createColumns();

		this.treeTable = new LabelTreeTable<BPMNTreeTableElement, String>("bpmnComponentTreeTable", columns,
				this.treeTableProvider, Integer.MAX_VALUE, new BPMNTreeTableExpansionModel());

		this.treeTable.setOutputMarkupId(true);

		this.treeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.treeTable.getTable(), this.treeTableProvider));

		this.layoutForm.addOrReplace(this.treeTable);
	}

	@SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	private List<IColumn<BPMNTreeTableElement, String>> createColumns() {
		final List<IColumn<BPMNTreeTableElement, String>> columns = new ArrayList<IColumn<BPMNTreeTableElement, String>>();

		columns.add(new TreeColumn<BPMNTreeTableElement, String>(Model.of("BPMN element"), "content"));

		columns.add(new AbstractColumn(new Model("Monitoring Points")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {

				final BPMNTreeTableElement treeTableElement = (BPMNTreeTableElement) rowModel.getObject();

				final int entryId = treeTableElement.getID();
				final boolean isTask = treeTableElement.getContent() instanceof BPMNTask;
				final boolean isMonitorableEvent = (treeTableElement.getContent() instanceof BPMNIntermediateEvent && !((BPMNIntermediateEvent) treeTableElement
						.getContent()).getIntermediateEventType().equals(BPMNEventType.Timer))
						|| (treeTableElement.getContent() instanceof BPMNStartEvent);

				if (isTask || isMonitorableEvent) {
					final MonitoringPointsPanel monitoringPointsPanel = new MonitoringPointsPanel(componentId, entryId,
							treeTableElement);
					cellItem.add(monitoringPointsPanel);
				} else {
					cellItem.add(new EmptyPanel(componentId, entryId, BPMNQueryEditor.this.treeTableProvider));
				}
			}
		});

		// TODO: Add value selection possibility

		return columns;
	}

	private void addCreateQueriesButton() {
		this.createQueriesButton = new BlockingAjaxButton("createQueries", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);

				if (BPMNQueryEditor.this.selectedBPMNProcess == null) {
					BPMNQueryEditor.this.page.getFeedbackPanel().error("Select a BPMN process!");
					target.add(BPMNQueryEditor.this.page.getFeedbackPanel());
				} else {
					final CorrelationProcess process = CorrelationProcess
							.findByBPMNProcess(BPMNQueryEditor.this.selectedBPMNProcess);
					if (process == null) {
						BPMNQueryEditor.this.page.getFeedbackPanel()
								.error("Process does not exit for this BPMN model!");
						target.add(BPMNQueryEditor.this.page.getFeedbackPanel());
					}
					if (!process.hasCorrelation()) {
						BPMNQueryEditor.this.page.getFeedbackPanel().error("No correlation exists for process!");
						target.add(BPMNQueryEditor.this.page.getFeedbackPanel());
					} else {
						// BPMNProcess in RPST umwandeln
						final RPSTBuilder rpst = new RPSTBuilder(BPMNQueryEditor.this.selectedBPMNProcess);

						BPMNQueryMonitor.getInstance().getProcessMonitorForProcess(process).getProcess()
								.setProcessDecompositionTree(rpst.getProcessDecompositionTree());

						// Queries erzeugen und bei Esper registrieren
						// RPST in Queries umwandeln
						try {
							final PatternQueryGenerator queryGenerator = new PatternQueryGenerator(rpst);

							queryGenerator.generateQueries();

							BPMNQueryEditor.this.page.getFeedbackPanel().success("Queries created!");
							target.add(BPMNQueryEditor.this.page.getFeedbackPanel());
						} catch (final QueryGenerationException e) {
							BPMNQueryEditor.this.page.getFeedbackPanel().error(
									"Query could not be create because:" + e.getMessage());
							target.add(BPMNQueryEditor.this.page.getFeedbackPanel());
						}

					}
				}
			}
		};
		this.layoutForm.add(this.createQueriesButton);
	}
}
