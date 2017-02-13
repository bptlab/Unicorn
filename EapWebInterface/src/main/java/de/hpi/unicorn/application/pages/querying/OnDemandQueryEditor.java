/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.querying;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;

import com.espertech.esper.client.EPStatementException;

import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.tree.LabelTreeElement;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

/**
 * This page enables the creation and modification of On-Demand @see
 * QueryWrapper
 */
public class OnDemandQueryEditor extends QueryEditor {

	private static final long serialVersionUID = 1L;
	private static final String ON_DEMAND_QUERY_HELP_TEXT = "On-Demand Queries are always asked from a Window"
			+ QueryEditor.lineBreak + QueryEditor.lineBreak + "Example-Query:" + QueryEditor.lineBreak
			+ "SELECT ValueName, Timestamp" + QueryEditor.lineBreak + "FROM EventTypeWindow" + QueryEditor.lineBreak
			+ "WHERE ValueName = 'ValueX'";
	private BlockingAjaxButton executeQueryButton;
	private AjaxButton editQueryButton, deleteQueryButton, saveQueryButton;

	public OnDemandQueryEditor() {
		super();
		this.helpText = OnDemandQueryEditor.ON_DEMAND_QUERY_HELP_TEXT;

		this.updateQueryListChoice();
		this.buildMainLayout();
		this.buildFinalLayout();
	}

	private void updateQueryListChoice() {
		this.queries = QueryWrapper.getAllOnDemandQueries();
		if (!this.queries.isEmpty()) {
			this.selectedQuery = this.queries.get(0);
		}
	}

	@SuppressWarnings("serial")
	private void buildFinalLayout() {

		this.saveQueryButton = new AjaxButton("saveQueryButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				try {
					final String queryTitle = OnDemandQueryEditor.this.queryNameTextField.getValue();
					if (queryTitle.isEmpty()) {
						OnDemandQueryEditor.this.getFeedbackPanel().error("Please specify a name for the query!");
						target.add(OnDemandQueryEditor.this.getFeedbackPanel());
						return;
					}

					if (OnDemandQueryEditor.this.selectedQuery == null) {
						final QueryWrapper query = new QueryWrapper(queryTitle, OnDemandQueryEditor.this.queryString,
								QueryTypeEnum.ONDEMAND);
						query.validate();
						query.save();
						OnDemandQueryEditor.this.queries.add(query);
					} else {
						OnDemandQueryEditor.this.selectedQuery.setQuery(OnDemandQueryEditor.this.queryString);
						OnDemandQueryEditor.this.selectedQuery.validate();
						OnDemandQueryEditor.this.selectedQuery.save();
					}
					OnDemandQueryEditor.this.textFieldDefaultValues.setQueryNameTextField("");
					OnDemandQueryEditor.this.queryString = "";
					OnDemandQueryEditor.this.updateQueryListChoice();
					OnDemandQueryEditor.this.queryListChoice.setChoices(OnDemandQueryEditor.this.queries);
					target.add(OnDemandQueryEditor.this.queryListChoice);
					target.add(OnDemandQueryEditor.this.queryNameTextField);
					target.add(OnDemandQueryEditor.this.queryTextArea);
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				} catch (final EPStatementException e) {
					OnDemandQueryEditor.this.getFeedbackPanel().error(e.getMessage());
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				} catch (final RuntimeException e) {
					OnDemandQueryEditor.this.getFeedbackPanel().error(e.getMessage());
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				}
			}
		};
		this.layoutForm.add(this.saveQueryButton);

		this.editQueryButton = new AjaxButton("editQueryButton", this.layoutForm) {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if (OnDemandQueryEditor.this.selectedQuery == null) {
					OnDemandQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				} else {
					OnDemandQueryEditor.this.textFieldDefaultValues
							.setQueryNameTextField(OnDemandQueryEditor.this.selectedQuery.getTitle());
					OnDemandQueryEditor.this.queryString = OnDemandQueryEditor.this.selectedQuery.getQueryString();
					OnDemandQueryEditor.this.updateQueryListChoice();
					target.add(OnDemandQueryEditor.this.queryNameTextField);
					target.add(OnDemandQueryEditor.this.queryTextArea);
				}
			}
		};
		this.layoutForm.add(this.editQueryButton);

		this.deleteQueryButton = new AjaxButton("deleteQueryButton", this.layoutForm) {
			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if (OnDemandQueryEditor.this.selectedQuery == null) {
					OnDemandQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				} else {
					final QueryWrapper toBeRemoved = OnDemandQueryEditor.this.selectedQuery;
					OnDemandQueryEditor.this.queries.remove(OnDemandQueryEditor.this.selectedQuery);
					OnDemandQueryEditor.this.selectedQuery = null;
					toBeRemoved.remove();
					OnDemandQueryEditor.this.updateQueryListChoice();
					OnDemandQueryEditor.this.queryListChoice.setChoices(OnDemandQueryEditor.this.queries);
					target.add(OnDemandQueryEditor.this.queryListChoice);
				}
			}
		};
		this.layoutForm.add(this.deleteQueryButton);

		// executes the on-demand query on the events in the windows
		this.executeQueryButton = new BlockingAjaxButton("executeQueryButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if (OnDemandQueryEditor.this.selectedQuery == null) {
					OnDemandQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(OnDemandQueryEditor.this.getFeedbackPanel());
				} else {
					// System.out.println("Number of events in runtime before query: "
					// + epAdapter.getEsperRuntime().getNumEventsEvaluated());
					OnDemandQueryEditor.this.queryResult = OnDemandQueryEditor.this.selectedQuery.execute();
					target.add(OnDemandQueryEditor.this.queryResultTextArea);
				}
			}
		};
		this.layoutForm.add(this.executeQueryButton);
	}

	/**
	 * creates a preview of the registered events and their attributes for
	 * easier query formulation
	 */
	@Override
	protected ArrayList<LabelTreeElement<String>> generateNodesOfEventTypeTree() {
		int count = 0;
		final ArrayList<LabelTreeElement<String>> treeElements = new ArrayList<LabelTreeElement<String>>();
		final List<EapEventType> eventTypes = EapEventType.findAll();
		for (final EapEventType eventType : eventTypes) {
			final LabelTreeElement<String> eventTypeRootElement = new LabelTreeElement<String>(count++,
					eventType.getTypeName() + "Window");
			treeElements.add(eventTypeRootElement);
			final List<LabelTreeElement<String>> rootElements = this.convertToTreeElements(count,
					eventType.getRootLevelValueTypes());
			for (final LabelTreeElement<String> element : rootElements) {
				element.setParent(eventTypeRootElement);
			}
		}
		return treeElements;
	}

	// /**
	// * generates a preview of windows registered at esper for an easier query
	// creation
	// */
	// @Override
	// protected ArrayList<TypeTreeNode> generateNodesOfEventTypeTree() {
	// ArrayList<TypeTreeNode> treeElements = new ArrayList<TypeTreeNode>();
	// String[] windowNames = epAdapter.getWindowNames();
	// for (String windowName : windowNames) {
	// TypeTreeNode eventTypeRootElement = new TypeTreeNode(windowName);
	// treeElements.add(eventTypeRootElement);
	//
	// String eventTypeName = windowName.replace("Window", "");
	// EapEventType eventType = EapEventType.findByTypeName(eventTypeName);
	// List<TypeTreeNode> rootAttributes = new
	// ArrayList<TypeTreeNode>(eventType.getRootLevelValueTypes());
	// for (TypeTreeNode rootAttribute : rootAttributes) {
	// rootAttribute.setParent(eventTypeRootElement);
	// }
	// }
	// return treeElements;
	//
	// }
}
