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
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
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
import de.hpi.unicorn.application.pages.eventrepository.model.ProcessInstanceFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.ProcessInstanceProvider;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * {@link Panel}, which shows the {@link CorrelationProcessInstance}s stored in
 * the database.
 */
public class ProcessInstancePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<IColumn<CorrelationProcessInstance, String>> columns;
	private final ProcessInstanceFilter processInstanceFilter;
	private final ProcessInstanceProvider processInstanceProvider;
	private final DefaultDataTable<CorrelationProcessInstance, String> dataTable;

	/**
	 * Constructor for the process instance panel. The page is initialized in
	 * this method and the data is loaded from the database.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public ProcessInstancePanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.processInstanceProvider = new ProcessInstanceProvider();
		this.processInstanceFilter = new ProcessInstanceFilter();
		this.processInstanceProvider.setProcessInstanceFilter(this.processInstanceFilter);

		final Form<Void> buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> processFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID",
				"Process", "Process (ID)" }));
		final String selectedEventCriteria = "ID";

		final DropDownChoice<String> eventTypeFilterCriteriaSelect = new DropDownChoice<String>(
				"processInstanceFilterCriteria", new Model<String>(selectedEventCriteria), processFilterCriteriaList);
		buttonForm.add(eventTypeFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>(
				"processInstanceFilterCondition", new Model<String>(selectedCondition), conditions);
		buttonForm.add(eventFilterConditionSelect);

		final TextField<String> searchValueInput = new TextField<String>("searchValueInput", Model.of(""));
		buttonForm.add(searchValueInput);

		final AjaxButton filterButton = new AjaxButton("filterButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				final String eventFilterCriteria = eventTypeFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(eventTypeFilterCriteriaSelect.getValue()));
				final String eventFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				ProcessInstancePanel.this.processInstanceProvider.setProcessInstanceFilter(new ProcessInstanceFilter(
						eventFilterCriteria, eventFilterCondition, filterValue));
				target.add(ProcessInstancePanel.this.dataTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new AjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				ProcessInstancePanel.this.processInstanceProvider.setProcessInstanceFilter(new ProcessInstanceFilter());
				target.add(ProcessInstancePanel.this.dataTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton deleteButton = new BlockingAjaxButton("deleteButton", buttonForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				ProcessInstancePanel.this.processInstanceProvider.deleteSelectedEntries();
				target.add(ProcessInstancePanel.this.dataTable);
			}
		};

		buttonForm.add(deleteButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				ProcessInstancePanel.this.processInstanceProvider.selectAllEntries();
				target.add(ProcessInstancePanel.this.dataTable);
			}
		};
		buttonForm.add(selectAllButton);

		this.add(buttonForm);

		this.columns = new ArrayList<IColumn<CorrelationProcessInstance, String>>();
		this.columns.add(new PropertyColumn<CorrelationProcessInstance, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<CorrelationProcessInstance, String>(Model.of("Process"), "process"));
		this.columns.add(new PropertyColumn<CorrelationProcessInstance, String>(Model.of("Correlation Attributes"),
				"correlationAttributesAndValues") {
			@Override
			public void populateItem(final Item<ICellPopulator<CorrelationProcessInstance>> item,
					final String componentId, final IModel<CorrelationProcessInstance> rowModel) {
				final String values = rowModel.getObject().getCorrelationAttributesAndValues().toString();
				final Label label = new Label(componentId, values.substring(1, values.length() - 1));
				item.add(label);
			}
		});
		// columns.add(new PropertyColumn<CorrelationProcessInstance, String>(
		// Model.of("Timer Event"), "timerEvent"));
		this.columns.add(new AbstractColumn<CorrelationProcessInstance, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((CorrelationProcessInstance) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId,
						ProcessInstancePanel.this.processInstanceProvider));
			}
		});

		this.dataTable = new DefaultDataTable<CorrelationProcessInstance, String>("processInstances", this.columns,
				new ProcessInstanceProvider(), 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);
	}
};
