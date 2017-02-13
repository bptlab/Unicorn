/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import java.util.Date;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

public abstract class EventAdapter {

	String name = new String();

	public EventAdapter(final String name) {
		this.name = name;
	}

	public void start(final long interval) {
		try {
			// Grab the Scheduler instance from the Factory
			final org.quartz.Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();

			// define the job and tie it to our NokiaHereAdapter class
			final JobDetail jd = new JobDetail(this.name, Scheduler.DEFAULT_GROUP, AdapterJob.class);

			// triggers all 120 seconds the execution of execution, never ends
			final SimpleTrigger simpleTrigger = new SimpleTrigger(this.name, Scheduler.DEFAULT_GROUP, new Date(), null,
					SimpleTrigger.REPEAT_INDEFINITELY, interval * 1000);

			// Tell quartz to schedule the job using our trigger
			scheduler.scheduleJob(jd, simpleTrigger);
			scheduler.start();
		} catch (final SchedulerException se) {
			se.printStackTrace();
		}
	};

	public boolean stop() {
		try {
			// Grab the Scheduler instance from the Factory
			final Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
			scheduler.deleteJob(this.name, Scheduler.DEFAULT_GROUP);
			return true;
		} catch (final SchedulerException se) {
			se.printStackTrace();
		}
		return false;
	}

	abstract void trigger();
	// abstract void registerEventType();

}
