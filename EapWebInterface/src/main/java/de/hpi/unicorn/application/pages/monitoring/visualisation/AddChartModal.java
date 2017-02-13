/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.visualisation;

import de.hpi.unicorn.application.components.form.BootstrapModal;

/**
 * Modal that encapsulates the panel for the creation of attribute charts
 */
public class AddChartModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private final ChartConfigurationPanel panel;

	public AddChartModal(final String id, final AttributeChartPage visualisationPage) {
		super(id, "Add Chart");
		this.panel = new ChartConfigurationPanel("chartConfigurationPanel", visualisationPage);
		this.add(this.panel);
		this.panel.updateSlider();
	}
}
