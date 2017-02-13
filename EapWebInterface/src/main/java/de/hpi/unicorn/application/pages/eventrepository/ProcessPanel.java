/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
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
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.model.ProcessFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.ProcessProvider;
import de.hpi.unicorn.application.pages.eventrepository.processeditor.ProcessEditor;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * {@link Panel}, which shows the {@link CorrelationProcess}es stored in the
 * database.
 */
public class ProcessPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<IColumn<CorrelationProcess, String>> columns;
	private final ProcessFilter processFilter;
	private final ProcessProvider processProvider;
	private final DefaultDataTable<CorrelationProcess, String> dataTable;

	/**
	 * Constructor for the process panel. The page is initialized in this method
	 * and the data is loaded from the database.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public ProcessPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.processProvider = new ProcessProvider();
		this.processFilter = new ProcessFilter();
		this.processProvider.setProcessFilter(this.processFilter);

		final Form<Void> buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> processFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID", "Name",
				"Process Instance", "Correlation Attribute" }));
		final String selectedEventCriteria = "ID";

		final DropDownChoice<String> eventTypeFilterCriteriaSelect = new DropDownChoice<String>(
				"processFilterCriteria", new Model<String>(selectedEventCriteria), processFilterCriteriaList);
		buttonForm.add(eventTypeFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>("processFilterCondition",
				new Model<String>(selectedCondition), conditions);
		buttonForm.add(eventFilterConditionSelect);

		final TextField<String> searchValueInput = new TextField<String>("searchValueInput", Model.of(""));
		buttonForm.add(searchValueInput);

		final AjaxButton filterButton = new AjaxButton("filterButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String eventFilterCriteria = eventTypeFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(eventTypeFilterCriteriaSelect.getValue()));
				final String eventFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				ProcessPanel.this.processProvider.setProcessFilter(new ProcessFilter(eventFilterCriteria,
						eventFilterCondition, filterValue));
				target.add(ProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new AjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				ProcessPanel.this.processProvider.setProcessFilter(new ProcessFilter());
				target.add(ProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton deleteButton = new BlockingAjaxButton("deleteButton", buttonForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				ProcessPanel.this.processProvider.deleteSelectedEntries();
				target.add(ProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(deleteButton);
		this.add(buttonForm);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				;
				ProcessPanel.this.processProvider.selectAllEntries();
				target.add(ProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(selectAllButton);

		final Button createButton = new Button("createButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				this.setResponsePage(ProcessEditor.class);
			}
		};
		buttonForm.add(createButton);
		this.add(buttonForm);

		this.columns = new ArrayList<IColumn<CorrelationProcess, String>>();
		this.columns.add(new PropertyColumn<CorrelationProcess, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<CorrelationProcess, String>(Model.of("Name"), "name"));
		this.columns.add(new AbstractColumn<CorrelationProcess, String>(new Model("Correlation Condition")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				String correlationCondition = new String();
				final CorrelationProcess process = ((CorrelationProcess) rowModel.getObject());
				if (process.getCorrelationAttributes() != null && !process.getCorrelationAttributes().isEmpty()) {
					correlationCondition = "Attribute correlation with event types: " + process.getEventTypes()
							+ " and common attributes: " + process.getCorrelationAttributes();
				} else if (process.getCorrelationRules() != null && !process.getCorrelationRules().isEmpty()) {
					correlationCondition = "Rule correlation: " + process.getCorrelationRules();
				}
				cellItem.add(new Label(componentId, correlationCondition));
			}
		});
		this.columns.add(new PropertyColumn<CorrelationProcess, String>(Model.of("Process Instances"),
				"processInstances"));
		this.columns.add(new AbstractColumn<CorrelationProcess, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((CorrelationProcess) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, ProcessPanel.this.processProvider));
			}
		});

		this.dataTable = new DefaultDataTable<CorrelationProcess, String>("processes", this.columns,
				new ProcessProvider(), 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);
	}
};
