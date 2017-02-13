/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.bpmn;

import de.hpi.unicorn.application.pages.AbstractEapPage;

/**
 * This page displays the {@link BPMNProcessUploadPanel}.
 * 
 * @author micha
 */
public class BPMNProcessUpload extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor for a page, which displays the {@link BPMNProcessUploadPanel}
	 * .
	 */
	public BPMNProcessUpload() {
		super();

		this.add(new BPMNProcessUploadPanel("bpmnProcessUploadPanel", this));
	}
}
