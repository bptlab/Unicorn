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

import com.espertech.esper.client.EPException;

import de.hpi.unicorn.application.UNICORNApplication;
import de.hpi.unicorn.application.components.form.BlockingAjaxButton;
import de.hpi.unicorn.application.components.tree.LabelTreeElement;
import de.hpi.unicorn.esper.StreamProcessingAdapter;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.notification.NotificationRuleForQuery;
import de.hpi.unicorn.query.QueryTypeEnum;
import de.hpi.unicorn.query.QueryWrapper;

/**
 * This page enables the creation and modification of Live @see QueryWrapper
 */
public class LiveQueryEditor extends QueryEditor {

	private static final long serialVersionUID = 1L;
	private static final String LIVE_QUERY_HELP_TEXT = "Live Queries are either asked from the eventtypes"
			+ QueryEditor.lineBreak + "or defined by a pattern. " + QueryEditor.lineBreak + QueryEditor.lineBreak
			+ "Example-Query:" + QueryEditor.lineBreak + "SELECT ValueName, Timestamp" + QueryEditor.lineBreak
			+ "FROM EventType" + QueryEditor.lineBreak + "WHERE eventType = 'ValueNameX'" + QueryEditor.lineBreak
			+ QueryEditor.lineBreak + "Example-Query for Patterns:" + QueryEditor.lineBreak
			+ "SELECT A.Value1, B.Value2 Timestamp" + QueryEditor.lineBreak + "FROM Pattern[ every A=EventType1 "
			+ QueryEditor.lineBreak + "-> B=EventType2(A.Value1 = B.Value1)]" + QueryEditor.lineBreak
			+ QueryEditor.lineBreak + "Other useful constructs might be: and, or, not" + QueryEditor.lineBreak;

	private BlockingAjaxButton showQueryLogButton;
	private AjaxButton editQueryButton, deleteQueryButton, saveQueryButton, deleteQueriesWithoutNotificationRuleButton;

	public LiveQueryEditor() {

		super();
		this.helpText = LiveQueryEditor.LIVE_QUERY_HELP_TEXT;

		this.updateQueryListChoice();
		this.buildMainLayout();
		this.buildFinalLayout();
	}

	private void updateQueryListChoice() {
		this.queries = QueryWrapper.getAllLiveQueries();
	}

	private void buildFinalLayout() {

		this.saveQueryButton = new AjaxButton("saveQueryButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				try {
					final StreamProcessingAdapter epAdapter = ((UNICORNApplication) this.getApplication()).getEp();
					final String queryTitle = LiveQueryEditor.this.queryNameTextField.getValue();
					if (queryTitle.isEmpty()) {
						LiveQueryEditor.this.getFeedbackPanel().error("Please specify a name for the query!");
						target.add(LiveQueryEditor.this.getFeedbackPanel());
						return;
					}
					final QueryWrapper liveQuery = new QueryWrapper(queryTitle, LiveQueryEditor.this.queryString,
							QueryTypeEnum.LIVE);
					epAdapter.addLiveQuery(liveQuery);
					LiveQueryEditor.this.queries.add(liveQuery);
					liveQuery.setQuery(LiveQueryEditor.this.queryString);
					liveQuery.save();
					// set input fields to null
					LiveQueryEditor.this.textFieldDefaultValues.setQueryNameTextField("");
					LiveQueryEditor.this.queryString = "";
					LiveQueryEditor.this.updateQueryListChoice();
					LiveQueryEditor.this.queryListChoice.setChoices(LiveQueryEditor.this.queries);
					target.add(LiveQueryEditor.this.queryListChoice);
					target.add(LiveQueryEditor.this.queryNameTextField);
					target.add(LiveQueryEditor.this.queryTextArea);
				} catch (final EPException e) {
					LiveQueryEditor.this.getFeedbackPanel().error(e.getMessage());
					target.add(LiveQueryEditor.this.getFeedbackPanel());
				}
			}
		};
		this.layoutForm.add(this.saveQueryButton);

		this.editQueryButton = new AjaxButton("editQueryButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if (LiveQueryEditor.this.selectedQuery == null) {
					LiveQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(LiveQueryEditor.this.getFeedbackPanel());
				} else {
					LiveQueryEditor.this.textFieldDefaultValues
							.setQueryNameTextField(LiveQueryEditor.this.selectedQuery.getTitle());
					LiveQueryEditor.this.queryString = LiveQueryEditor.this.selectedQuery.getQueryString();
					LiveQueryEditor.this.updateQueryListChoice();
					target.add(LiveQueryEditor.this.queryNameTextField);
					target.add(LiveQueryEditor.this.queryTextArea);
					target.add(LiveQueryEditor.this.queryListChoice);
				}
			}
		};
		this.layoutForm.add(this.editQueryButton);

		this.deleteQueryButton = new AjaxButton("deleteQueryButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				if (LiveQueryEditor.this.selectedQuery == null) {
					LiveQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(LiveQueryEditor.this.getFeedbackPanel());
				} else {
					final QueryWrapper query = LiveQueryEditor.this.selectedQuery;
					LiveQueryEditor.this.queries.remove(LiveQueryEditor.this.selectedQuery);
					LiveQueryEditor.this.selectedQuery = null;
					query.remove();
					LiveQueryEditor.this.updateQueryListChoice();
					LiveQueryEditor.this.queryListChoice.setChoices(LiveQueryEditor.this.queries);
					target.add(LiveQueryEditor.this.queryListChoice);
				}
			}
		};
		this.layoutForm.add(this.deleteQueryButton);

		this.deleteQueriesWithoutNotificationRuleButton = new AjaxButton("deleteQueriesWithoutNotificationRuleButton") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				int numberOfRemovedQueries = 0;
				LiveQueryEditor.this.selectedQuery = null;
				final ArrayList<QueryWrapper> tempQueries = new ArrayList<QueryWrapper>(LiveQueryEditor.this.queries);
				for (final QueryWrapper query : tempQueries) {
					final List<NotificationRuleForQuery> rules = query.getNotificationRulesForQuery();
					if (rules == null || rules.isEmpty()) {
						LiveQueryEditor.this.queries.remove(query);
						query.remove();
						numberOfRemovedQueries++;
					}
				}
				LiveQueryEditor.this.updateQueryListChoice();
				LiveQueryEditor.this.queryListChoice.setChoices(LiveQueryEditor.this.queries);
				target.add(LiveQueryEditor.this.queryListChoice);
				switch (numberOfRemovedQueries) {
				case 0:
					LiveQueryEditor.this.getFeedbackPanel().error("No query has been deleted.");
					break;
				case 1:
					LiveQueryEditor.this.getFeedbackPanel().success("1 query has been deleted.");
					break;
				default:
					LiveQueryEditor.this.getFeedbackPanel().success(
							numberOfRemovedQueries + " queries have been deleted.");
				}
				target.add(LiveQueryEditor.this.getFeedbackPanel());
			}
		};
		this.layoutForm.add(this.deleteQueriesWithoutNotificationRuleButton);

		this.showQueryLogButton = new BlockingAjaxButton("showQueryLogButton", this.layoutForm) {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				if (LiveQueryEditor.this.selectedQuery == null) {
					LiveQueryEditor.this.getFeedbackPanel().error("No query selected.");
					target.add(LiveQueryEditor.this.getFeedbackPanel());
				} else {
					super.onSubmit(target, form);
					LiveQueryEditor.this.queryResult = LiveQueryEditor.this.selectedQuery.getPrintableLog();
					target.add(LiveQueryEditor.this.queryResultTextArea);
				}
			}
		};
		this.layoutForm.add(this.showQueryLogButton);
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
					eventType.getTypeName());
			treeElements.add(eventTypeRootElement);
			final List<LabelTreeElement<String>> rootElements = this.convertToTreeElements(count,
					eventType.getRootLevelValueTypes());
			for (final LabelTreeElement<String> element : rootElements) {
				element.setParent(eventTypeRootElement);
			}
		}
		return treeElements;
	}

}
