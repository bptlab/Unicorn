/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.Collapsible;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.input.replayer.EventReplayer.TimeMode;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.ReplayEvent;
import de.hpi.unicorn.importer.csv.CSVImporter;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;

public class FilesPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private ReplayerPage page;
	private FilesPanel panel;
	private WarnOnExitForm layoutForm;
	private ArrayList<ITab> tabs;

	public FilesPanel(String id, ReplayerPage page) {
		super(id);
		this.page = page;
		this.panel = this;

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);
		addScenarioTabs();
		layoutForm.setOutputMarkupId(true);
	}

	// TODO button to delete files from panel -> make table

	private void addScenarioTabs() {
		tabs = new ArrayList<ITab>();

		// tabs.add(new AbstractTab(new Model<String>("GET Scenario 3")) {
		// @Override
		// public Panel getPanel(final String panelId) {
		// return new Scenario3Panel(panelId, panel);
		// }
		// });

		final Map<String, List<ReplayFileBean>> fileBeans = ReplayerContainer.getFileBeans();

		for (final String category : fileBeans.keySet()) {
			tabs.add(new AbstractTab(new Model<String>(category)) {
				@Override
				public Panel getPanel(final String panelId) {
					return new CategoryPanel(panelId, panel, category, fileBeans.get(category));
				}
			});
		}

		this.layoutForm.addOrReplace(new Collapsible("collapsible", tabs, Model.of(-1)));
	}

	public List<EapEvent> generateEventsFromCSV(String file, EapEventType et) {
		CSVImporter c = new CSVImporter();
		c.setSeparator(',');
		List<EapEvent> events = c.importEventsFromFile(file, et.getValueTypes(), et.getTimestampName());
		for (EapEvent e : events) {
			e.setEventType(et);
		}
		return events;
	}

	public List<EapEvent> generateEventsFromXML(String file, EapEventType et) {
		List<EapEvent> events = new ArrayList<EapEvent>();
		try {
			events = XMLParser.generateEventsFromXML(file);
			for (EapEvent e : events) {
				e.setEventType(et);
			}
		} catch (XMLParsingException ex) {
			ex.printStackTrace();
		}
		return events;
	}

	public void replayEvents(List<EapEvent> events, int scaleFactor, String category,
			List<ReplayFileBean> selectedFiles, TimeMode mode, Date simulationTime, Long fixedOffset) {
		EventReplayer er = new EventReplayer(events, scaleFactor, category, selectedFiles, mode, simulationTime,
				fixedOffset);
		er.replay();
		ReplayerContainer.addReplayer(er);
	}

	public void replayEventsWithMultipleAlignment(TreeSet<ReplayEvent> replayEvents, int scaleFactor, String category,
			List<ReplayFileBean> selectedFiles, TimeMode mode, Date simulationTime, Long fixedOffset) {
		EventReplayer er = new EventReplayer(new ArrayList<EapEvent>(), scaleFactor, category, selectedFiles, mode,
				simulationTime, fixedOffset);
		er.setReplayList(replayEvents);
		er.replay();
		ReplayerContainer.addReplayer(er);
	}

	public ReplayerPage getParentPage() {
		return page;
	}

	public void removeCategory(AjaxRequestTarget target, String name) {
		ReplayerContainer.removeCategory(name);
		addScenarioTabs();
		target.add(layoutForm);
	}

}
