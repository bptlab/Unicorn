/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.link.ExternalLink;

public class FaviconLink extends ExternalLink {

	private static final long serialVersionUID = 1L;

	public FaviconLink(final String id, final String href) {
		super(id, href);
		this.add(new AttributeModifier("type", "image/x-icon"));
		this.add(new AttributeModifier("rel", "shortcut icon"));
	}

}
