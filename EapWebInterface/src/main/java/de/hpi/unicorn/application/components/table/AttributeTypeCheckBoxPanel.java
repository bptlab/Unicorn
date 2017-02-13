/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.table;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.pages.input.model.EventAttributeProvider;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class AttributeTypeCheckBoxPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public AttributeTypeCheckBoxPanel(final String id, final TypeTreeNode attribute, final boolean checkBoxEnabled,
			final EventAttributeProvider dataProvider, final Component tableContainer) {
		super(id);
		final Form<Void> form = new Form<Void>("layoutForm");

		final CheckBox checkBox = new CheckBox("attributeTypeCheckBox", Model.of(dataProvider
				.isEntrySelected(attribute)));
		checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (dataProvider.isEntrySelected(attribute)) {
					dataProvider.deselectEntry(attribute);
				} else if (!dataProvider.isEntrySelected(attribute)) {
					dataProvider.selectEntry(attribute);
				}
				target.add(tableContainer);
			}
		});

		checkBox.setEnabled(checkBoxEnabled);

		form.add(checkBox);
		this.add(form);
	}
}
