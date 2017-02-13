/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.export;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.resource.FileResourceStream;
import org.apache.wicket.util.resource.IResourceStream;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.BlockingForm;
import de.hpi.unicorn.application.components.table.SelectEntryPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.model.EventFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.EventProvider;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.importer.csv.CSVExporter;
import de.hpi.unicorn.importer.xml.XMLExporter;

/**
 * This class is a page to export {@link EapEvent}s as CSV files. It is possible
 * to specify, which events should be exported with a filter.
 * 
 * @author micha
 */
public class Export extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private String selectedEventTypeName;
	private EapEventType selectedEventType;
	private final Form<Void> layoutForm;
	protected String eventTypeNameFromTree;
	private DropDownChoice<String> eventTypeDropDownChoice;
	private ArrayList<IColumn<EapEvent, String>> columns;
	private DefaultDataTable<EapEvent, String> dataTable;
	private final EventProvider eventProvider;
	protected Export page;
	private DropDownChoice<String> eventFilterCriteriaSelect;
	private ArrayList<String> eventFilterCriteriaList;

	/**
	 * Constructor for a page to export {@link EapEvent}s as CSV files. It is
	 * possible to specify, which events should be exported with a filter.
	 */
	public Export() {
		super();

		this.page = this;
		this.layoutForm = new BlockingForm("layoutForm");
		this.add(this.layoutForm);

		this.eventProvider = new EventProvider();
		this.eventProvider.setEventFilter(new EventFilter("Event Type (ID)", "=", "-1"));

		this.addEventTypeSelect();
		this.addFilterTools();
		this.addExportButtons();
		this.addEventTable();
	}

	private void addEventTypeSelect() {
		final List<String> eventTypes = EapEventType.getAllTypeNames();
		this.eventTypeDropDownChoice = new DropDownChoice<String>("eventTypeDropDownChoice", new PropertyModel<String>(
				this, "selectedEventTypeName"), eventTypes);
		this.eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				Export.this.updateOnChangeOfDropDownChoice(target);
			}
		});
		this.layoutForm.add(this.eventTypeDropDownChoice);
	}

	private void addFilterTools() {

		this.eventFilterCriteriaSelect = new DropDownChoice<String>("eventFilterCriteria", new Model<String>(),
				new ArrayList<String>());
		this.eventFilterCriteriaSelect.setOutputMarkupId(true);
		this.layoutForm.add(this.eventFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>("eventFilterCondition",
				new Model<String>(selectedCondition), conditions);
		this.layoutForm.add(eventFilterConditionSelect);

		final TextField<String> searchValueInput = new TextField<String>("searchValueInput", Model.of(""));
		this.layoutForm.add(searchValueInput);

		final AjaxButton filterButton = new AjaxButton("filterButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String eventFilterCriteria = Export.this.eventFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(Export.this.eventFilterCriteriaSelect.getValue()));
				final String eventFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				Export.this.eventProvider.setSecondEventFilter(new EventFilter(eventFilterCriteria,
						eventFilterCondition, filterValue));
				target.add(Export.this.dataTable);
			}
		};
		this.layoutForm.add(filterButton);

		final AjaxButton resetButton = new BlockingAjaxButton("resetButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				;
				Export.this.eventProvider.setSecondEventFilter(new EventFilter());
				target.add(Export.this.dataTable);
			}
		};
		this.layoutForm.add(resetButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				Export.this.eventProvider.selectAllEntries();
				target.add(Export.this.dataTable);
			}
		};
		this.layoutForm.add(selectAllButton);
	}

	protected void updateOnChangeOfDropDownChoice(final AjaxRequestTarget target) {
		this.selectedEventType = EapEventType.findByTypeName(this.selectedEventTypeName);

		if (this.selectedEventType != null) {
			this.eventProvider.setEventFilter(new EventFilter("Event Type (ID)", "=", Integer
					.toString(this.selectedEventType.getID())));
			target.add(this.dataTable);
			this.eventFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID", "Event Type (ID)",
					"Process Instance" }));
			for (final String eventAttribute : this.selectedEventType.getEventAttributes()) {
				this.eventFilterCriteriaList.add(eventAttribute);
			}
			this.eventFilterCriteriaSelect.setChoices(this.eventFilterCriteriaList);
			target.add(this.eventFilterCriteriaSelect);
		}
	}

	private void addExportButtons() {

		final AjaxButton csvExportButton = new AjaxButton("csvExportButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				final CSVExporter csvExporter = new CSVExporter();
				final List<EapEvent> events = (Export.this.eventProvider.getSelectedEntities().isEmpty()) ? Export.this.eventProvider
						.getEntities() : Export.this.eventProvider.getSelectedEntities();
				if (events.isEmpty()) {
					Export.this.page.getFeedbackPanel().error("No events selected.");
					target.add(Export.this.page.getFeedbackPanel());
				} else if (events.get(0).isHierarchical()) {
					Export.this.page.getFeedbackPanel().error("Hierachical events cannot be exported as CSV file.");
					target.add(Export.this.page.getFeedbackPanel());
				} else {
					final AJAXDownload csvDownload = new AJAXDownload() {

						@Override
						protected IResourceStream getResourceStream() {
							final File csv = csvExporter.generateExportFile(Export.this.selectedEventType, events);
							return new FileResourceStream(new org.apache.wicket.util.file.File(csv));
						}

						@Override
						protected String getFileName() {
							return events.get(0).getEventType().getTypeName() + ".csv";
						}
					};
					Export.this.layoutForm.add(csvDownload);
					csvDownload.initiate(target);
					Export.this.page.getFeedbackPanel().success(
							events.size() + " events have been exported in a CSV file.");
					target.add(Export.this.page.getFeedbackPanel());
				}
			}
		};

		this.layoutForm.add(csvExportButton);

		final AjaxButton xmlExportButton = new AjaxButton("xmlExportButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final XMLExporter xmlExporter = new XMLExporter();
				final List<EapEvent> events = (Export.this.eventProvider.getSelectedEntities().isEmpty()) ? Export.this.eventProvider
						.getEntities() : Export.this.eventProvider.getSelectedEntities();
				if (events.isEmpty()) {
					Export.this.page.getFeedbackPanel().error("No events selected.");
					target.add(Export.this.page.getFeedbackPanel());
				} else if (events.size() == 1) {
					final AJAXDownload xmlDownload = new AJAXDownload() {

						@Override
						protected IResourceStream getResourceStream() {
							final File xml = xmlExporter.generateExportFile(events.get(0));
							return new FileResourceStream(new org.apache.wicket.util.file.File(xml));
						}

						@Override
						protected String getFileName() {
							return events.get(0).getEventType().getTypeName() + events.get(0).getID() + ".xml";
						}
					};
					Export.this.layoutForm.add(xmlDownload);
					xmlDownload.initiate(target);
					Export.this.page.getFeedbackPanel().success("One event has been exported as XML file.");
					target.add(Export.this.page.getFeedbackPanel());
				} else {
					final AJAXDownload zipDownload = new AJAXDownload() {

						@Override
						protected IResourceStream getResourceStream() {
							final File zip = xmlExporter.generateZipWithXMLFiles(events);
							return new FileResourceStream(new org.apache.wicket.util.file.File(zip));
						}

						@Override
						protected String getFileName() {
							return events.get(0).getEventType().getTypeName() + ".zip";
						}
					};
					Export.this.layoutForm.add(zipDownload);
					zipDownload.initiate(target);
					Export.this.page.getFeedbackPanel().success(
							events.size() + " events have been exported as XML files in a ZIP archive.");
					target.add(Export.this.page.getFeedbackPanel());
				}
			}
		};
		this.layoutForm.add(xmlExportButton);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void addEventTable() {
		this.columns = new ArrayList<IColumn<EapEvent, String>>();
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("Timestamp"), "timestamp"));
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("EventType"), "eventType"));
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("Values"), "values"));
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("Process Instances"), "processInstances"));

		this.columns.add(new AbstractColumn<EapEvent, String>(new Model("Select")) {
			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((EapEvent) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, Export.this.eventProvider));
			}
		});

		this.dataTable = new DefaultDataTable<EapEvent, String>("events", this.columns, this.eventProvider, 20);
		this.dataTable.setOutputMarkupId(true);
		this.add(this.dataTable);
	}
}
