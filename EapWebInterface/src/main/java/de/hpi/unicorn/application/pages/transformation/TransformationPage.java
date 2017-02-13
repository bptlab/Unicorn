/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.transformation;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.agilecoders.wicket.markup.html.bootstrap.tabs.BootstrapTabbedPanel;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.eventrepository.eventtypeeditor.EventTypeEditor;
import de.hpi.unicorn.application.pages.transformation.patternbuilder.externalknowledge.ExternalKnowledgeModal;

public class TransformationPage extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private TransformationPage transformationPage;
	private ExternalKnowledgeModal externalKnowledgeModal;

	public TransformationPage() {
		super();
		this.transformationPage = this;

		this.buildOpenEventTypeEditorButton();

		final List<ITab> tabs = new ArrayList<ITab>();
		tabs.add(new AbstractTab(new Model<String>("Basic Rule Editor")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new BasicTransformationRuleEditorPanel(panelId, TransformationPage.this.transformationPage);
			}
		});
		tabs.add(new AbstractTab(new Model<String>("Advanced Rule Editor")) {
			@Override
			public Panel getPanel(final String panelId) {
				return new AdvancedTransformationRuleEditorPanel(panelId, TransformationPage.this.transformationPage);
			}
		});

		this.add(new BootstrapTabbedPanel<ITab>("tabs", tabs));

		this.externalKnowledgeModal = new ExternalKnowledgeModal("externalKnowledgeModal");
		this.externalKnowledgeModal.setOutputMarkupId(true);
		this.add(this.externalKnowledgeModal);
	}

	private void buildOpenEventTypeEditorButton() {
		final Button openEventTypeEditorButton = new Button("openEventTypeEditorButton") {
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {
				this.setResponsePage(EventTypeEditor.class);
			}
		};
		final Form<Void> layoutForm = new Form<Void>("layoutForm");
		this.add(layoutForm);
		layoutForm.add(openEventTypeEditorButton);
	}

	public ExternalKnowledgeModal getExternalKnowledgeModal() {
		return this.externalKnowledgeModal;
	}

	public void setExternalKnowledgeModal(final ExternalKnowledgeModal externalKnowledgeModal) {
		this.externalKnowledgeModal = externalKnowledgeModal;
	}
}
