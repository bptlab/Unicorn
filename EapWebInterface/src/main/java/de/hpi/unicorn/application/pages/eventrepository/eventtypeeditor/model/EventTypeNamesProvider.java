/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor.model;

import java.util.List;

import org.apache.wicket.model.IModel;

import de.hpi.unicorn.event.EapEventType;

/**
 * This class is a {@link IModel} and provides the names of the event types in
 * the database.
 * 
 * @author micha
 */
public class EventTypeNamesProvider implements IModel<List<String>> {

	private static final long serialVersionUID = -8008561704069525479L;

	@Override
	public void detach() {

	}

	@Override
	public List<String> getObject() {
		return EapEventType.getAllTypeNames();
	}

	@Override
	public void setObject(final List<String> object) {

	}

}
