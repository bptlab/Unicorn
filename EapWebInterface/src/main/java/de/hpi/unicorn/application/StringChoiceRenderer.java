/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application;

import org.apache.wicket.markup.html.form.IChoiceRenderer;

public class StringChoiceRenderer implements IChoiceRenderer<String> {

	@Override
	public Object getDisplayValue(final String t) {
		return t;
	}

	@Override
	public String getIdValue(final String t, final int i) {
		return t;
	}

}
