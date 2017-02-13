package de.hpi.unicorn.application.pages.input.replayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.sort.ISortState;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.extensions.markup.html.repeater.util.SingleSortState;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;

public class ProgressProvider implements ISortableDataProvider<Date, String> {

	private static final long serialVersionUID = -356494776994033148L;
	protected List<Date> entities;
	protected ISortState<String> sortState = new SingleSortState<String>();

	public ProgressProvider() {
		this.entities = new ArrayList<Date>();
	}

	public ProgressProvider(List<Date> entities) {
		this.entities = entities;
	}

	@Override
	public Iterator<Date> iterator(long first, long count) {
		List<Date> data = entities;
		Collections.sort(data, new Comparator<Date>() {
			public int compare(Date e1, Date e2) {
				// descending: latest first
				return e2.compareTo(e1);
			}
		});
		return data.subList((int) first, (int) Math.min(first + count, data.size())).iterator();
	}

	@Override
	public IModel<Date> model(Date entity) {
		return Model.of(entity);
	}

	@Override
	public long size() {
		return entities.size();
	}

	@Override
	public void detach() {
	}

	@Override
	public ISortState<String> getSortState() {
		return sortState;
	}

	public void setEntities(List<Date> entities) {
		this.entities = entities;
	}

	public void clearEntities() {
		this.entities = new ArrayList<Date>();
	}

	public void removeEntity(Date creationDate) {
		this.entities.remove(creationDate);
	}
}
