/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.adapter;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class AdapterJob implements Job {

	public AdapterJob() {
		// do not remove this empty constructor, required for Quartz Jobs to
		// work properly
	}

	@Override
	public void execute(final JobExecutionContext context) throws JobExecutionException {
		final String adapterName = context.getJobDetail().getName();
		System.out.println("Executing job '" + adapterName + "'");

		final AdapterManager am = AdapterManager.getInstance();

		final EventAdapter adapter = am.get(adapterName);
		adapter.trigger();
	}

}
