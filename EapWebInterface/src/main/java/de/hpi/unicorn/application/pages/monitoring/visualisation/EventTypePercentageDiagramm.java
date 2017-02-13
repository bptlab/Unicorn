/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.Cursor;
import com.googlecode.wickedcharts.highcharts.options.DataLabels;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.PlotOptions;
import com.googlecode.wickedcharts.highcharts.options.PlotOptionsChoice;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.Tooltip;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.color.NullColor;
import com.googlecode.wickedcharts.highcharts.options.functions.PercentageFormatter;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.options.series.Series;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;

/**
 * This class prepares a pie diagram that illustrates the percentage of events
 * by event type. This object can be used to create a chart-object with the
 * wicked chart framework.
 */
public class EventTypePercentageDiagramm extends Options {

	private static final long serialVersionUID = 1L;

	public EventTypePercentageDiagramm() {

		this.setChartOptions(new ChartOptions().setPlotBackgroundColor(new NullColor()).setPlotBorderWidth(null)
				.setPlotShadow(Boolean.FALSE));

		this.setTitle(new Title("Percentage of Events by Event Types"));

		this.setTooltip(new Tooltip().setFormatter(new PercentageFormatter()).setPercentageDecimals(1));

		this.setPlotOptions(new PlotOptionsChoice().setPie(new PlotOptions()
				.setAllowPointSelect(Boolean.TRUE)
				.setCursor(Cursor.POINTER)
				.setDataLabels(
						new DataLabels().setEnabled(Boolean.TRUE).setColor(new HexColor("#000000"))
								.setConnectorColor(new HexColor("#000000")).setFormatter(new PercentageFormatter()))));

		this.addSeries(this.prepareEventSeries());

	}

	/**
	 * prepare data for diagram by calculating percentages of events by
	 * eventtype
	 * 
	 * @return data series for pie chart
	 */
	private Series<Point> prepareEventSeries() {
		final Series<Point> series = new PointSeries().setType(SeriesType.PIE).setName("Event Types Percentage");

		// get overall number of events
		final double numberOfEvents = EapEvent.getNumberOfEvents();

		for (final EapEventType type : EapEventType.findAll()) {
			final double numberOfEventsOfEventType = EapEvent.getNumberOfEventsByEventType(type);
			double percentage = 0;
			if (numberOfEvents > 0) {
				// calculate percentage
				percentage = numberOfEventsOfEventType / numberOfEvents;
			}
			series.addPoint(new Point(type.getTypeName(), Math.round(percentage * 100) / 100.0));
		}
		return series;
	}

}
