/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.process.modal;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.components.form.BootstrapModal;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This panel is a {@link BootstrapModal} and allows the creation and deletion
 * of {@link CorrelationProcess}es.
 * 
 * @author micha
 */
public class ProcessEditorModal extends BootstrapModal {

	private static final long serialVersionUID = 1L;
	private final ListChoice<String> existingProcessesList;
	private final List<String> processNames = new ArrayList<String>();
	private static String selectedProcessName = new String();

	/**
	 * Constructor for a panel, which is a {@link BootstrapModal} and allows the
	 * creation and deletion of {@link CorrelationProcess}es.
	 * 
	 * @param processSelect
	 * @param window
	 */
	public ProcessEditorModal(final String id, final DropDownChoice<String> processSelect) {
		super(id, "Process Editor");
		final Form<Void> processEditForm = new WarnOnExitForm("processEditForm");
		this.add(processEditForm);

		// Input für neuen Prozess
		final TextField<String> processNameInput = new TextField<String>("processNameInput", Model.of(""));
		processNameInput.setOutputMarkupId(true);
		processEditForm.add(processNameInput);

		// Button zum Speichern
		final AjaxButton addProcessButton = new AjaxButton("addProcessButton", processEditForm) {
			private static final long serialVersionUID = -8422505767509635904L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final String processName = processNameInput.getValue();
				if (!processName.isEmpty() && !CorrelationProcess.exists(processName)) {
					final CorrelationProcess process = new CorrelationProcess(processName);
					process.save();
					ProcessEditorModal.this.processNames.add(processName);
					target.add(ProcessEditorModal.this.existingProcessesList);
					processSelect.setChoices(ProcessEditorModal.this.processNames);
					target.add(processSelect);
				}
			}
		};
		processEditForm.add(addProcessButton);

		// ListChoice für bestehende Prozesse
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNames.add(process.getName());
		}
		this.existingProcessesList = new ListChoice<String>("existingProcessSelect", new Model<String>(
				ProcessEditorModal.selectedProcessName), this.processNames);
		this.existingProcessesList.setOutputMarkupId(true);
		processEditForm.add(this.existingProcessesList);

		// Button zum Löschen bestehender Prozesse
		final AjaxButton removeProcessButton = new AjaxButton("removeProcessButton", processEditForm) {
			private static final long serialVersionUID = 3874692865572427214L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				final CorrelationProcess selectedProcess = ProcessEditorModal.this.getSelectedProcess();
				if (selectedProcess != null) {
					final String processName = selectedProcess.getName();
					ProcessEditorModal.this.processNames.remove(processName);
					selectedProcess.remove();
				}
				target.add(ProcessEditorModal.this.existingProcessesList);

				processSelect.setChoices(ProcessEditorModal.this.processNames);
				target.add(processSelect);
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
