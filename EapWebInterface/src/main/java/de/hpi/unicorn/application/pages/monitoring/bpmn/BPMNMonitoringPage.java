/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.bpmn;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.monitoring.bpmn.analysis.BPMNAnalysisPanel;
import de.hpi.unicorn.application.pages.monitoring.bpmn.monitoring.BPMNMonitoringPanel;

/**
 * This page facilitates the monitoring of the execution of BPMN processes. It
 * provides a {@link BPMNMonitoringPanel} and a {@link BPMNAnalysisPanel}.
 * 
 * @author micha
 */
public class BPMNMonitoringPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private AbstractEapPage monitoringPage;

	/**
	 * Constructor for a page, which facilitates the monitoring of the execution
	 * of BPMN processes. It provides a {@link BPMNMonitoringPanel} and a
	 * {@link BPMNAnalysisPanel}.
	 */
	public BPMNMonitoringPage() {
		super();
		this.monitoringPage = this;

		final List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTab(new Model<String>("BPMN-based Monitoring")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new BPMNMonitoringPanel(panelId, BPMNMonitoringPage.this.monitoringPage);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("BPMN-based Analysis")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new BPMNAnalysisPanel(panelId, BPMNMonitoringPage.this.monitoringPage);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
	}

}
