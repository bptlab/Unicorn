/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.table;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;

/**
 * This class renders a checkbox inside of a form. The checkbox informs the
 * given {@link AbstractDataProvider} about ajax state changes.
 * 
 * @author micha
 */
public class SelectEntryPanel extends Panel {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a checkbox inside of a form. The checkbox informs the
	 * given {@link AbstractDataProvider} about ajax state changes.
	 * 
	 * @param id
	 * @param entryId
	 * @param dataProvider
	 */
	public SelectEntryPanel(final String id, final int entryId, final AbstractDataProvider dataProvider) {
		super(id);
		final Form<Void> form = new Form<Void>("form");

		final CheckBox checkBox = new CheckBox("checkBoxID", Model.of(dataProvider.isEntrySelected(entryId)));
		checkBox.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				if (dataProvider.isEntrySelected(entryId)) {
					dataProvider.deselectEntry(entryId);
				} else if (!dataProvider.isEntrySelected(entryId)) {
					dataProvider.selectEntry(entryId);
				}
			}
		});

		form.add(checkBox);

		this.add(form);
	}
}
