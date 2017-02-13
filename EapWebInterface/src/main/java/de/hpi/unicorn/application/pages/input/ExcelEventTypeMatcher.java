/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.csv.CSVImporter;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.importer.excel.FileNormalizer;
import de.hpi.unicorn.importer.excel.ImportEvent;

public class ExcelEventTypeMatcher extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	private List<String> columnTitles = new ArrayList<String>();
	private FileNormalizer fileNormalizer;
	private final String filePath;
	private final List<Map<String, String>> tableRows = new ArrayList<Map<String, String>>();
	private List<String> selectedColumnTitles = new ArrayList<String>();
	private List<EapEventType> selectedEventTypes;
	private ListView<List<String>> headerContainer;
	private ListView<Map<String, String>> rowContainer;
	private WebMarkupContainer tableContainer;
	private CheckBoxMultipleChoice<EapEventType> existingTypesCheckBoxMultipleChoice;
	private DropDownChoice<EapEventType> eventTypeForPreviewDropDownChoice;
	private EapEventType eventTypeForPreview;
	private Form<Void> layoutForm;

	public ExcelEventTypeMatcher(final PageParameters parameters) {

		this.filePath = parameters.get("filePath").toString();
		final int index = this.filePath.lastIndexOf('.');
		final String fileExtension = this.filePath.substring(index + 1, this.filePath.length());
		if (fileExtension.contains("xls")) {
			this.fileNormalizer = new ExcelImporter();
		} else {
			this.fileNormalizer = new CSVImporter();
		}
		this.columnTitles = this.fileNormalizer.getColumnTitlesFromFile(this.filePath);
		if (!FileUploader.noEventTypesFound(parameters)) {
			this.buildMainLayout();
		}
	}

	private void buildMainLayout() {

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);
		// find matching event types
		final String importTimeName = ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME;
		final List<EapEventType> eventTypes = EapEventType.findMatchingEventTypesForNonHierarchicalAttributes(
				this.columnTitles, importTimeName);

		if (!eventTypes.isEmpty()) {
			this.selectedEventTypes = new ArrayList<EapEventType>(Arrays.asList(eventTypes.get(0)));
			this.eventTypeForPreview = this.selectedEventTypes.get(0);
			this.selectedColumnTitles = this.eventTypeForPreview.getNonHierarchicalAttributeExpressions();
		}

		this.configurePreviewTable();

		final Button openExcelEventTypeCreatorButton = new Button("openExcelEventTypeCreatorButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				final PageParameters pageParameters = new PageParameters();
				pageParameters.add("filePath", ExcelEventTypeMatcher.this.filePath);
				this.setResponsePage(ExcelEventTypeCreator.class, pageParameters);
			}
		};

		this.layoutForm.add(openExcelEventTypeCreatorButton);

		this.existingTypesCheckBoxMultipleChoice = new CheckBoxMultipleChoice<EapEventType>(
				"existingTypesCheckBoxMultipleChoice", new PropertyModel<ArrayList<EapEventType>>(this,
						"selectedEventTypes"), eventTypes);
		this.existingTypesCheckBoxMultipleChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				target.add(ExcelEventTypeMatcher.this.eventTypeForPreviewDropDownChoice);
				if (!ExcelEventTypeMatcher.this.selectedEventTypes.isEmpty()) {
					ExcelEventTypeMatcher.this.eventTypeForPreview = ExcelEventTypeMatcher.this.selectedEventTypes
							.get(0);
					ExcelEventTypeMatcher.this.updatePreviewTable(target);
				}
			}
		});

		this.layoutForm.add(this.existingTypesCheckBoxMultipleChoice);

		this.eventTypeForPreviewDropDownChoice = new DropDownChoice<EapEventType>("eventTypeForPreviewDropDownChoice",
				new PropertyModel<EapEventType>(this, "eventTypeForPreview"), this.selectedEventTypes);

		this.eventTypeForPreviewDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ExcelEventTypeMatcher.this.updatePreviewTable(target);
			}
		});

		this.eventTypeForPreviewDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.eventTypeForPreviewDropDownChoice);

		final BlockingAjaxButton addToEventTypeButton = new BlockingAjaxButton("addToEventTypeButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				if (ExcelEventTypeMatcher.this.selectedEventTypes.isEmpty()) {
					ExcelEventTypeMatcher.this.getFeedbackPanel().error("Please select at least one event type!");
				} else {
					int eventsCount = 0;
					for (final EapEventType selectedEventType : ExcelEventTypeMatcher.this.selectedEventTypes) {
						final EapEventType eventType = selectedEventType;
						final String timestamp = eventType.getTimestampName();
						final List<EapEvent> events = ExcelEventTypeMatcher.this.fileNormalizer.importEventsFromFile(
								ExcelEventTypeMatcher.this.filePath, eventType.getRootLevelValueTypes(), timestamp);
						for (final EapEvent event : events) {
							event.setEventType(selectedEventType);
						}
						Broker.getEventImporter().importEvents(events);
						eventsCount = events.size();
					}
					final String selectedEventTypesString = ExcelEventTypeMatcher.this.selectedEventTypes.toString()
							.substring(1, ExcelEventTypeMatcher.this.selectedEventTypes.toString().length() - 1);
					final PageParameters pageParameters = new PageParameters();
					pageParameters.add("successFeedback", eventsCount + " events have been added to "
							+ selectedEventTypesString);
					this.setResponsePage(MainPage.class, pageParameters);
				}
			}
		};

		this.layoutForm.add(addToEventTypeButton);
	}

	private void updatePreviewTable(final AjaxRequestTarget target) {
		this.selectedColumnTitles = this.eventTypeForPreview.getNonHierarchicalAttributeExpressions();
		target.add(this.tableContainer);
	}

	private void configurePreviewTable() {

		final List<ImportEvent> events = this.fileNormalizer.importEventsForPreviewFromFile(this.filePath,
				this.columnTitles);

		for (final ImportEvent event : events) {
			final Map<String, String> eventValues = new HashMap<String, String>();
			if (event.getTimestamp() != null) {
				// TODO: another name for key required!!!
				eventValues.put(event.getExtractedTimestampName(), event.getTimestamp().toString());
			}
			final Set<String> attributeNames = event.getValues().keySet();
			for (final String attributeName : attributeNames) {
				final String attributeValue = event.getValues().get(attributeName).toString();
				eventValues.put(attributeName, attributeValue);
			}
			eventValues.put(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME, event.getImportTime().toString());
			this.tableRows.add(eventValues);
		}

		this.tableContainer = new WebMarkupContainer("tableContainer");
		this.tableContainer.setOutputMarkupId(true);

		final List<String> allColumnTitles = new ArrayList<String>(this.columnTitles);
		allColumnTitles.add(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME);
		final List<List<String>> headers = new ArrayList<List<String>>();
		headers.add(allColumnTitles);

		this.headerContainer = new ListView<List<String>>("headerContainer", headers) {
			private static final long serialVersionUID = 3658948592812295572L;

			@Override
			protected void populateItem(final ListItem<List<String>> item) {
				item.add(new ListView<String>("column", ExcelEventTypeMatcher.this.selectedColumnTitles) {
					private static final long serialVersionUID = -3627947713326647386L;

					@Override
					protected void populateItem(final ListItem<String> item) {
						item.add(new Label("cell", item.getModelObject()));
					}

				});
			}
		};
		this.headerContainer.setOutputMarkupId(true);

		this.rowContainer = new ListView<Map<String, String>>("rowContainer", this.tableRows) {
			private static final long serialVersionUID = -3353890746328461012L;

			@Override
			protected void populateItem(final ListItem<Map<String, String>> item) {
				final Map<String, String> row = item.getModelObject();
				item.add(new ListView<String>("column", ExcelEventTypeMatcher.this.selectedColumnTitles) {
					private static final long serialVersionUID = -6270375159398080371L;
					int i = 0;

					@Override
					protected void populateItem(final ListItem<String> item) {
						item.add(new Label("cell", row.get(ExcelEventTypeMatcher.this.selectedColumnTitles
								.get(this.i++))));

					}
				});
			}
		};
		this.rowContainer.setOutputMarkupId(true);

		this.tableContainer.add(this.headerContainer);
		this.tableContainer.add(this.rowContainer);
		this.layoutForm.add(this.tableContainer);
	}
}
