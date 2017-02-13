/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.form;

/**
 * The enum provides text emphasis classes for Bootstrap.
 * 
 * @author micha
 */
public enum BootStrapTextEmphasisClass {

	Muted("muted"), Warning("text-warning"), Error("text-error"), Info("text-info"), Success("text-success");

	private String classValue;

	BootStrapTextEmphasisClass(final String classValue) {
		this.classValue = classValue;
	}

	public String getClassValue() {
		return this.classValue;
	}

}
