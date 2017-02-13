/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.modal.ProcessInstanceMonitoringModal;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringFilter;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringProvider;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;
import de.hpi.unicorn.process.CorrelationProcess;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This panel facilitates the monitoring of running
 * {@link CorrelationProcessInstance}s. The details for a single process
 * instance are visualized with a {@link ProcessInstanceMonitoringStatusPanel}.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class BPMNMonitoringPanel extends Panel {

	private ArrayList<String> processNameList;
	private DropDownChoice<String> processSelect;
	private DefaultDataTable<ProcessInstanceMonitor, String> dataTable;
	private ProcessInstanceMonitoringProvider processInstanceMonitoringProvider;
	private ProcessInstanceMonitoringFilter processInstanceMonitoringFilter;
	private CorrelationProcess process;
	private ProcessInstanceMonitoringModal processInstanceMonitorModal;

	/**
	 * Constructor for a panel, which facilitates the monitoring of running
	 * {@link CorrelationProcessInstance}s.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public BPMNMonitoringPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);
		this.createProcessInstanceMonitoringProvider();

		this.buildMainLayout();
	}

	private void buildMainLayout() {
		final Form<Void> layoutForm = new Form<Void>("layoutForm");
		this.add(layoutForm);
		this.addProcessSelect(layoutForm);

		this.addProcessInstanceMonitorModal(layoutForm);

		this.addProcessInstanceTable(layoutForm);
	}

	private void addProcessInstanceTable(final Form<Void> layoutForm) {
		this.dataTable = new DefaultDataTable<ProcessInstanceMonitor, String>("processInstancesMonitoringTable",
				this.createColumns(), this.processInstanceMonitoringProvider, 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);

	}

	private void addProcessInstanceMonitorModal(final Form<Void> layoutForm) {
		this.processInstanceMonitorModal = new ProcessInstanceMonitoringModal("processInstanceMonitorModal");
		this.add(this.processInstanceMonitorModal);
	}

	private List<? extends IColumn<ProcessInstanceMonitor, String>> createColumns() {
		final ArrayList<IColumn<ProcessInstanceMonitor, String>> columns = new ArrayList<IColumn<ProcessInstanceMonitor, String>>();
		columns.add(new PropertyColumn<ProcessInstanceMonitor, String>(Model.of("ID"), "ID"));
		columns.add(new PropertyColumn<ProcessInstanceMonitor, String>(Model.of("ProcessInstance"), "processInstance"));
		// columns.add(new PropertyColumn<ProcessInstanceMonitor,
		// String>(Model.of("Status"), "status"));

		columns.add(new AbstractColumn(new Model("Status")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((ProcessInstanceMonitor) rowModel.getObject()).getID();
				cellItem.add(new ProcessInstanceMonitoringStatusPanel(componentId, entryId,
						BPMNMonitoringPanel.this.processInstanceMonitoringProvider));
			}
		});

		columns.add(new AbstractColumn(new Model("Status Details")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((ProcessInstanceMonitor) rowModel.getObject()).getID();
				cellItem.add(new ProcessInstanceMonitorEntryDetailsPanel(componentId, entryId,
						BPMNMonitoringPanel.this.processInstanceMonitoringProvider,
						BPMNMonitoringPanel.this.processInstanceMonitorModal));
			}
		});

		return columns;
	}

	private void addProcessSelect(final Form<Void> layoutForm) {
		this.processNameList = new ArrayList<String>();
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNameList.add(process.getName());
		}

		this.processSelect = new DropDownChoice<String>("processSelect", new Model<String>(), this.processNameList);
		this.processSelect.setOutputMarkupId(true);
		this.processSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				BPMNMonitoringPanel.this.process = CorrelationProcess.findByName(
						BPMNMonitoringPanel.this.processSelect.getChoices().get(
								Integer.parseInt(BPMNMonitoringPanel.this.processSelect.getValue()))).get(0);
				BPMNMonitoringPanel.this.createProcessInstanceMonitoringProvider();
				target.add(BPMNMonitoringPanel.this.dataTable);
			}

		});

		layoutForm.add(this.processSelect);
	}

	private void createProcessInstanceMonitoringProvider() {
		this.processInstanceMonitoringProvider = new ProcessInstanceMonitoringProvider(this.process);
		this.processInstanceMonitoringFilter = new ProcessInstanceMonitoringFilter();
		this.processInstanceMonitoringProvider.setProcessInstanceMonitorFilter(this.processInstanceMonitoringFilter);
	}
}
