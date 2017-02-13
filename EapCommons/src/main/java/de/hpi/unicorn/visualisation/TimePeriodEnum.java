/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.visualisation;

import java.util.Calendar;
import java.util.Date;

/**
 * This enumeration encapsulates the possible time periods for event views.
 */
public enum TimePeriodEnum {
	ONEHOUR("last hour", 3600), ONEDAY("last day", 86400), ONEMONTH("last month", 2592000), ONEYEAR("last year",
			31536000), INF("infinite", 0);

	private String type;
	private int seconds;

	TimePeriodEnum(final String type, final int seconds) {
		this.type = type;
		this.seconds = seconds;
	}

	@Override
	public String toString() {
		return this.type;
	}

	public int getTime() {
		return this.seconds;
	}

	/**
	 * Calculates a date that lies as far back from now as the time period.
	 * Between the returned date and now lies the configured time period.
	 * 
	 * @return date
	 */
	public Date getStartTime() {
		final Calendar cal = Calendar.getInstance();
		cal.add(Calendar.SECOND, -this.seconds);
		return cal.getTime();
	}

}
