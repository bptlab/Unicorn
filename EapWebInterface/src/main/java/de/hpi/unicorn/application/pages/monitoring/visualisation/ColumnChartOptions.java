/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.wickedcharts.highcharts.options.Axis;
import com.googlecode.wickedcharts.highcharts.options.ChartOptions;
import com.googlecode.wickedcharts.highcharts.options.CreditOptions;
import com.googlecode.wickedcharts.highcharts.options.DataLabels;
import com.googlecode.wickedcharts.highcharts.options.Function;
import com.googlecode.wickedcharts.highcharts.options.Global;
import com.googlecode.wickedcharts.highcharts.options.HorizontalAlignment;
import com.googlecode.wickedcharts.highcharts.options.Labels;
import com.googlecode.wickedcharts.highcharts.options.Legend;
import com.googlecode.wickedcharts.highcharts.options.LegendLayout;
import com.googlecode.wickedcharts.highcharts.options.Options;
import com.googlecode.wickedcharts.highcharts.options.Overflow;
import com.googlecode.wickedcharts.highcharts.options.PlotOptions;
import com.googlecode.wickedcharts.highcharts.options.PlotOptionsChoice;
import com.googlecode.wickedcharts.highcharts.options.SeriesType;
import com.googlecode.wickedcharts.highcharts.options.Title;
import com.googlecode.wickedcharts.highcharts.options.Tooltip;
import com.googlecode.wickedcharts.highcharts.options.VerticalAlignment;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.series.SimpleSeries;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.visualisation.ChartConfiguration;

/**
 * This class prepares a column diagram that illustrates the frequency of the
 * values of an attribute of an eventtype This object can be used to create a
 * chart-object with the wicked chart framework.
 */
public class ColumnChartOptions extends Options {

	private static final long serialVersionUID = 1L;

	public EapEventType eventType;
	public String attributeName;
	public AttributeTypeEnum attributeType;
	public int rangeSize;
	public String title;

	public ColumnChartOptions(final ChartConfiguration configuration) {
		this.eventType = configuration.getEventType();
		this.attributeName = configuration.getAttributeName();
		this.attributeType = configuration.getAttributeType();
		this.rangeSize = configuration.getRangeSize();
		this.title = configuration.getTitle();

		this.setChartOptions(new ChartOptions().setType(SeriesType.COLUMN));

		this.setGlobal(new Global().setUseUTC(Boolean.TRUE));

		this.setTitle(new Title(this.title));

		this.setxAxis(new Axis().setCategories(this.eventType.getTypeName()).setTitle(new Title(null)));

		this.setyAxis(new Axis().setTitle(new Title("Frequency").setAlign(HorizontalAlignment.HIGH)).setLabels(
				new Labels().setOverflow(Overflow.JUSTIFY)));

		this.setTooltip(new Tooltip().setFormatter(new Function("return ''+this.series.name +': '+ this.y;")));

		this.setPlotOptions(new PlotOptionsChoice().setBar(new PlotOptions().setDataLabels(new DataLabels()
				.setEnabled(Boolean.TRUE))));

		this.setLegend(new Legend().setLayout(LegendLayout.VERTICAL).setAlign(HorizontalAlignment.RIGHT)
				.setVerticalAlign(VerticalAlignment.TOP).setX(-100).setY(100).setFloating(Boolean.TRUE)
				.setBorderWidth(1).setBackgroundColor(new HexColor("#ffffff")).setShadow(Boolean.TRUE));

		this.setCredits(new CreditOptions().setEnabled(Boolean.FALSE));

		for (final SimpleSeries serie : this.buildSeries()) {
			this.addSeries(serie);
		}

	}

	/**
	 * create data series for diagram
	 * 
	 * @return data series
	 */
	public List<SimpleSeries> buildSeries() {
		if (this.attributeType.equals(AttributeTypeEnum.STRING)) {
			return this.getStringSeries();
		} else {
			return this.getIntegerValues();
		}
	}

	/**
	 * create data series for string attributes
	 * 
	 * @return data series
	 */
	public List<SimpleSeries> getStringSeries() {
		final List<SimpleSeries> series = new ArrayList<SimpleSeries>();
		// collect values of attribute, each value will be one column
		final List<String> distinctValues = EapEvent.findDistinctValuesOfAttributeOfType(this.attributeName,
				this.eventType);
		for (final String value : distinctValues) {
			// count number of appearances
			final long numberOfAppearances = EapEvent.findNumberOfAppearancesByAttributeValue(this.attributeName,
					value, this.eventType);
			final SimpleSeries serie = new SimpleSeries();
			serie.setName(value);
			serie.setData(numberOfAppearances);
			series.add(serie);
		}
		return series;
	}

	/**
	 * create data series for integer attributes
	 * 
	 * @return data series
	 */
	public List<SimpleSeries> getIntegerValues() {
		try {
			final List<SimpleSeries> series = new ArrayList<SimpleSeries>();
			// build groups
			final List<IntegerBarChartValue> periods = new ArrayList<IntegerBarChartValue>();
			final long max = EapEvent.getMaxOfAttributeValue(this.attributeName, this.eventType);
			final long min = EapEvent.getMinOfAttributeValue(this.attributeName, this.eventType);
			if (min < 0) {
				// build ranges for min to 0
				for (int i = -1; i <= min; i -= this.rangeSize) {
					periods.add(new IntegerBarChartValue(i - this.rangeSize - 1, i));
				}
			}
			// build ranges from 0 up
			for (int i = 0; i <= max; i += this.rangeSize) {
				periods.add(new IntegerBarChartValue(i, i + this.rangeSize - 1));
			}
			// collect frequencies
			final List<EapEvent> events = EapEvent.findByEventType(this.eventType);
			for (final EapEvent event : events) {
				// int value = Integer.parseInt((String)
				// event.getValues().getValueOfAttribute(attributeName));
				final int value = Integer.parseInt((String) event.getValueTree()
						.getValueOfAttribute(this.attributeName));
				for (final IntegerBarChartValue period : periods) {
					if (period.containsValue(value)) {
						period.increaseFrequency();
						break;
					}
				}
			}
			// put into series
			for (final IntegerBarChartValue value : periods) {
				if (value.getFrequency() == 0) {
					continue;
				}
				final SimpleSeries serie = new SimpleSeries();
				serie.setName(value.getNameOfPeriod());
				serie.setData(value.getFrequency());
				series.add(serie);
			}
			return series;
		} catch (final Exception e) {
			return this.getStringSeries();
		}
	}

}