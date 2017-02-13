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
import de.hpi.unicorn.simulation.DerivationType;

public class DurationEntryPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final TextField<String> durationTextField, derivationTextField;

	public DurationEntryPanel(final String id, final int entryId,
			final SimulationTreeTableProvider<Object> simulationTreeTableProvider) {
		super(id);
		final Form<Void> form = new Form<Void>("form");

		this.durationTextField = new TextField<String>("durationTextField", Model.of(simulationTreeTableProvider
				.getDurationForEntry(entryId)));
		this.durationTextField.setOutputMarkupPlaceholderTag(true);
		this.durationTextField.setOutputMarkupId(true);
		this.durationTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				simulationTreeTableProvider.setDurationForEntry(DurationEntryPanel.this.getMeanDurationFromField(),
						entryId);
			}
		});

		form.add(this.durationTextField);

		this.derivationTextField = new TextField<String>("derivationTextField", Model.of(simulationTreeTableProvider
				.getDerivationForEntry(entryId)));
		this.derivationTextField.setOutputMarkupPlaceholderTag(true);
		this.derivationTextField.setOutputMarkupId(true);
		this.derivationTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {

				simulationTreeTableProvider.setDerivationForEntry(DurationEntryPanel.this.getMeanDurationFromField(),
						entryId);
			}
		});
		final DerivationType derivationType = simulationTreeTableProvider.getDerivationTypeForEntry(entryId);
		if (!(DerivationType.NORMAL.equals(derivationType))) {
			this.derivationTextField.setVisible(false);
		}
		form.add(this.derivationTextField);

		this.add(form);
		simulationTreeTableProvider.registerDurationInputAtEntry(this, entryId);
	}

	public void setTable(final LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable) {
	}

	private String getMeanDurationFromField() {
		return this.durationTextField.getValue();
	}

	public void setDerivationType(final DerivationType derivationType) {
		if (derivationType.equals(DerivationType.NORMAL)) {
			this.derivationTextField.setVisible(true);
		} else {
			this.derivationTextField.setVisible(false);
		}
	}
}
