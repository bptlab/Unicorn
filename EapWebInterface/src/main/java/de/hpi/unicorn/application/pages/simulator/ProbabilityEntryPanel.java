/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableElement;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableProvider;

public class ProbabilityEntryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final TextField<String> textField;

	public ProbabilityEntryPanel(final String id, final int entryId,
			final SimulationTreeTableProvider<Object> simulationTreeTableProvider) {
		super(id);
		final Form<Void> form = new Form<Void>("form");

		this.textField = new TextField<String>("textFieldID", Model.of(simulationTreeTableProvider
				.getProbabilityForEntry(entryId)));
		this.textField.setOutputMarkupPlaceholderTag(true);
		this.textField.setOutputMarkupId(true);
		this.textField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				simulationTreeTableProvider.setProbabilityForEntry(ProbabilityEntryPanel.this.textField.getValue(),
						entryId);
			}
		});

		form.add(this.textField);

		this.add(form);
	}

	public void setTable(final LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable) {
	}
}
