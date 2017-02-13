/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.monitoring.eventviews;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;

import com.googlecode.wickedcharts.wicket6.highcharts.Chart;

import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.visualisation.EventView;

/**
 * This page displays the existing event views and allows to add new ones.
 */
@SuppressWarnings("serial")
public class EventViewPage extends AbstractEapPage {

	private AjaxButton addButton;
	private final Form<Void> form;
	public ListView listview;
	public AddViewModal addViewModal;

	IModel<List<EventView>> views = new LoadableDetachableModel<List<EventView>>() {
		@Override
		protected List<EventView> load() {
			return EventView.findAll();
		}
	};

	public EventViewPage() {
		super();

		// Create the modal window.
		this.addViewModal = new AddViewModal("addViewModal", this);
		this.add(this.addViewModal);

		this.form = new Form<Void>("form");
		this.form.add(this.addAddButton());
		this.add(this.form);

		this.addViews();
	}

	private Component addAddButton() {
		this.addButton = new AjaxButton("addViewButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				target.prependJavaScript("Wicket.Window.unloadConfirmation = false;");
				EventViewPage.this.addViewModal.show(target);
			}
		};
		return this.addButton;
	}

	@SuppressWarnings({ "unchecked" })
	private void addViews() {
		this.listview = new ListView("listview", this.views) {
			@Override
			protected void populateItem(final ListItem item) {
				// prepare and add view
				final EventView viewOptions = (EventView) item.getModelObject();
				final WebMarkupContainer view = new WebMarkupContainer("view");
				try {
					// build view
					final EventViewOptions options = new EventViewOptions(viewOptions);
					view.add(new Chart("view", options));
					view.add(new Label("sub", options.getExplanationString()));
				} catch (final Exception e) {
					e.printStackTrace();
					// if chart could not be build, display error message
					view.add(new Label("view", "This View could not be built."));
					view.add(new Label("sub", "Sorry for the inconvenience"));
				}
				item.add(view);
				// prepare and add removeButton
				final AjaxButton removeButton = new AjaxButton("removeViewButton") {
					private static final long serialVersionUID = 1L;

					@Override
					public void onSubmit(final AjaxRequestTarget target, final Form form) {
						viewOptions.remove();
						EventViewPage.this.views.detach();
						target.add(EventViewPage.this.listview.getParent());
					}
				};
				final Form<Void> removeform = new Form<Void>("removeform");
				removeform.add(removeButton);
				item.add(removeform);
			}
		};
		this.listview.setOutputMarkupId(true);

		this.add(this.listview);
	}

}
