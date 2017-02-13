/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.components.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.table.model.AbstractDataProvider;
import de.hpi.unicorn.persistence.Persistable;

public class EapProvider<E extends Persistable> extends AbstractDataProvider implements
		ISortableDataProvider<E, String> {

	private static final long serialVersionUID = 1L;
	protected List<E> entities;
	protected List<E> selectedEntities;
	protected ISortState<String> sortState = new SingleSortState<String>();

	public EapProvider(final List<E> entities) {
		this.entities = entities;
		this.selectedEntities = new ArrayList<E>();
	}

	public EapProvider(final List<E> entities, final List<E> selectedEntities) {
		this.entities = entities;
		this.selectedEntities = selectedEntities;
	}

	@Override
	public void detach() {
		// attributes = null;
	}

	public void removeItem(final E p) {
		this.entities.remove(p);
	}

	public void addItem(final E p) {
		this.entities.add(p);
	}

	@Override
	public Iterator<? extends E> iterator(final long first, final long count) {
		final List<E> data = this.entities;
		Collections.sort(data, new Comparator<E>() {
			@Override
			public int compare(final E e1, final E e2) {
				return (new Integer(e1.getID()).compareTo(e2.getID()));
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	@Override
	public IModel<E> model(final E entity) {
		return Model.of(entity);
	}

	@Override
	public long size() {
		return this.entities.size();
	}

	public List<E> getEntities() {
		return this.entities;
	}

	public List<E> getSelectedEntities() {
		return this.selectedEntities;
	}

	public void setEntities(final List<E> entityList) {
		this.entities = entityList;
	}

	@Override
	public ISortState<String> getSortState() {
		return this.sortState;
	}

	@Override
	public void selectEntry(final int entryId) {
		for (final E entity : this.entities) {
			if (entity.getID() == entryId) {
				this.selectEntry(entity);
				return;
			}
		}
	}

	public void selectEntry(final E entity) {
		this.selectedEntities.add(entity);
	}

	@Override
	public void deselectEntry(final int entryId) {
		for (final E entity : this.entities) {
			if (entity.getID() == entryId) {
				this.deselectEntry(entity);
				return;
			}
		}
	}

	public void deselectEntry(final E entity) {
		this.selectedEntities.remove(entity);
	}

	public void clearSelectedEntities() {
		this.selectedEntities.clear();
	}

	@Override
	public boolean isEntrySelected(final int entryId) {
		for (final E entity : this.selectedEntities) {
			if (entity.getID() == entryId) {
				return true;
			}
		}
		return false;
	}

	public boolean isEntrySelected(final E notification) {
		if (this.selectedEntities.contains(notification)) {
			return true;
		}
		return false;
	}

	public void deleteSelectedEntries() {
		for (final E entity : this.selectedEntities) {
			this.entities.remove(entity);
			entity.remove();
		}
	}

	public void selectAllEntries() {
		for (final E entity : this.entities) {
			this.selectedEntities.add(entity);
		}
	}

	@Override
	public Object getEntry(final int entryId) {
		// TODO Auto-generated method stub
		return null;
	}

}
