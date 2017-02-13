/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;

public class SimulationPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private AbstractEapPage simulationPage;

	public SimulationPage() {
		super();
		this.simulationPage = this;

		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Simple")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new SimpleSimulationPanel(panelId, SimulationPage.this.simulationPage);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("BPMN")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new BPMNSimulationPanel(panelId, SimulationPage.this.simulationPage);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
	}

}
