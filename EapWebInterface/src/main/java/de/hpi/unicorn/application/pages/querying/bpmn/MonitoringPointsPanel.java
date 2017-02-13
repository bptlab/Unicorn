/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying.bpmn;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.pages.querying.bpmn.model.BPMNTreeTableElement;
import de.hpi.unicorn.bpmn.monitoringpoint.MonitoringPointStateTransition;

/**
 * A panel on which monitoring points can be assigned to BPMN process elements
 * with {@link MonitoringPointField}s.
 * 
 * @author micha
 */
public class MonitoringPointsPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final BPMNTreeTableElement treeTableElement;

	/**
	 * Constructor for a panel on which monitoring points can be assigned to
	 * BPMN process elements with {@link MonitoringPointField}s.
	 * 
	 * @param id
	 * @param entryId
	 * @param treeTableElement
	 */
	public MonitoringPointsPanel(final String id, final int entryId, final BPMNTreeTableElement treeTableElement) {
		super(id);
		this.treeTableElement = treeTableElement;
		this.buildLayout();
	}

	private void buildLayout() {
		final Form<Void> monitoringForm = new Form<Void>("monitoringForm");

		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.initialize);
		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.enable);
		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.begin);
		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.terminate);
		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.skip);
		new MonitoringPointField(this.treeTableElement).addMonitoringField(monitoringForm,
				MonitoringPointStateTransition.disrupt);

		this.add(monitoringForm);

	}

}
