/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.util.DateUtils;

import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.AxisType;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.DateTimeLabelFormat;
import com.googlecode.wickedcharts.highcharts.options.DateTimeLabelFormat.DateTimeProperties;
import com.googlecode.wickedcharts.highcharts.options.Function;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.Tooltip;
import com.googlecode.wickedcharts.highcharts.options.color.RgbaColor;
import com.googlecode.wickedcharts.highcharts.options.series.Coordinate;
import com.googlecode.wickedcharts.highcharts.options.series.CustomCoordinatesSeries;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.visualisation.ChartConfiguration;

/**
 * This class prepares a splatter diagram that illustrates the attribute values
 * of an attribute of an eventtype This object can be used to create a
 * chart-object with the wicked chart framework.
 */
public class SplatterChartOptions extends Options {

	public EapEventType eventType;
	public String attributeName;
	public String title;

	private static final long serialVersionUID = 1L;

	public SplatterChartOptions(final ChartConfiguration configuration) throws Exception {
		this.eventType = configuration.getEventType();
		this.attributeName = configuration.getAttributeName();
		this.title = configuration.getTitle();

		final ChartOptions chartOptions = new ChartOptions();
		chartOptions.setType(SeriesType.SCATTER);
		this.setChartOptions(chartOptions);

		this.setTitle(new Title(this.title));

		// X-Achse
		final Axis xAxis = new Axis();
		xAxis.setType(AxisType.DATETIME);

		final DateTimeLabelFormat dateTimeLabelFormat = new DateTimeLabelFormat()
				.setProperty(DateTimeProperties.DAY, "%e.%m.%Y").setProperty(DateTimeProperties.MONTH, "%m/%Y")
				.setProperty(DateTimeProperties.YEAR, "%Y");

		xAxis.setDateTimeLabelFormats(dateTimeLabelFormat);

		this.setxAxis(xAxis);

		// Y-Achse
		final Axis yAxis = new Axis();
		yAxis.setTitle(new Title(this.attributeName));
		yAxis.setType(AxisType.LINEAR);

		this.setyAxis(yAxis);

		// Tooltip
		final Tooltip tooltip = new Tooltip();
		tooltip.setFormatter(new Function(
				"return '<b>'+ this.series.name +'</b><br/>'+Highcharts.dateFormat('%e.%m.%Y', this.x) +': '+ this.y ;"));
		this.setTooltip(tooltip);

		final CustomCoordinatesSeries<String, Number> series = new CustomCoordinatesSeries<String, Number>();
		series.setColor(new RgbaColor(119, 152, 191, 0.5f));
		series.setName(this.eventType.getTypeName());
		series.setData(this.getSeriesData());
		this.addSeries(series);

	}

	/**
	 * prepares data for the chart
	 * 
	 * @return data series
	 * @throws Exception
	 */
	private List<Coordinate<String, Number>> getSeriesData() throws Exception {

		final List<Coordinate<String, Number>> seriesData = new ArrayList<Coordinate<String, Number>>();

		for (final EapEvent event : EapEvent.findByEventType(this.eventType)) {
			final Serializable value = event.getValues().get(this.attributeName);
			if (value == null) {
				throw new Exception("AttributeName " + this.attributeName + " contains null-Values for "
						+ this.eventType);
			}
			// values should be integer
			int intValue = 0;
			try {
				intValue = Integer.parseInt((String) value);
			} catch (final Exception e) {
				intValue = (Integer) value;
			}
			;
			seriesData.add(new Coordinate<String, Number>(DateUtils.format(event.getTimestamp(),
					"'Date.UTC('yyyy, M, d, h, m, s')'"), intValue));
		}

		return seriesData;
	}
}