/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.eventhandling;

import java.util.List;

import de.hpi.unicorn.event.EapEvent;

/**
 * Declares methods for importing events into the platform. All implementations
 * have to be threadsafe.
 * 
 * @author Robert Breske
 * 
 */
public interface EventImporter {

	/**
	 * Import a list of events into the platform
	 * 
	 * @param events
	 * @return the imported events
	 */
	public List<EapEvent> importEvents(List<EapEvent> events);

	/**
	 * See {@link send}.
	 */
	public EapEvent importEvent(EapEvent event);

	/**
	 * Import an event to the platform.
	 * 
	 * @param event
	 * @param rawSending
	 *            send without triggering any further actions
	 * @return the imported event
	 */
	public EapEvent importEvent(EapEvent event, boolean rawSending);

	/**
	 * Import events through an adapter, which is scheduled by event importer.
	 * 
	 * @param eventAdapter
	 */
	public void importEventsWithSchedule(Runnable eventAdapter); // TODO check
																	// with
																	// EventAdapter

}