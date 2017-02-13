/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
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
import de.hpi.unicorn.application.pages.eventrepository.model.EventFilter;
import de.hpi.unicorn.application.pages.eventrepository.model.EventProvider;
import de.hpi.unicorn.event.EapEvent;

/**
 * {@link Panel}, which shows the {@link EapEvent}s stored in the database.
 */
public class EventPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<IColumn<EapEvent, String>> columns;
	private final DefaultDataTable<EapEvent, String> dataTable;
	private final EventFilter eventFilter;
	private final EventProvider eventProvider;
	private final EventRepository eventRepository;
	private final SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	/**
	 * Constructor for the event panel. The page is initialized in this method
	 * and the data is loaded from the database.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	@SuppressWarnings("unchecked")
	public EventPanel(final String id, final EventRepository abstractEapPage) {
		super(id);

		this.eventProvider = new EventProvider();
		this.eventFilter = new EventFilter();
		this.eventProvider.setEventFilter(this.eventFilter);
		this.eventRepository = abstractEapPage;

		final Form<Void> buttonForm = new WarnOnExitForm("buttonForm");

		final List<String> eventFilterCriteriaList = new ArrayList<String>(Arrays.asList(new String[] { "ID",
				"Event Type (ID)", "Process Instance" }));
		for (final String eventAttribute : EapEvent.findAllEventAttributes()) {
			eventFilterCriteriaList.add(eventAttribute);
		}
		final String selectedEventCriteria = "ID";

		final DropDownChoice<String> eventFilterCriteriaSelect = new DropDownChoice<String>("eventFilterCriteria",
				new Model<String>(selectedEventCriteria), eventFilterCriteriaList);
		buttonForm.add(eventFilterCriteriaSelect);

		final List<String> conditions = new ArrayList<String>(Arrays.asList(new String[] { "<", "=", ">" }));
		final String selectedCondition = "=";

		final DropDownChoice<String> eventFilterConditionSelect = new DropDownChoice<String>("eventFilterCondition",
				new Model<String>(selectedCondition), conditions);
		buttonForm.add(eventFilterConditionSelect);

		final TextField<String> searchValueInput = new TextField<String>("searchValueInput", Model.of(""));
		buttonForm.add(searchValueInput);

		final AjaxButton filterButton = new AjaxButton("filterButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String eventFilterCriteria = eventFilterCriteriaSelect.getChoices().get(
						Integer.parseInt(eventFilterCriteriaSelect.getValue()));
				final String eventFilterCondition = eventFilterConditionSelect.getChoices().get(
						Integer.parseInt(eventFilterConditionSelect.getValue()));
				final String filterValue = searchValueInput.getValue();
				EventPanel.this.eventProvider.setEventFilter(new EventFilter(eventFilterCriteria, eventFilterCondition,
						filterValue));
				target.add(EventPanel.this.dataTable);
			}
		};
		buttonForm.add(filterButton);

		final AjaxButton resetButton = new AjaxButton("resetButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				EventPanel.this.eventProvider.setEventFilter(new EventFilter());
				target.add(EventPanel.this.dataTable);
			}
		};
		buttonForm.add(resetButton);

		final AjaxButton deleteButton = new BlockingAjaxButton("deleteButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				EventPanel.this.eventProvider.deleteSelectedEntries();
				target.add(EventPanel.this.dataTable);
			}
		};
		buttonForm.add(deleteButton);

		final AjaxButton selectAllButton = new AjaxButton("selectAllButton", buttonForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				EventPanel.this.eventProvider.selectAllEntries();
				target.add(EventPanel.this.dataTable);
			}
		};
		buttonForm.add(selectAllButton);

		this.add(buttonForm);

		this.columns = new ArrayList<IColumn<EapEvent, String>>();
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("ID"), "ID"));
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("Timestamp"), "timestamp") {
			@Override
			public void populateItem(final Item<ICellPopulator<EapEvent>> item, final String componentId,
					final IModel<EapEvent> rowModel) {
				final String shortenedValues = EventPanel.this.formatter.format(rowModel.getObject().getTimestamp());
				final Label label = new Label(componentId, shortenedValues);
				item.add(label);
			}
		});
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("EventType"), "eventType"));
		this.columns.add(new AbstractColumn<EapEvent, String>(Model.of("Values"), "values") {
			@Override
			public void populateItem(final Item<ICellPopulator<EapEvent>> item, final String componentId,
					final IModel<EapEvent> rowModel) {
				String shortenedValues = rowModel.getObject().getValues().toString();
				shortenedValues = shortenedValues.substring(1, shortenedValues.length() - 1);
				if (shortenedValues.length() > 200) {
					shortenedValues = shortenedValues.substring(0, 200) + "...";
				}
				final Label label = new Label(componentId, shortenedValues);
				label.add(new AjaxEventBehavior("onclick") {
					@Override
					protected void onEvent(final AjaxRequestTarget target) {
						// on click open Event View Modal
						EventPanel.this.eventRepository.getEventViewModal().getPanel().setEvent(rowModel.getObject());
						EventPanel.this.eventRepository.getEventViewModal().getPanel().detach();
						target.add(EventPanel.this.eventRepository.getEventViewModal().getPanel());
						EventPanel.this.eventRepository.getEventViewModal().show(target);
					}
				});
				item.add(label);
			}
		});
		this.columns.add(new PropertyColumn<EapEvent, String>(Model.of("Process Instances"), "processInstances"));
		this.columns.add(new AbstractColumn<EapEvent, String>(new Model("Select")) {
			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final int entryId = ((EapEvent) rowModel.getObject()).getID();
				cellItem.add(new SelectEntryPanel(componentId, entryId, EventPanel.this.eventProvider));
			};
		});

		this.dataTable = new DefaultDataTable<EapEvent, String>("events", this.columns, this.eventProvider, 20);
		this.dataTable.setOutputMarkupId(true);

		this.add(this.dataTable);

	}

};
