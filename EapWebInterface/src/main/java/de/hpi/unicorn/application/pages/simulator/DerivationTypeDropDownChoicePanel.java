/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.simulator;

import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.tree.LabelTreeTable;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableElement;
import de.hpi.unicorn.application.pages.simulator.model.SimulationTreeTableProvider;
import de.hpi.unicorn.simulation.DerivationType;

public class DerivationTypeDropDownChoicePanel extends Panel {

	private static final long serialVersionUID = 1L;
	private final List<DerivationType> derivationTypes = Arrays.asList(DerivationType.values());
	private LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable;
	protected DerivationType derivationType;

	public DerivationTypeDropDownChoicePanel(final String id, final int entryId,
			final SimulationTreeTableProvider<Object> simulationTreeTableProvider) {
		super(id);
		final Form<Void> layoutForm = new Form<Void>("layoutForm");

		final DropDownChoice<DerivationType> derivationTypeDropDownChoice = new DropDownChoice<DerivationType>(
				"derivationTypeDropDownChoice",
				Model.of(simulationTreeTableProvider.getDerivationTypeForEntry(entryId)), this.derivationTypes);
		derivationTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				simulationTreeTableProvider.setDerivationTypeForEntry(derivationTypeDropDownChoice.getModelObject(),
						entryId);

				if (DerivationTypeDropDownChoicePanel.this.treeTable != null) {
					target.add(DerivationTypeDropDownChoicePanel.this.treeTable);
				} else {
					target.add(DerivationTypeDropDownChoicePanel.this.getPage());
				}
			}
		});

		derivationTypeDropDownChoice.setEnabled(true);
		layoutForm.add(derivationTypeDropDownChoice);
		this.add(layoutForm);
	}

	public void setTable(final LabelTreeTable<SimulationTreeTableElement<Object>, String> treeTable) {
		this.treeTable = treeTable;
	}

}
