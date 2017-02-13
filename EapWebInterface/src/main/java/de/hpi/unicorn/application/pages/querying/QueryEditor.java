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
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.io.IClusterable;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.components.tree.LabelTree;
import de.hpi.unicorn.application.components.tree.LabelTreeElement;
import de.hpi.unicorn.application.components.tree.LabelTreeExpansion;
import de.hpi.unicorn.application.components.tree.LabelTreeExpansionModel;
import de.hpi.unicorn.application.components.tree.LabelTreeProvider;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.query.QueryWrapper;

/**
 * This class is a super class for the creation and modification of Live and
 * On-Demand @see QueryWrapper
 */
public abstract class QueryEditor extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	protected List<QueryWrapper> queries;
	protected Form<Void> layoutForm;
	protected TextFieldDefaultValues textFieldDefaultValues;
	protected TextField<String> queryNameTextField;
	protected AjaxButton helpButton;
	protected ListChoice<QueryWrapper> queryListChoice;
	protected TextArea<String> queryTextArea;
	protected String helpText, queryString;
	protected QueryWrapper selectedQuery;
	protected String queryResult;
	protected TextArea<String> queryResultTextArea;
	protected static String lineBreak = System.getProperty("line.separator");
	private QueryEditorHelpModal helpModal;

	public QueryEditor() {
		super();
	}

	protected void buildMainLayout() {

		this.layoutForm = new WarnOnExitForm("layoutForm");
		this.add(this.layoutForm);

		this.textFieldDefaultValues = new TextFieldDefaultValues();
		this.setDefaultModel(new CompoundPropertyModel<TextFieldDefaultValues>(this.textFieldDefaultValues));

		this.queryNameTextField = new TextField<String>("queryNameTextField");
		this.queryNameTextField.setOutputMarkupId(true);
		this.layoutForm.add(this.queryNameTextField);

		// Create the modal window.
		this.helpModal = new QueryEditorHelpModal("helpModal", this.helpText);
		this.add(this.helpModal);

		this.helpButton = new AjaxButton("helpButton", this.layoutForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				super.onSubmit(target, form);
				QueryEditor.this.helpModal.show(target);
			}

		};
		this.layoutForm.add(this.helpButton);

		this.queryListChoice = new ListChoice<QueryWrapper>("queryListChoice", new PropertyModel<QueryWrapper>(this,
				"selectedQuery"), this.queries) {
			private static final long serialVersionUID = 1L;

			@Override
			protected CharSequence getDefaultChoice(final String selected) {
				return "";
			}
		};
		this.queryListChoice.setOutputMarkupId(true);
		this.queryListChoice.setMaxRows(5);
		this.layoutForm.add(this.queryListChoice);

		this.queryTextArea = new TextArea<String>("queryTextArea", new PropertyModel<String>(this, "queryString"));
		this.queryTextArea.setOutputMarkupId(true);
		this.layoutForm.add(this.queryTextArea);

		this.buildEventTypeTree();
		this.buildQueryResultTextArea();
	}

	private void buildEventTypeTree() {
		final LabelTreeExpansionModel<String> expansionModel = new LabelTreeExpansionModel<String>();
		final LabelTree<LabelTreeElement<String>> eventTypeTree = new LabelTree<LabelTreeElement<String>>(
				"eventTypeTree", new LabelTreeProvider<String>(this.generateNodesOfEventTypeTree()), expansionModel);
		((LabelTreeExpansion<String>) expansionModel.getObject()).collapseAll();
		this.layoutForm.add(eventTypeTree);
	}

	protected abstract ArrayList<LabelTreeElement<String>> generateNodesOfEventTypeTree();

	protected List<LabelTreeElement<String>> convertToTreeElements(int count, final List<TypeTreeNode> attributes) {
		final List<LabelTreeElement<String>> treeElements = new ArrayList<LabelTreeElement<String>>();
		for (final TypeTreeNode attribute : attributes) {
			final LabelTreeElement<String> element = new LabelTreeElement<String>(count++, attribute.toString());
			if (attribute.hasChildren()) {
				element.setChildren(this.convertToTreeElements(count, attribute.getChildren()));
			}
			treeElements.add(element);
		}
		return treeElements;
	}

	private void buildQueryResultTextArea() {
		this.queryResultTextArea = new TextArea<String>("queryResultTextArea", new PropertyModel<String>(this,
				"queryResult"));
		this.queryResultTextArea.setOutputMarkupId(true);
		this.layoutForm.add(this.queryResultTextArea);
	}

	@SuppressWarnings("serial")
	class TextFieldDefaultValues implements IClusterable {
		public String queryNameTextField;

		public String getQueryNameTextField() {
			return this.queryNameTextField;
		}

		public void setQueryNameTextField(final String queryNameTextField) {
			this.queryNameTextField = queryNameTextField;
		}

		@Override
		public String toString() {
			return "queryNameTextField = '" + this.queryNameTextField + "'; queryTextArea = '"
					+ QueryEditor.this.queryTextArea + "'";
		}
	}
}
