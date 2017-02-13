/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;

@SuppressWarnings("serial")
public class ReplayerPage extends AbstractEapPage {

	private ReplayerPage page;

	public ReplayerPage() {
		super();
		this.page = this;

		// if (!EapConfiguration.persistEvents) {
		// ReplayPage.this.getFeedbackPanel().info("Storage of new incoming events has been disabled.");
		// }

		// select event type
		// select category ("scenario 3")
		// take filename as identifier

		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Upload files")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new UploadPanel(panelId, page);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("Replay events")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new FilesPanel(panelId, page);
			}
		});

		tabs.add(new AbstractTab(new Model<String>("See progress")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new ProgressPanel(panelId, page);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));
		setOutputMarkupId(true);

	}
}
