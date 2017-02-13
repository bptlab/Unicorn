/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.eventrepository.processeditor;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This class is page to create and delete {@link CorrelationProcess}es from the
 * database.
 * 
 * @author micha
 */
public class ProcessEditor extends AbstractEapPage {

	private static final long serialVersionUID = 1L;
	private final ListChoice<String> existingProcessesList;
	private final List<String> processNames = new ArrayList<String>();
	private static String selectedProcessName = new String();

	/**
	 * Constructor for a page to create and delete {@link CorrelationProcess}es
	 * from the database.
	 */
	public ProcessEditor() {
		super();
		final Form<Void> processEditForm = new WarnOnExitForm("processEditForm");
		this.add(processEditForm);

		// Input für neuen Prozess
		final TextField<String> processNameInput = new TextField<String>("processNameInput", Model.of(""));
		processNameInput.setOutputMarkupId(true);
		processEditForm.add(processNameInput);

		// Plus-Button zum Speichern
		final AjaxButton addProcessButton = new AjaxButton("addProcessButton", processEditForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String processName = processNameInput.getValue();
				if (!processName.isEmpty() && !CorrelationProcess.exists(processName)) {
					final CorrelationProcess process = new CorrelationProcess(processName);
					process.save();
					ProcessEditor.this.processNames.add(processName);
					target.add(ProcessEditor.this.existingProcessesList);
				}
			}
		};
		processEditForm.add(addProcessButton);

		// ListChoice für bestehende Prozesse
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNames.add(process.getName());
		}
		this.existingProcessesList = new ListChoice<String>("existingProcessSelect", new Model(
				ProcessEditor.selectedProcessName), this.processNames);
		this.existingProcessesList.setOutputMarkupId(true);

		this.existingProcessesList.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
			}
		});
		processEditForm.add(this.existingProcessesList);

		// Minus-Button zum Löschen bestehender Prozesse
		final AjaxButton removeProcessButton = new AjaxButton("removeProcessButton", processEditForm) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final CorrelationProcess selectedProcess = ProcessEditor.this.getSelectedProcess();
				if (selectedProcess != null) {
					final String processName = selectedProcess.getName();
					ProcessEditor.this.processNames.remove(processName);
					selectedProcess.remove();
				}
				target.add(ProcessEditor.this.existingProcessesList);
			}

		};
		processEditForm.add(removeProcessButton);
	}

	private CorrelationProcess getSelectedProcess() {
		try {
			final int processListIndex = Integer.parseInt(this.existingProcessesList.getValue());
			final String processName = this.existingProcessesList.getChoices().get(processListIndex);
			return CorrelationProcess.findByName(processName).get(0);
		} catch (final Exception e) {
			return null;
		}
	}
}
