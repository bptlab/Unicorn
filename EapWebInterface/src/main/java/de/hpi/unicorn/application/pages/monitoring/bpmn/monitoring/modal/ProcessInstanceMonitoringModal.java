/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.modal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.hpi.unicorn.application.components.form.BootstrapModal;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.QueryMonitoringStatusPanel;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.QueryViolationMonitoringStatusPanel;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableElement;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableExpansionModel;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableProvider;
import de.hpi.unicorn.monitoring.bpmn.ProcessInstanceMonitor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This is a modal for displaying the monitoring status for a
 * {@link CorrelationProcess}.
 * 
 * @author micha
 */
public class ProcessInstanceMonitoringModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private static final ResourceReference MODAL_SIZE_CSS = new PackageResourceReference(BootstrapModal.class,
			"modal_size.css");
	private ProcessInstanceMonitor processInstanceMonitor;
	private Form<Void> layoutForm;
	private LabelTreeTable<ProcessInstanceMonitoringTreeTableElement, String> treeTable;
	private final ProcessInstanceMonitoringTreeTableProvider treeTableProvider = new ProcessInstanceMonitoringTreeTableProvider(
			this.processInstanceMonitor);

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(ProcessInstanceMonitoringModal.MODAL_SIZE_CSS));
	}

	/**
	 * Constructor for a modal, which displays the monitoring status for a
	 * {@link CorrelationProcess}.
	 * 
	 * @param id
	 */
	public ProcessInstanceMonitoringModal(final String id) {
		super(id, "Process Instance Monitoring");
		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.layoutForm = new Form<Void>("layoutForm");

		// Add componentTree
		this.createTreeTable();

		this.add(this.layoutForm);
	}

	private void createTreeTable() {
		final List<IColumn<ProcessInstanceMonitoringTreeTableElement, String>> columns = this.createColumns();

		this.treeTable = new LabelTreeTable<ProcessInstanceMonitoringTreeTableElement, String>(
				"processInstanceMonitoringTreeTable", columns, this.treeTableProvider, Integer.MAX_VALUE,
				new ProcessInstanceMonitoringTreeTableExpansionModel());

		this.treeTable.setOutputMarkupId(true);

		this.treeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.treeTable.getTable(), this.treeTableProvider));

		ProcessInstanceMonitoringTreeTableExpansionModel.get().expandAll();

		this.layoutForm.addOrReplace(this.treeTable);
	}

	private List<IColumn<ProcessInstanceMonitoringTreeTableElement, String>> createColumns() {
		final List<IColumn<ProcessInstanceMonitoringTreeTableElement, String>> columns = new ArrayList<IColumn<ProcessInstanceMonitoringTreeTableElement, String>>();

		columns.add(new TreeColumn<ProcessInstanceMonitoringTreeTableElement, String>(Model.of("Query"), "query"));
		columns.add(new PropertyColumn<ProcessInstanceMonitoringTreeTableElement, String>(Model
				.of("Monitored Elements"), "monitoredElements"));

		columns.add(new AbstractColumn(new Model("Status")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((ProcessInstanceMonitoringTreeTableElement) rowModel.getObject()).getID();
				cellItem.add(new QueryMonitoringStatusPanel(componentId, entryId,
						ProcessInstanceMonitoringModal.this.treeTableProvider));
			}
		});

		columns.add(new AbstractColumn(new Model("Violation Status")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((ProcessInstanceMonitoringTreeTableElement) rowModel.getObject()).getID();
				cellItem.add(new QueryViolationMonitoringStatusPanel(componentId, entryId,
						ProcessInstanceMonitoringModal.this.treeTableProvider));
			}
		});

		columns.add(new PropertyColumn<ProcessInstanceMonitoringTreeTableElement, String>(Model.of("Start Time"),
				"startTime"));
		columns.add(new PropertyColumn<ProcessInstanceMonitoringTreeTableElement, String>(Model.of("End Time"),
				"endTime"));

		return columns;
	}

	public void setProcessInstanceMonitor(final ProcessInstanceMonitor processInstanceMonitor,
			final AjaxRequestTarget target) {
		this.processInstanceMonitor = processInstanceMonitor;
		this.treeTableProvider.setProcessInstanceMonitor(processInstanceMonitor);
		this.refreshTreeTable(target);
	}

	public void refreshTreeTable(final AjaxRequestTarget target) {
		this.createTreeTable();
		target.add(this.treeTable);
	}
}
