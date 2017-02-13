/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.form;

import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * This component creates a Button within a Panel, usable e.g. within a
 * DataTable. Button needs to have the wicket-id "button".
 */
public class DeleteButtonPanel extends Panel {

	private static final long serialVersionUID = 1L;

	public DeleteButtonPanel(final String id, final Button button) throws Exception {
		super(id);
		final Form<Void> form = new Form<Void>("form");
		if (button.getId().equals("button")) {
			form.add(button);
		} else {
			throw new Exception("Button-id needs to be 'button'!");
		}
		this.add(form);

	}

}
