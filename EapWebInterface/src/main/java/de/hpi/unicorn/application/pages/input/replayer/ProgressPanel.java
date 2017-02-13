/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.Date;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;

import de.hpi.unicorn.application.components.form.DataTableButtonPanel;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;

public class ProgressPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private ReplayerPage page;
	private ProgressPanel panel;
	private WarnOnExitForm layoutForm;
	private DefaultDataTable<Date, String> progressTable;

	public ProgressPanel(String id, ReplayerPage page) {
		super(id);
		this.page = page;
		this.panel = this;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);
		addReplayerTable();
	}

	private void addReplayerTable() {
		final ProgressProvider provider = new ProgressProvider(new ArrayList<Date>(ReplayerContainer.getReplayers()
				.keySet()));
		ArrayList<IColumn<Date, String>> columns = new ArrayList<IColumn<Date, String>>();

		columns.add(new AbstractColumn<Date, String>(Model.of("Start date")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				item.add(new Label(componentId, creationDate.toString()));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Progress")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer er = ReplayerContainer.getReplayer(creationDate);
				float percentage = (float) er.getReplayedNumberOfEvents() / er.getTotalNumberOfEvents() * 100;
				item.add(new Label(componentId, percentage + " % (" + er.getReplayedNumberOfEvents() + " of "
						+ er.getTotalNumberOfEvents() + " events)"));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Category")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer r = ReplayerContainer.getReplayer(creationDate);
				item.add(new Label(componentId, r.category));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Files w/ name (event type)")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer r = ReplayerContainer.getReplayer(creationDate);
				item.add(new Label(componentId, r.getBeans().toString()));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Scale factor")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer r = ReplayerContainer.getReplayer(creationDate);
				item.add(new Label(componentId, r.getScaleFactor()));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Time mode")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer r = ReplayerContainer.getReplayer(creationDate);
				item.add(new Label(componentId, r.mode));
			}
		});

		columns.add(new AbstractColumn<Date, String>(Model.of("Delete")) {
			@Override
			public void populateItem(Item<ICellPopulator<Date>> item, String componentId, IModel<Date> rowModel) {
				final Date creationDate = (Date) rowModel.getObject();
				EventReplayer r = ReplayerContainer.getReplayer(creationDate);
				if (r.getReplayedNumberOfEvents() / r.getTotalNumberOfEvents() == 1) {
					final AjaxButton confirmButton = new AjaxButton("button", layoutForm) {

						private static final long serialVersionUID = 1L;

						@Override
						public void onSubmit(final AjaxRequestTarget target, final Form form) {
							super.onSubmit(target, form);
							provider.removeEntity(creationDate);
							ReplayerContainer.removeReplayer(creationDate);
							page.getFeedbackPanel().success("Deleted replayer from" + creationDate + ".");
							target.add(progressTable);
						}
					};

					WebMarkupContainer buttonPanel = new WebMarkupContainer(componentId);
					try {
						buttonPanel = new DataTableButtonPanel(componentId, confirmButton);
					} catch (final Exception e) {
						e.printStackTrace();
					}
					buttonPanel.setOutputMarkupId(true);
					item.add(buttonPanel);
				} else {
					item.add(new Label(componentId, "running"));
				}

			}
		});

		progressTable = new DefaultDataTable<Date, String>("progressTable", columns, provider, 10000);
		progressTable.setOutputMarkupId(true);
		layoutForm.add(progressTable);

		layoutForm.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(2)) {
			@Override
			protected void onPostProcessTarget(AjaxRequestTarget target) {
				provider.setEntities(new ArrayList<Date>(ReplayerContainer.getReplayers().keySet()));
				target.add(progressTable);
			}
		});

	}

}
