/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;

import de.hpi.unicorn.application.components.table.EapProvider;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.eventhandling.Broker;

/**
 * This class is the provider for {@link EapEvent}s. A filter can be specified
 * to return only some events.
 * 
 * @author micha
 */
public class EventProvider extends EapProvider<EapEvent> implements ISortableDataProvider<EapEvent, String>,
		IFilterStateLocator {

	private static final long serialVersionUID = 1L;
	private EventFilter eventFilter = new EventFilter();

	/**
	 * Constructor for providing {@link EapEvent}s.
	 */
	public EventProvider() {
		super(EapEvent.findAllUI());
		this.entities = this.filterEvents(EapEvent.findAllUI(), this.eventFilter);
	}

	@Override
	public void deleteSelectedEntries() {
		for (final EapEvent entity : this.selectedEntities) {
			this.entities.remove(entity);
			Broker.getEventAdministrator().removeEvent(entity);
		}
	}

	private List<EapEvent> filterEvents(final List<EapEvent> eventsToFilter, final EventFilter eventFilter) {
		final List<EapEvent> returnedEvents = new ArrayList<EapEvent>();
		for (final EapEvent event : eventsToFilter) {
			if (eventFilter.match(event)) {
				returnedEvents.add(event);
			}
		}
		return returnedEvents;
	}

	@Override
	public Object getFilterState() {
		return this.eventFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.eventFilter = (EventFilter) state;
	}

	public EventFilter getEventFilter() {
		return this.eventFilter;
	}

	public void setEventFilter(final EventFilter eventFilter) {
		this.eventFilter = eventFilter;
		this.entities = this.filterEvents(EapEvent.findAllUI(), eventFilter);
		this.selectedEntities = new ArrayList<EapEvent>();
	}

	public void setSecondEventFilter(final EventFilter eventFilter) {
		this.entities = this.filterEvents(EapEvent.findAllUI(), this.eventFilter);
		this.entities = this.filterEvents(this.entities, eventFilter);
		this.selectedEntities = new ArrayList<EapEvent>();
	}

	@Override
	public Iterator<? extends EapEvent> iterator(final long first, final long count) {
		final List<EapEvent> data = this.entities;
		Collections.sort(data, new Comparator<EapEvent>() {
			@Override
			public int compare(final EapEvent e1, final EapEvent e2) {
				return (new Integer(e2.getID()).compareTo(e1.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}
}
