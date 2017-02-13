/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.eventviews;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.googlecode.wickedcharts.highcharts.options.ZoomType;
import com.googlecode.wickedcharts.highcharts.options.series.Coordinate;
import com.googlecode.wickedcharts.highcharts.options.series.CustomCoordinatesSeries;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.process.CorrelationProcessInstance;
import de.hpi.unicorn.visualisation.EventView;

/**
 * This class prepares a splatter diagram that illustrates the occurence of
 * events of certain eventtypes This object can be used to create a chart-object
 * with the wicked chart framework.
 */
public class EventViewOptions extends Options {

	public EventView eventView;

	private final List<Coordinate<String, Number>> eventsWithoutProcessInstance = new ArrayList<Coordinate<String, Number>>();
	private final HashMap<CorrelationProcessInstance, List<Coordinate<String, Number>>> processSeriesData = new HashMap<CorrelationProcessInstance, List<Coordinate<String, Number>>>();

	private final HashMap<EapEventType, Integer> mappingTypeToInt = new HashMap<EapEventType, Integer>();

	private static final long serialVersionUID = 1L;

	public EventViewOptions(final EventView configuration) {
		this.eventView = configuration;

		final ChartOptions chartOptions = new ChartOptions();
		chartOptions.setType(SeriesType.SCATTER);

		// enable zooming
		chartOptions.setZoomType(ZoomType.X);

		this.setChartOptions(chartOptions);

		this.setTitle(new Title("EventView (" + this.eventView.getTimePeriod().toString() + ")"));

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
		yAxis.setTitle(new Title("EventTypes"));
		yAxis.setType(AxisType.LINEAR);
		// disable Decimals, because integers represent event types, decimals
		// make no sense here
		yAxis.setAllowDecimals(false);
		yAxis.setMax(this.eventView.getEventTypes().size());
		this.setyAxis(yAxis);

		// Tooltip
		final Tooltip tooltip = new Tooltip();
		tooltip.setFormatter(new Function(
				"return '<b>'+ this.series.name +'</b><br/>'+Highcharts.dateFormat('%e.%m.%Y', this.x);"));
		this.setTooltip(tooltip);

		// create a mapping from eventtypes to integer values, because in a
		// splatter chart strings cannot be used in the y-axis
		this.generateMappingForEventTypes();

		for (final EapEventType type : this.eventView.getEventTypes()) {
			this.sortEventsForEventType(type);
		}

		// add series for each process instance to diagram
		for (final Entry<CorrelationProcessInstance, List<Coordinate<String, Number>>> seriesTuple : this.processSeriesData
				.entrySet()) {
			final List<Coordinate<String, Number>> seriesData = seriesTuple.getValue();
			final CustomCoordinatesSeries<String, Number> series = new CustomCoordinatesSeries<String, Number>();
			series.setName(seriesTuple.getKey().toString());
			series.setData(seriesData);
			this.addSeries(series);
		}

		// add series for uncorrelated events
		final CustomCoordinatesSeries<String, Number> series = new CustomCoordinatesSeries<String, Number>();
		series.setName("uncorrelated");
		series.setData(this.eventsWithoutProcessInstance);
		this.addSeries(series);
	};

	private void generateMappingForEventTypes() {
		// save an integer value for each event type
		for (int i = 1; i <= this.eventView.getEventTypes().size(); i++) {
			this.mappingTypeToInt.put(this.eventView.getEventTypes().get(i - 1), i);
		}
	}

	private void sortEventsForEventType(final EapEventType eventType) {
		for (final EapEvent event : EapEvent.findByEventTypeAndTime(eventType, this.eventView.getTimePeriod())) {
			final List<CorrelationProcessInstance> processInstances = event.getProcessInstances();
			if (processInstances.isEmpty()) {
				// no process instance
				this.eventsWithoutProcessInstance.add(this.getCoordinate(event, eventType));
				continue;
			}
			for (final CorrelationProcessInstance instance : processInstances) {
				List<Coordinate<String, Number>> seriesData = this.processSeriesData.get(instance);
				if (seriesData.equals(null)) { // create new seriesDatea
					seriesData = new ArrayList<Coordinate<String, Number>>();
				}
				seriesData.add(this.getCoordinate(event, eventType));
				this.processSeriesData.put(instance, seriesData);
			}
		}
	}

	private Coordinate<String, Number> getCoordinate(final EapEvent event, final EapEventType eventType) {
		return new Coordinate<String, Number>(DateUtils.format(event.getTimestamp(),
				"'Date.UTC('yyyy, M, d, h, m, s')'"), this.mappingTypeToInt.get(eventType));
	}

	/**
	 * creates an explanation string that translates the mapping from integer
	 * values to event types
	 * 
	 * @return explanation string
	 */
	public String getExplanationString() {
		String explanation = "";
		// sort event types
		final Map<Integer, EapEventType> intToEvent = EventViewOptions.invert(this.mappingTypeToInt);
		for (int i = 1; i <= intToEvent.size(); i++) {
			explanation += (i) + " : " + intToEvent.get(i).getTypeName() + "\t";
		}
		return explanation;
	}

	/**
	 * inverts a map, so that the former values become keys
	 * 
	 * @param map
	 * @return inverted map
	 */
	private static <V, K> Map<V, K> invert(final Map<K, V> map) {
		final Map<V, K> inv = new HashMap<V, K>();

		for (final Entry<K, V> entry : map.entrySet()) {
			inv.put(entry.getValue(), entry.getKey());
		}

		return inv;
	}

}