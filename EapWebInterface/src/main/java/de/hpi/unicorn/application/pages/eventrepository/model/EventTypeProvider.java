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

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.IFilterStateLocator;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;

/**
 * This class is the provider for {@link EapEventType}s. A filter can be
 * specified to return only some event types.
 * 
 * @author micha
 */
public class EventTypeProvider extends AbstractDataProvider implements ISortableDataProvider<EapEventType, String>,
		IFilterStateLocator {

	private static final long serialVersionUID = 1L;
	private static List<EapEventType> eventTypes;
	private final ISortState sortState = new SingleSortState();
	private EventTypeFilter eventTypeFilter = new EventTypeFilter();
	private final List<EapEventType> selectedEventTypes;

	/**
	 * Constructor for providing {@link EapEventType}s.
	 */
	public EventTypeProvider() {
		EventTypeProvider.eventTypes = this.filterEventTypes(EapEventType.findAll(), this.eventTypeFilter);
		this.selectedEventTypes = new ArrayList<EapEventType>();
	}

	@Override
	public void detach() {
		// events = null;
	}

	@Override
	public Iterator<? extends EapEventType> iterator(final long first, final long count) {
		final List<EapEventType> data = EventTypeProvider.eventTypes;
		Collections.sort(data, new Comparator<EapEventType>() {
			@Override
			public int compare(final EapEventType e1, final EapEventType e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	private List<EapEventType> filterEventTypes(final List<EapEventType> eventTypesToFilter,
			final EventTypeFilter eventTypeFilter) {
		final List<EapEventType> returnedEventTypes = new ArrayList<EapEventType>();
		for (final EapEventType eventType : eventTypesToFilter) {
			if (eventTypeFilter.match(eventType)) {
				returnedEventTypes.add(eventType);
			}
		}
		return returnedEventTypes;
	}

	@Override
	public IModel<EapEventType> model(final EapEventType eventType) {
		return Model.of(eventType);
	}

	@Override
	public long size() {
		return EventTypeProvider.eventTypes.size();
	}

	public static List<EapEventType> getEventTypes() {
		return EventTypeProvider.eventTypes;
	}

	public static void setEventTypes(final List<EapEventType> eventTypeList) {
		EventTypeProvider.eventTypes = eventTypeList;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public Object getFilterState() {
		return this.eventTypeFilter;
	}

	@Override
	public void setFilterState(final Object state) {
		this.eventTypeFilter = (EventTypeFilter) state;
	}

	public EventTypeFilter getEventTypeFilter() {
		return this.eventTypeFilter;
	}

	public void setEventTypeFilter(final EventTypeFilter eventTypeFilter) {
		this.eventTypeFilter = eventTypeFilter;
		EventTypeProvider.eventTypes = this.filterEventTypes(EapEventType.findAll(), eventTypeFilter);
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final Object element : EventTypeProvider.eventTypes) {
			final EapEventType eventType = (EapEventType) element;
			if (eventType.getID() == entryId) {
				this.selectedEventTypes.add(eventType);
				return;
			}
		}
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final EapEventType eventType : EventTypeProvider.eventTypes) {
			if (eventType.getID() == entryId) {
				this.selectedEventTypes.remove(eventType);
				return;
			}
		}
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final EapEventType eventType : this.selectedEventTypes) {
			if (eventType.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final EapEventType eventType : this.selectedEventTypes) {
			EventTypeProvider.eventTypes.remove(eventType);
			Broker.getEventAdministrator().removeEventType(eventType);
		}
	}

	public void selectAllEntries() {
		for (final EapEventType eventType : EventTypeProvider.eventTypes) {
			this.selectedEventTypes.add(eventType);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		for (final EapEventType eventType : EventTypeProvider.eventTypes) {
			if (eventType.getID() == entryId) {
				return eventType;
			}
		}
		return null;
	}

}
