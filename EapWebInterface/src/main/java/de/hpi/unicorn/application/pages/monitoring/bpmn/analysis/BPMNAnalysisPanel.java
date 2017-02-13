/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.ProcessAnalysingModal;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.model.ProcessMonitoringFilter;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.model.ProcessMonitoringProvider;
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * This panel facilitates the analysis of finished
 * {@link CorrelationProcessInstance}s. The details for a single process
 * instance are visualized with a {@link ProcessAnalysingModal}.
 * 
 * @author micha
 */
@SuppressWarnings("serial")
public class BPMNAnalysisPanel extends Panel {

	private DefaultDataTable<ProcessMonitor, String> dataTable;
	private ProcessMonitoringProvider processMonitoringProvider;
	private ProcessMonitoringFilter processMonitoringFilter;
	private ProcessAnalysingModal processMonitorModal;

	/**
	 * Constructor for a panel, which facilitates the analysis of finished
	 * {@link CorrelationProcessInstance}s.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public BPMNAnalysisPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);
		this.createProcessInstanceMonitoringProvider();

		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.addProcessInstanceMonitorModal();

		this.addProcessTable();
	}

	private void addProcessTable() {
		this.dataTable = new DefaultDataTable<ProcessMonitor, String>("processAnalysisTable", this.createColumns(),
				this.processMonitoringProvider, 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);

	}

	private void addProcessInstanceMonitorModal() {
		this.processMonitorModal = new ProcessAnalysingModal("processMonitorModal");
		this.add(this.processMonitorModal);
	}

	private List<? extends IColumn<ProcessMonitor, String>> createColumns() {
		final ArrayList<IColumn<ProcessMonitor, String>> columns = new ArrayList<IColumn<ProcessMonitor, String>>();
		columns.add(new PropertyColumn<ProcessMonitor, String>(Model.of("ID"), "ID"));
		columns.add(new PropertyColumn<ProcessMonitor, String>(Model.of("Process"), "process"));
		columns.add(new PropertyColumn<ProcessMonitor, String>(Model.of("# of Process Instances"),
				"numberOfProcessInstances"));
		columns.add(new PropertyColumn<ProcessMonitor, String>(Model.of("Average runtime"), "averageRuntimeMillis"));

		// columns.add(new AbstractColumn(new Model("Status")) {
		// @Override
		// public void populateItem(Item cellItem, String componentId, IModel
		// rowModel) {
		// int entryId = ((ProcessInstanceMonitor)
		// rowModel.getObject()).getID();
		// cellItem.add(new ProcessInstanceMonitoringStatusPanel(componentId,
		// entryId, processInstanceMonitoringProvider));
		// }
		// });

		columns.add(new AbstractColumn(new Model("Status Details")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((ProcessMonitor) rowModel.getObject()).getID();
				cellItem.add(new ProcessMonitorEntryDetailsPanel(componentId, entryId,
						BPMNAnalysisPanel.this.processMonitoringProvider, BPMNAnalysisPanel.this.processMonitorModal));
			}
		});

		return columns;
	}

	private void createProcessInstanceMonitoringProvider() {
		this.processMonitoringProvider = new ProcessMonitoringProvider();
		this.processMonitoringFilter = new ProcessMonitoringFilter();
		this.processMonitoringProvider.setProcessMonitorFilter(this.processMonitoringFilter);
	}
}
