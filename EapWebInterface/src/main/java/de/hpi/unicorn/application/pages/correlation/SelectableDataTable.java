/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.correlation;

import java.util.List;

import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.ISortableDataProvider;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;

public class SelectableDataTable<T, S> extends DefaultDataTable<T, S> {

	public SelectableDataTable(final String id, final List<? extends IColumn<T, S>> columns,
			final ISortableDataProvider<T, S> dataProvider, final int rowsPerPage) {
		super(id, columns, dataProvider, rowsPerPage);
	}

	@Override
	protected Item<T> newRowItem(final String id, final int index, final IModel<T> model) {
		final Item<T> rowItem = new Item<T>(id, index, model);
		rowItem.add(

		new AjaxEventBehavior("onclick") {
			private static final long serialVersionUID = 6720512493017210281L;

			@Override
			protected void onEvent(final AjaxRequestTarget target) {
				// callback or do some stuff
			}

		});
		return rowItem;

	}

}
