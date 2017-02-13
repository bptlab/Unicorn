/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.table.AttributeTypeCheckBoxPanel;
import de.hpi.unicorn.application.components.table.AttributeTypeDropDownChoicePanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.input.model.EventAttributeProvider;
import de.hpi.unicorn.application.pages.main.MainPage;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.AttributeTypeTree;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.FileUtils;
import de.hpi.unicorn.importer.csv.CSVImporter;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.importer.excel.FileNormalizer;
import de.hpi.unicorn.importer.excel.ImportEvent;
import de.hpi.unicorn.importer.excel.TimeStampNames;
import de.hpi.unicorn.importer.xml.AbstractXMLParser;
import de.hpi.unicorn.utils.DateUtils;

public class ExcelEventTypeCreator extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	public static String GENERATED_TIMESTAMP_COLUMN_NAME = AbstractXMLParser.GENERATED_TIMESTAMP_COLUMN_NAME;
	private List<IColumn<TypeTreeNode, String>> attributeTableColumns;
	private DefaultDataTable<TypeTreeNode, String> attributeTable;
	private EventAttributeProvider eventAttributeProvider;
	private ArrayList<String> columnTitles = new ArrayList<String>();
	private FileNormalizer fileNormalizer;
	private final String filePath;
	private final List<Map<String, String>> tableRows = new ArrayList<Map<String, String>>();
	private DropDownChoice<String> timestampDropDownChoice;
	private ListView<List<String>> headerContainer;
	private ListView<Map<String, String>> rowContainer;
	private WebMarkupContainer tableContainer;
	private final ExcelEventTypeCreator excelEventTypeCreator;
	private Form<Void> layoutForm;
	private TextField<String> eventTypeNameInput;
	private String eventTypeName;
	private AttributeTypeTree eventTypeAttributesTree;
	private TypeTreeNode timestampAttribute;

	private AjaxCheckBox importTimeCheckBox;
	private boolean eventTypeUsingImportTime = false;

	private String timestampName;

	private List<ImportEvent> events;

	public ExcelEventTypeCreator(final PageParameters parameters) {

		super();
		this.excelEventTypeCreator = this;
		this.filePath = parameters.get("filePath").toString();
		this.eventTypeName = FileUtils.getFileNameWithoutExtension(this.filePath);
		final int index = this.filePath.lastIndexOf('.');
		final String fileExtension = this.filePath.substring(index + 1, this.filePath.length());
		if (fileExtension.contains("xls")) {
			this.fileNormalizer = new ExcelImporter();
		} else {
			this.fileNormalizer = new CSVImporter();
		}
		this.columnTitles = (ArrayList<String>) this.fileNormalizer.getColumnTitlesFromFile(this.filePath);

		this.buildMainlayout();
	}

	private void buildMainlayout() {
		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		final List<PropertyColumn> columns = new ArrayList<PropertyColumn>();
		for (final String title : this.columnTitles) {
			columns.add(new PropertyColumn(new Model(title), title));
		}
		columns.add(new PropertyColumn(new Model(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME),
				ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME));

		// text field with event type name
		this.eventTypeNameInput = new TextField<String>("eventTypeNameInput", new PropertyModel<String>(this,
				"eventTypeName"));
		this.eventTypeNameInput.setOutputMarkupId(true);
		this.layoutForm.add(this.eventTypeNameInput);

		// render table with events and values
		this.configurePreviewTable();

		// only Date attributes added to timestamp dropdown choice (~=
		// attributeExpressions)
		final Map<String, Serializable> eventValues = this.events.get(0).getValues();
		final List<String> attributeExpressions = new ArrayList<String>(eventValues.keySet());
		final List<String> dateExpressions = new ArrayList<String>();
		final List<TypeTreeNode> attributes = new ArrayList<TypeTreeNode>();
		for (final String attributeExpression : attributeExpressions) {
			final Serializable value = eventValues.get(attributeExpression);
			if (DateUtils.isDate(value.toString())) {
				attributes.add(new TypeTreeNode(attributeExpression, AttributeTypeEnum.DATE));
				dateExpressions.add(attributeExpression);
			} else if (value.toString().matches("[0-9]*")) {
				attributes.add(new TypeTreeNode(attributeExpression, AttributeTypeEnum.INTEGER));
			} else if (value.toString().matches("[-+]?[0-9]*\\.?[0-9]+$")) {
				attributes.add(new TypeTreeNode(attributeExpression, AttributeTypeEnum.FLOAT));
			} else {
				attributes.add(new TypeTreeNode(attributeExpression, AttributeTypeEnum.STRING));
			}
		}
		final String extractedTimestampName = this.events.get(0).getExtractedTimestampName();
		if (extractedTimestampName != null) {
			dateExpressions.add(extractedTimestampName);
			attributes.add(new TypeTreeNode(extractedTimestampName, AttributeTypeEnum.DATE));
		}

		// initialize attributes (value types) tree of event type
		this.eventTypeAttributesTree = new AttributeTypeTree(attributes);
		// initialize provider for event preview table
		this.eventAttributeProvider = new EventAttributeProvider(attributes, this.timestampName);

		// if there is no attribute that could be used as timestamp
		if (attributeExpressions.isEmpty()) {
			// use import time as timestamp
			this.timestampName = ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME;
			this.eventTypeUsingImportTime = true;
		} else {
			// else pre-select a timestamp in the dropdown choice
			this.timestampName = this.setTimestampPreselection(attributes);
			if (this.timestampName == null) {
				for (final TypeTreeNode attribute : this.eventTypeAttributesTree.getAttributes()) {
					if (attribute.getType() == AttributeTypeEnum.DATE) {
						this.timestampName = attribute.getAttributeExpression();
						break;
					}
				}
			}
			if (this.timestampName == null) {
				this.timestampName = ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME;
				this.eventTypeUsingImportTime = true;
			} else {
				this.timestampAttribute = this.eventTypeAttributesTree.getAttributeByExpression(this.timestampName);
				this.eventAttributeProvider.deselectEntry(this.timestampAttribute);
			}
		}
		this.eventAttributeProvider.setTimestampName(this.timestampName);

		this.importTimeCheckBox = new AjaxCheckBox("importTimeCheckBox", new PropertyModel<Boolean>(this,
				"eventTypeUsingImportTime")) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return !dateExpressions.isEmpty();
			}

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (ExcelEventTypeCreator.this.eventTypeUsingImportTime) {
					ExcelEventTypeCreator.this.timestampName = ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME;
				} else {
					ExcelEventTypeCreator.this.timestampName = ExcelEventTypeCreator.this.timestampAttribute.getName();
				}
				// disable timestamp dropdown choice if check box is checked
				ExcelEventTypeCreator.this.timestampDropDownChoice
						.setEnabled(!ExcelEventTypeCreator.this.eventTypeUsingImportTime);
				ExcelEventTypeCreator.this.eventAttributeProvider
						.setTimestampName(ExcelEventTypeCreator.this.timestampName);
				target.add(ExcelEventTypeCreator.this.timestampDropDownChoice);
				target.add(ExcelEventTypeCreator.this.attributeTable);
				target.add(ExcelEventTypeCreator.this.tableContainer);
			}
		};

		this.timestampDropDownChoice = new DropDownChoice<String>("timestampDropDownChoice", new PropertyModel<String>(
				this, "timestampName"), dateExpressions) {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled() {
				return !ExcelEventTypeCreator.this.eventTypeUsingImportTime;
			}
		};
		this.timestampDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				ExcelEventTypeCreator.this.timestampAttribute = ExcelEventTypeCreator.this.eventTypeAttributesTree
						.getAttributeByExpression(ExcelEventTypeCreator.this.timestampName);
				ExcelEventTypeCreator.this.eventAttributeProvider
						.setTimestampName(ExcelEventTypeCreator.this.timestampName);
				target.add(ExcelEventTypeCreator.this.attributeTable);
				target.add(ExcelEventTypeCreator.this.tableContainer);
			}
		});

		// disable components if there is no attribute that could be used as
		// timestamp
		if (attributeExpressions.isEmpty()) {
			this.importTimeCheckBox.setEnabled(false);
			this.timestampDropDownChoice.setEnabled(false);
		}
		this.importTimeCheckBox.setOutputMarkupId(true);
		this.timestampDropDownChoice.setOutputMarkupId(true);
		this.layoutForm.add(this.importTimeCheckBox);
		this.layoutForm.add(this.timestampDropDownChoice);

		this.attributeTableColumns = new ArrayList<IColumn<TypeTreeNode, String>>();
		this.attributeTableColumns.add(new AbstractColumn<TypeTreeNode, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final TypeTreeNode attribute = ((TypeTreeNode) rowModel.getObject());
				boolean checkBoxEnabled = true;
				// disable attribute checkbox if the attribute is
				// selected in
				// timestamp dropdown
				if (!ExcelEventTypeCreator.this.eventTypeUsingImportTime
						&& ExcelEventTypeCreator.this.timestampAttribute != null
						&& ExcelEventTypeCreator.this.timestampAttribute.equals(attribute)) {
					checkBoxEnabled = false;
				}
				cellItem.add(new AttributeTypeCheckBoxPanel(componentId, attribute, checkBoxEnabled,
						ExcelEventTypeCreator.this.eventAttributeProvider, ExcelEventTypeCreator.this.tableContainer));
			}
		});
		this.attributeTableColumns.add(new PropertyColumn<TypeTreeNode, String>(Model.of("Name"), "name"));
		this.attributeTableColumns.add(new AbstractColumn<TypeTreeNode, String>(new Model("Type")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final TypeTreeNode attribute = ((TypeTreeNode) rowModel.getObject());
				boolean dropDownChoiceEnabled = true;
				// disable attribute type dropdown choice if the
				// attribute is
				// selected in timestamp dropdown
				if (!ExcelEventTypeCreator.this.eventTypeUsingImportTime
						&& ExcelEventTypeCreator.this.timestampAttribute != null
						&& ExcelEventTypeCreator.this.timestampAttribute.equals(attribute)) {
					dropDownChoiceEnabled = false;
				}
				cellItem.add(new AttributeTypeDropDownChoicePanel(componentId, attribute, dropDownChoiceEnabled,
						ExcelEventTypeCreator.this.eventAttributeProvider));
			}
		});

		this.attributeTable = new DefaultDataTable<TypeTreeNode, String>("attributeTable", this.attributeTableColumns,
				this.eventAttributeProvider, 20);
		this.attributeTable.setOutputMarkupId(true);

		this.layoutForm.add(this.attributeTable);

		// confirm button
		final BlockingAjaxButton confirmButton = new BlockingAjaxButton("confirmButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);

				if (ExcelEventTypeCreator.this.eventTypeName.isEmpty()) {
					ExcelEventTypeCreator.this.eventTypeName = FileUtils
							.getFileNameWithoutExtension(ExcelEventTypeCreator.this.filePath);
				}

				// check if event type with this name already exists
				if (EapEventType.getAllTypeNames().contains(ExcelEventTypeCreator.this.eventTypeName)) {
					// TODO: Ask the user if it shall be added to the existing
					// event type with this name
					ExcelEventTypeCreator.this.excelEventTypeCreator.getFeedbackPanel().error(
							"Event type with this name already exists!");
					target.add(ExcelEventTypeCreator.this.excelEventTypeCreator.getFeedbackPanel());
					return;
				}

				/*
				 * remove attribute to be used as timestamp from attributes
				 * (value type) tree since the attribute is stored in the event
				 * type directly
				 */
				if (!ExcelEventTypeCreator.this.eventTypeUsingImportTime) {
					ExcelEventTypeCreator.this.timestampAttribute.removeAttribute();
					ExcelEventTypeCreator.this.eventTypeAttributesTree
							.removeRoot(ExcelEventTypeCreator.this.timestampAttribute);
				}

				// remove attributes that are not selected
				ExcelEventTypeCreator.this.eventTypeAttributesTree
						.retainAllAttributes(ExcelEventTypeCreator.this.eventAttributeProvider.getSelectedEntities());
				EapEventType eventType;
				try {
					eventType = new EapEventType(ExcelEventTypeCreator.this.eventTypeName,
							ExcelEventTypeCreator.this.eventTypeAttributesTree,
							ExcelEventTypeCreator.this.timestampName);
					Broker.getEventAdministrator().importEventType(eventType);
				} catch (final RuntimeException e) {
					ExcelEventTypeCreator.this.excelEventTypeCreator.getFeedbackPanel().error(e.getMessage());
					target.add(ExcelEventTypeCreator.this.excelEventTypeCreator.getFeedbackPanel());
					return;
				}
				final List<EapEvent> events = ExcelEventTypeCreator.this.fileNormalizer.importEventsFromFile(
						ExcelEventTypeCreator.this.filePath,
						ExcelEventTypeCreator.this.eventTypeAttributesTree.getRoots(),
						ExcelEventTypeCreator.this.timestampName);
				if (ExcelEventTypeCreator.this.eventTypeUsingImportTime) {
					for (final EapEvent actualEvent : events) {
						actualEvent.setTimestamp(new Date());
					}
				}
				for (final EapEvent event : events) {
					event.setEventType(eventType);
				}

				Broker.getEventImporter().importEvents(events);
				final PageParameters pageParameters = new PageParameters();

				pageParameters.add("successFeedback", events.size() + " events have been added to "
						+ ExcelEventTypeCreator.this.eventTypeName);
				this.setResponsePage(MainPage.class, pageParameters);
			}
		};

		this.layoutForm.add(confirmButton);
	}

	private void configurePreviewTable() {

		this.events = this.fileNormalizer.importEventsForPreviewFromFile(this.filePath, this.columnTitles);

		for (final ImportEvent event : this.events) {
			final Map<String, String> eventValues = new HashMap<String, String>();
			if (event.getTimestamp() != null) {
				eventValues.put(event.getExtractedTimestampName(), event.getTimestamp().toString());
			}
			for (final String columnTitle : this.columnTitles) {
				if (!(TimeStampNames.contains(columnTitle) || columnTitle
						.equals(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME))) {
					final String actualEventValue = (String) event.getValues().get(columnTitle);
					if (actualEventValue != null) {
						eventValues.put(columnTitle, actualEventValue.toString());
					}
				}
			}
			eventValues.put(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME, event.getImportTime().toString());
			this.tableRows.add(eventValues);
		}

		// table

		this.tableContainer = new WebMarkupContainer("tableContainer");
		this.tableContainer.setOutputMarkupId(true);

		final List<String> allColumnTitles = new ArrayList<String>(this.columnTitles);
		allColumnTitles.add(ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME);
		final List<List<String>> headers = new ArrayList<List<String>>();
		headers.add(allColumnTitles);

		this.headerContainer = new ListView<List<String>>("headerContainer", headers) {
			private static final long serialVersionUID = -5180277632835328415L;

			@Override
			protected void populateItem(final ListItem<List<String>> item) {
				final List<String> selectedColumnTitles = ExcelEventTypeCreator.this.eventAttributeProvider
						.getSelectedColumnNames();
				item.add(new ListView<String>("column", selectedColumnTitles) {
					private static final long serialVersionUID = -6368677142275503560L;

					@Override
					protected void populateItem(final ListItem<String> item) {
						item.add(new Label("cell", item.getModelObject()));
					}

				});
			}
		};
		this.headerContainer.setOutputMarkupId(true);

		this.rowContainer = new ListView<Map<String, String>>("rowContainer", this.tableRows) {
			private static final long serialVersionUID = -3289707574304971363L;

			@Override
			protected void populateItem(final ListItem<Map<String, String>> item) {
				final List<String> selectedColumnTitles = ExcelEventTypeCreator.this.eventAttributeProvider
						.getSelectedColumnNames();
				final Map<String, String> row = item.getModelObject();
				item.add(new ListView<String>("column", selectedColumnTitles) {
					private static final long serialVersionUID = -2477176270802239757L;
					int i = 0;

					@Override
					protected void populateItem(final ListItem<String> item) {
						item.add(new Label("cell", row.get(selectedColumnTitles.get(this.i++))));
					}
				});
			}
		};
		this.rowContainer.setOutputMarkupId(true);

		this.tableContainer.add(this.headerContainer);
		this.tableContainer.add(this.rowContainer);
		this.layoutForm.add(this.tableContainer);
	}

	private String setTimestampPreselection(final List<TypeTreeNode> attributes) {
		final TypeTreeNode timestampAttribute = this.findBestTimestampMatch(attributes);
		if (timestampAttribute != null) {
			return timestampAttribute.getName();
		} else {
			return null;
		}
	}

	public TypeTreeNode findBestTimestampMatch(final List<TypeTreeNode> attributes) {
		for (final TypeTreeNode attribute : attributes) {
			final String attributeName = attribute.getName().toLowerCase();
			final Pattern regex = Pattern.compile("^time|time$|date|datum|uhrzeit|zeit$");
			final Matcher match = regex.matcher(attributeName);
			if (match.find()) {
				return attribute;
			}
		}
		return null;
	}
}