/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.googlecode.wickedcharts.wicket6.highcharts.Chart;

import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.visualisation.ChartConfiguration;

/**
 * This page offers shows a percentage chart of the events by event type, offers
 * a possibility to create an attribute chart and shows the attribute charts
 * already configured.
 */
public class AttributeChartPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private AjaxButton addButton;
	private final Form<Void> form;
	public ListView listview;
	public AddChartModal addChartModal;

	// loads all ChartConfiguration-Objects from the database
	@SuppressWarnings("serial")
	private IModel<List<ChartConfiguration>> options = new LoadableDetachableModel<List<ChartConfiguration>>() {
		@Override
		protected List<ChartConfiguration> load() {
			return ChartConfiguration.findAll();
		}
	};

	public AttributeChartPage() {
		super();

		// pie chart of percentage of events by event type
		this.add(new Chart("eventTypeChart", new EventTypePercentageDiagramm()));

		// Create the modal window for attribute chart creation.
		this.addChartModal = new AddChartModal("addChartModal", this);
		this.add(this.addChartModal);

		this.form = new Form<Void>("form");
		this.form.add(this.addAddChartButton());

		this.add(this.form);

		// add attribute charts from database
		this.addCharts();
	}

	/**
	 * creates the button that opens the addChartModal
	 * 
	 * @return AjaxButton, that opens the addChartModal
	 */
	private Component addAddChartButton() {
		this.addButton = new AjaxButton("addChartButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				target.prependJavaScript("Wicket.Window.unloadConfirmation = false;");
				AttributeChartPage.this.addChartModal.show(target);
			}
		};
		return this.addButton;
	}

	/**
	 * adds all attribute charts from database to page
	 */
	@SuppressWarnings({ "unchecked" })
	private void addCharts() {
		this.listview = new ListView("listview", this.getOptions()) {
			@Override
			protected void populateItem(final ListItem item) {
				// prepare and add chart
				final ChartConfiguration currentOptions = (ChartConfiguration) item.getModelObject();
				item.add(AttributeChartPage.this.addChart(currentOptions));
				// prepare and add removeButton
				final AjaxButton removeButton = new AjaxButton("removeChartButton") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form form) {
						currentOptions.remove();
						AttributeChartPage.this.getOptions().detach();
						target.add(AttributeChartPage.this.listview.getParent());
					}
				};
				final Form<Void> removeform = new Form<Void>("form");
				removeform.add(removeButton);
				item.add(removeform);
			}

		};
		this.listview.setOutputMarkupId(true);

		this.add(this.listview);
	}

	/**
	 * Forms the chartConfiguration into a chart-Component, that can be
	 * displayed
	 * 
	 * @param chartConfiguration
	 * @return chart-component
	 */
	private Component addChart(final ChartConfiguration currentOptions) {
		try {
			switch (currentOptions.getType()) {
			case SPLATTER: {
				return new Chart("chart", new SplatterChartOptions(currentOptions));
			}
			case COLUMN: {
				return new Chart("chart", new ColumnChartOptions(currentOptions));
			}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			return new Label("chart", currentOptions.getTitle() + " : This Chart could not be built. Sorry.");
		}
		return new Label("chart", currentOptions.getTitle() + " : Unsupported Chart type. Sorry.");
	}

	public IModel<List<ChartConfiguration>> getOptions() {
		return this.options;
	}

	public void setOptions(final IModel<List<ChartConfiguration>> options) {
		this.options = options;
	}
}
