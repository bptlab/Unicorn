/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.eventview;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;

import de.hpi.unicorn.application.components.tree.LabelTree;
import de.hpi.unicorn.application.components.tree.TreeExpansion;
import de.hpi.unicorn.application.components.tree.TreeExpansionModel;
import de.hpi.unicorn.application.components.tree.TreeProvider;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.collection.EventTransformationElement;
import de.hpi.unicorn.event.collection.EventTreeElement;

/**
 * This panel displays an event with its key information and its attributes in a
 * tree structure.
 */
public class EventViewPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private EapEvent event;
	private LabelTree<EventTreeElement> tree;
	private Label label;
	private Label timestamp;
	private final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

	public EventViewPanel(final String id, final EapEvent event) {
		super(id);
		this.setEvent(event);
	}

	public EventViewPanel(final String id) {
		super(id);

		// add eventtype
		this.label = new Label("eventType");
		this.label.setOutputMarkupId(true);
		this.add(this.label);

		// timestamp
		this.timestamp = new Label("timestamp");
		this.timestamp.setOutputMarkupId(true);
		this.add(this.timestamp);

		// hierarchical display of attributes
		this.tree = new LabelTree<EventTreeElement>("treeTable", new TreeProvider(
				new ArrayList<EventTreeElement<String>>(this.generateNodesOfEventTypeTree())), new TreeExpansionModel());
		TreeExpansion.get().expandAll();
		this.tree.setOutputMarkupId(true);
		this.add(this.tree);
	}

	public void setEvent(final EapEvent event) {
		this.event = event;

		// add eventtype
		this.label = new Label("eventType", event.getEventType().getTypeName());
		this.label.setOutputMarkupId(true);
		this.addOrReplace(this.label);

		// timestamp
		this.timestamp = new Label("timestamp", this.dateFormatter.format(event.getTimestamp()));
		this.timestamp.setOutputMarkupId(true);
		this.addOrReplace(this.timestamp);

		// hierarchical display of attributes
		this.tree = new LabelTree<EventTreeElement>("treeTable", new TreeProvider(this.generateNodesOfEventTypeTree()),
				new TreeExpansionModel());
		TreeExpansion.get().expandAll();
		this.tree.setOutputMarkupId(true);
		this.addOrReplace(this.tree);
	}

	protected ArrayList<EventTreeElement<String>> generateNodesOfEventTypeTree() {
		final ArrayList<EventTreeElement<String>> treeElements = new ArrayList<EventTreeElement<String>>();
		if (this.event != null) {
			// List<EventTransformationElement<String, Serializable>>
			// firstLevelValues = event.getValues().getTreeRootElements();
			final List<EventTransformationElement<String, Serializable>> firstLevelValues = this.event.getValueTree()
					.getTreeRootElements();
			for (final EventTransformationElement<String, Serializable> firstLevelValue : firstLevelValues) {
				EventTreeElement<String> rootElement;
				if (!firstLevelValue.hasChildren()) {
					String value;
					if (firstLevelValue.getValue() instanceof Date) {
						value = this.dateFormatter.format((Date) firstLevelValue.getValue());
					} else {
						value = firstLevelValue.getValue().toString();
					}
					rootElement = new EventTreeElement<String>(firstLevelValue.getKey() + " : " + value);
					treeElements.add(rootElement);
				} else {
					rootElement = new EventTreeElement<String>(firstLevelValue.getKey());
					treeElements.add(rootElement);
					this.fillTreeLevel(rootElement, firstLevelValue.getChildren());
				}
			}
		}
		return treeElements;
	}

	private void fillTreeLevel(final EventTreeElement<String> parent,
			final List<EventTransformationElement<String, Serializable>> children) {
		for (final EventTransformationElement<String, Serializable> newValue : children) {
			EventTreeElement<String> newElement;
			if (!newValue.hasChildren()) {
				String value;
				if (newValue.getValue() instanceof Date) {
					value = this.dateFormatter.format((Date) newValue.getValue());
				} else {
					value = newValue.getValue().toString();
				}
				newElement = new EventTreeElement<String>(parent, newValue.getKey() + " : " + value);
			} else {
				newElement = new EventTreeElement<String>(parent, newValue.getKey());
				this.fillTreeLevel(newElement, newValue.getChildren());
			}
		}
	}

}
