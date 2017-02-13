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
import de.hpi.unicorn.application.pages.eventrepository.model.BPMNProcessFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.BPMNProcessProvider;
import de.hpi.unicorn.bpmn.element.BPMNProcess;

/**
 * {@link Panel}, which shows the {@link BPMNProcess}s stored in the database.
 */
public class BPMNProcessPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<IColumn<BPMNProcess, String>> columns;
	private final BPMNProcessFilter bpmnProcessFilter;
	private final BPMNProcessProvider bpmnProcessProvider;
	private final DefaultDataTable<BPMNProcess, String> dataTable;

	/**
	 * Constructor for the BPMN process panel. The page is initialized in this
	 * method and the data is loaded from the database.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public BPMNProcessPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.bpmnProcessProvider = new BPMNProcessProvider();
		this.bpmnProcessFilter = new BPMNProcessFilter();
		this.bpmnProcessProvider.setBPMNProcessFilter(this.bpmnProcessFilter);

		final Form<Void> buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> processFilterCriteriaList = new ArrayList<String>(
				Arrays.asList(new String[] { "ID", "Name" }));
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
				super.onSubmit(target, form);
				final String eventFilterCriteria = eventTypeFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(eventTypeFilterCriteriaSelect.getValue()));
				final String eventFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				BPMNProcessPanel.this.bpmnProcessProvider.setBPMNProcessFilter(new BPMNProcessFilter(
						eventFilterCriteria, eventFilterCondition, filterValue));
				target.add(BPMNProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new AjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				BPMNProcessPanel.this.bpmnProcessProvider.setBPMNProcessFilter(new BPMNProcessFilter());
				target.add(BPMNProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton deleteButton = new BlockingAjaxButton("deleteButton", buttonForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				;
				BPMNProcessPanel.this.bpmnProcessProvider.deleteSelectedEntries();
				target.add(BPMNProcessPanel.this.dataTable);
			}
		};

		buttonForm.add(deleteButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				BPMNProcessPanel.this.bpmnProcessProvider.selectAllEntries();
				target.add(BPMNProcessPanel.this.dataTable);
			}
		};
		buttonForm.add(selectAllButton);

		this.add(buttonForm);

		final Button createButton = new Button("linkButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				// setResponsePage(ProcessEditor.class);
			}
		};
		buttonForm.add(createButton);
		this.add(buttonForm);

		this.columns = new ArrayList<IColumn<BPMNProcess, String>>();
		this.columns.add(new PropertyColumn<BPMNProcess, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<BPMNProcess, String>(Model.of("Name"), "name"));
		this.columns.add(new AbstractColumn<BPMNProcess, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((BPMNProcess) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, BPMNProcessPanel.this.bpmnProcessProvider));
			}
		});

		this.dataTable = new DefaultDataTable<BPMNProcess, String>("processes", this.columns,
				new BPMNProcessProvider(), 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);
	}
};
