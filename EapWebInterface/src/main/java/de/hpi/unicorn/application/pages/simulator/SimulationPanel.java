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
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.Collapsible;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableProvider;
import de.hpi.unicorn.bpmn.element.BPMNTask;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

public abstract class SimulationPanel extends Panel {

	protected AbstractEapPage abstractEapPage;
	protected TextField<String> instanceNumberInput;
	protected TextField<String> daysNumberInput;
	protected Form<Void> layoutForm;
	protected SimulationPanel simulationPanel;
	protected AdvancedValuesPanel advancedValuesPanel;
	protected IndependentUnexpectedEventPanel independentUnexpectedEventPanel;
	protected SimulationTreeTableProvider<Object> treeTableProvider;

	public SimulationPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);
		this.simulationPanel = this;
		this.abstractEapPage = abstractEapPage;
		this.treeTableProvider = new SimulationTreeTableProvider<Object>();
	}

	protected void addTabs() {
		final List<ITab> tabs = new ArrayList<ITab>();
		this.addAdvancedValuesPanel(tabs);
		this.addUnexpectedEventPanel(tabs);
		this.addIndependentUnexpectedEventPanel(tabs);

		this.layoutForm.add(new Collapsible("collapsible", tabs, Model.of(-1)));
	}

	private void addAdvancedValuesPanel(final List<ITab> tabs) {
		tabs.add(new AbstractTab(new Model<String>("Advanced values for attributes")) {

			@Override
			public Panel getPanel(final String panelId) {
				SimulationPanel.this.advancedValuesPanel = new AdvancedValuesPanel(panelId,
						SimulationPanel.this.simulationPanel);
				return SimulationPanel.this.advancedValuesPanel;
			}
		});
	}

	protected abstract void addUnexpectedEventPanel(List<ITab> tabs);

	protected void addIndependentUnexpectedEventPanel(final List<ITab> tabs) {
		tabs.add(new AbstractTab(new Model<String>("Unexpected Events (instance-independent)")) {

			@Override
			public Panel getPanel(final String panelId) {
				SimulationPanel.this.independentUnexpectedEventPanel = new IndependentUnexpectedEventPanel(panelId);
				return SimulationPanel.this.independentUnexpectedEventPanel;
			}
		});

	}

	public List<EapEventType> getUsedEventTypes() {
		return this.treeTableProvider.getEventTypes();
	}

	public List<BPMNTask> getTasks() {
		return this.treeTableProvider.getTasks();
	}

	public List<TypeTreeNode> getAttributesFromTable() {
		return this.treeTableProvider.getAttributes();
	}

}
