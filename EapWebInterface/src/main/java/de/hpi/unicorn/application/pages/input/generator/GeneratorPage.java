/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.generator;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import java.util.ArrayList;
import java.util.List;

/**
 * {@Link AbstractEapPage} that contains everything that is connected with the generation of events,
 * including the event generator itself and attribute dependency settings.
 */
@SuppressWarnings("serial")
public class GeneratorPage extends AbstractEapPage {

	private GeneratorPage page;

	public GeneratorPage() {
		super();
		this.page = this;

		final List<ITab> tabs = new ArrayList<ITab>();

		tabs.add(new AbstractTab(new Model<String>("Generate events")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new GeneratePanel(panelId, page);
			}
		});
		tabs.add(new AbstractTab(new Model<String>("Attribute dependencies")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new DependenciesPanel(panelId, page);
			}
		});
		tabs.add(new AbstractTab(new Model<String>("Export/import dependencies")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new ExportImportDependenciesPanel(panelId, page);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
		setOutputMarkupId(true);

	}
}
