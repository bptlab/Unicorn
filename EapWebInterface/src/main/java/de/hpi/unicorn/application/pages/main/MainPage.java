/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.main;

import org.apache.wicket.request.mapper.parameter.PageParameters;

import de.hpi.unicorn.application.pages.AbstractEapPage;

public class MainPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	public MainPage() {
		super();

		this.buildMainLayout();
	}

	public MainPage(final PageParameters pageParameters) {
		this();

		if (pageParameters.get("successFeedback") != null) {
			this.getFeedbackPanel().success(pageParameters.get("successFeedback"));
		} else if (pageParameters.get("errorFeedback") != null) {
			this.getFeedbackPanel().error(pageParameters.get("errorFeedback"));
		}
	}

	private void buildMainLayout() {

		// not logged-in : show image-Carousel
		// ImageModel imageModel = new ImageModel(this);
		// Carousel carousel = new Carousel("carousel", imageModel);
		// add(carousel);
	}

}
