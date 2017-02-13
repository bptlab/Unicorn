/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

import de.hpi.unicorn.application.components.form.BootstrapModal;
import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model.ProcessAnalysingTreeTableElement;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model.ProcessAnalysingTreeTableExpansionModel;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.modal.model.ProcessAnalysingTreeTableProvider;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.model.ProcessInstanceMonitoringTreeTableExpansionModel;
import de.hpi.unicorn.monitoring.bpmn.ProcessMonitor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This is a modal for displaying the analysis status for a
 * {@link CorrelationProcess}.
 * 
 * @author micha
 */
public class ProcessAnalysingModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private static final ResourceReference MODAL_SIZE_CSS = new PackageResourceReference(BootstrapModal.class,
			"modal_size.css");
	private ProcessMonitor processMonitor;
	private LabelTreeTable<ProcessAnalysingTreeTableElement, String> treeTable;
	private Form<Void> layoutForm;
	private final ProcessAnalysingTreeTableProvider treeTableProvider = new ProcessAnalysingTreeTableProvider(
			this.processMonitor);
	private Label processNameLabel;
	private String processName;

	@Override
	public void renderHead(final IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(ProcessAnalysingModal.MODAL_SIZE_CSS));
	}

	/**
	 * Constructor for a modal, which displays the analysis status for a
	 * {@link CorrelationProcess}.
	 * 
	 * @param id
	 */
	public ProcessAnalysingModal(final String id) {
		super(id, "Process Analysis");
		this.buildMainLayout();
	}

	private void buildMainLayout() {
		this.layoutForm = new Form<Void>("layoutForm");
		this.processNameLabel = new Label("processName", new PropertyModel<String>(this, "processName"));
		// processNameLabel = new Label("processName", Model.of(""));
		this.processNameLabel.setOutputMarkupId(true);
		this.layoutForm.add(this.processNameLabel);

		this.createTreeTable();
		this.add(this.layoutForm);
	}

	private void createTreeTable() {
		final List<IColumn<ProcessAnalysingTreeTableElement, String>> columns = this.createColumns();

		this.treeTable = new LabelTreeTable<ProcessAnalysingTreeTableElement, String>("processAnalysisTreeTable",
				columns, this.treeTableProvider, Integer.MAX_VALUE, new ProcessAnalysingTreeTableExpansionModel());

		this.treeTable.setOutputMarkupId(true);

		this.treeTable.getTable().addTopToolbar(
				new HeadersToolbar<String>(this.treeTable.getTable(), this.treeTableProvider));

		ProcessInstanceMonitoringTreeTableExpansionModel.get().expandAll();

		this.layoutForm.addOrReplace(this.treeTable);
	}

	private List<IColumn<ProcessAnalysingTreeTableElement, String>> createColumns() {
		final List<IColumn<ProcessAnalysingTreeTableElement, String>> columns = new ArrayList<IColumn<ProcessAnalysingTreeTableElement, String>>();

		columns.add(new TreeColumn<ProcessAnalysingTreeTableElement, String>(Model.of("Query"), "query"));
		columns.add(new PropertyColumn<ProcessAnalysingTreeTableElement, String>(Model.of("Monitored Elements"),
				"monitoredElements"));

		columns.add(new PropertyColumn<ProcessAnalysingTreeTableElement, String>(Model.of("Average Runtime"),
				"averageRuntime"));
		columns.add(new PropertyColumn<ProcessAnalysingTreeTableElement, String>(Model.of("Path frequency"),
				"pathFrequency"));

		return columns;
	}

	public void setProcessMonitor(final ProcessMonitor processMonitor, final AjaxRequestTarget target) {
		this.processMonitor = processMonitor;
		this.treeTableProvider.setProcessMonitor(processMonitor);
		this.refreshTreeTable(target);
		this.refreshLabel(processMonitor, target);
	}

	private void refreshLabel(final ProcessMonitor processMonitor, final AjaxRequestTarget target) {
		this.processName = (processMonitor != null) ? processMonitor.getProcess().getName() : "";
		target.add(this.processNameLabel);
	}

	public void refreshTreeTable(final AjaxRequestTarget target) {
		this.createTreeTable();
		target.add(this.treeTable);
	}

}
