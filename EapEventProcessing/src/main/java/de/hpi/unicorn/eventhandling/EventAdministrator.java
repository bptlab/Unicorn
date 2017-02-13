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
import de.hpi.unicorn.event.EapEventType;

/**
 * Administration of event types and events
 * 
 * @author Robert Breske
 * 
 */
public interface EventAdministrator {

	/**
	 * Permanently remove an event from the platform.
	 * 
	 * @param event
	 * @return the removed event
	 */
	public EapEvent removeEvent(EapEvent event);

	/**
	 * Permanently remove and eventType and consequently all its associated
	 * events from the platform.
	 * 
	 * @param eventType
	 * @return the removed event type
	 */
	public EapEventType removeEventType(EapEventType eventType);

	/**
	 * Register a new event type.
	 * 
	 * @param eventType
	 * @return the registered event type
	 */
	public EapEventType importEventType(EapEventType eventType);

	/**
	 * See all registered event types
	 * 
	 * @return all registered event types
	 */
	public List<EapEventType> getAllEventTypes();

}
