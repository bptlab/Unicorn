/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.DataTableButtonPanel;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.table.SelectEntryPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor.EventTypeEditor;
import de.hpi.unicorn.application.pages.eventrepository.model.EventTypeFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.EventTypeProvider;
import de.hpi.unicorn.application.pages.export.AJAXDownload;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.TempFolderUtil;

/**
 * {@link Panel}, which shows the {@link EapEventType}s stored in the database.
 */
public class EventTypePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private List<IColumn<EapEventType, String>> columns;
	private EventTypeFilter eventTypeFilter;
	private EventTypeProvider eventTypeProvider;
	private DefaultDataTable<EapEventType, String> dataTable;

	/**
	 * Constructor for the event type panel. The page is initialized in this
	 * method and the data is loaded from the database.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	public EventTypePanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);

		this.eventTypeProvider = new EventTypeProvider();
		this.eventTypeFilter = new EventTypeFilter();
		this.eventTypeProvider.setEventTypeFilter(this.eventTypeFilter);

		final WarnOnExitForm buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> eventTypeFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID",
				"Name" }));
		final String selectedEventCriteria = "ID";

		final DropDownChoice<String> eventTypeFilterCriteriaSelect = new DropDownChoice<String>(
				"eventTypeFilterCriteria", new Model<String>(selectedEventCriteria), eventTypeFilterCriteriaList);
		buttonForm.add(eventTypeFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>(
				"eventTypeFilterCondition", new Model<String>(selectedCondition), conditions);
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
				EventTypePanel.this.eventTypeProvider.setEventTypeFilter(new EventTypeFilter(eventFilterCriteria,
						eventFilterCondition, filterValue));
				target.add(EventTypePanel.this.dataTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new AjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				EventTypePanel.this.eventTypeProvider.setEventTypeFilter(new EventTypeFilter());
				target.add(EventTypePanel.this.dataTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton deleteButton = new BlockingAjaxButton("deleteButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				EventTypePanel.this.eventTypeProvider.deleteSelectedEntries();
				target.add(EventTypePanel.this.dataTable);
			}
		};
		buttonForm.add(deleteButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				EventTypePanel.this.eventTypeProvider.selectAllEntries();
				target.add(EventTypePanel.this.dataTable);
			}
		};
		buttonForm.add(selectAllButton);

		final Button createButton = new AjaxButton("createButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				this.setResponsePage(EventTypeEditor.class);
			}
		};
		buttonForm.add(createButton);
		this.add(buttonForm);

		this.columns = new ArrayList<IColumn<EapEventType, String>>();
		this.columns.add(new PropertyColumn<EapEventType, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<EapEventType, String>(Model.of("Name"), "typeName"));
		this.columns.add(new PropertyColumn<EapEventType, String>(Model.of("Timestamp"), "timestampName"));
		this.columns.add(new PropertyColumn<EapEventType, String>(Model.of("Attributes"), "valueTypes"));
		this.columns.add(new AbstractColumn<EapEventType, String>(new Model("Export")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final EapEventType eventType = ((EapEventType) rowModel.getObject());
				if (eventType.getXsdString() != null && !eventType.getXsdString().isEmpty()) {
					final AjaxButton exportButton = new AjaxButton("button", buttonForm) {
						private static final long serialVersionUID = 1L;

						@Override
						public void onSubmit(final AjaxRequestTarget target, final Form form) {
							final AJAXDownload xsdDownload = new AJAXDownload() {

								@Override
								protected IResourceStream getResourceStream() {
									final File file = new File(TempFolderUtil.getFolder()
											+ System.getProperty("file.separator") + eventType.getTypeName() + ".xsd");
									try {
										final FileWriter writer = new FileWriter(file, false);
										writer.write(eventType.getXsdString());
										writer.flush();
										writer.close();
									} catch (final IOException e1) {
										e1.printStackTrace();
									}
									return new FileResourceStream(new org.apache.wicket.util.file.File(file));
								}

								@Override
								protected String getFileName() {
									return eventType.getTypeName() + ".xsd";
								}
							};
							buttonForm.add(xsdDownload);
							xsdDownload.initiate(target);
						}
					};

					WebMarkupContainer buttonPanel = new WebMarkupContainer(componentId);
					try {
						buttonPanel = new DataTableButtonPanel(componentId, exportButton);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					buttonPanel.setOutputMarkupId(true);
					cellItem.add(buttonPanel);
				} else {
					cellItem.add(new Label(componentId, "n/a"));
				}
			}
		});
		this.columns.add(new AbstractColumn<EapEventType, String>(new Model("Select")) {

			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((EapEventType) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, EventTypePanel.this.eventTypeProvider));
			}
		});

		this.dataTable = new DefaultDataTable<EapEventType, String>("eventtypes", this.columns,
				new EventTypeProvider(), 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);
	}
}
