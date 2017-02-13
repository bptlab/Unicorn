/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.notification;

/**
 * This enumeration encapsulates the priorities for a notification rule.
 */
public enum NotificationMethod {
	GUI("low"), MAIL("high"), QUEUE("queue"), REST("rest");

	private String type;

	NotificationMethod(final String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		return this.type;
	}

}
