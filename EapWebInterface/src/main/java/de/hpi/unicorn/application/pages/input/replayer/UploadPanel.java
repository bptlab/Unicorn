/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

import de.hpi.unicorn.application.pages.input.replayer.ReplayFileBean.FileType;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.TempFolderUtil;

public class UploadPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private ReplayerPage page;
	private UploadPanel panel;
	private Form layoutForm;
	private FileUploadField fileUpload;
	protected FileType fileType = FileType.CSV;
	protected String eventTypeName;
	protected String name;
	protected String category;

	public UploadPanel(String id, final ReplayerPage page) {
		super(id);
		this.page = page;
		this.panel = this;

		layoutForm = new Form("layoutForm") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit() {

				final FileUpload uploadedFile = fileUpload.getFileUpload();
				if (uploadedFile != null) {
					final String uploadFolder = TempFolderUtil.getFolder();
					final String fileName = uploadedFile.getClientFileName();
					final File newFile = new File(uploadFolder + System.getProperty("file.separator") + fileName);

					if (newFile.exists()) {
						newFile.delete();
					}

					try {
						newFile.createNewFile();
						uploadedFile.writeTo(newFile);
					} catch (final IOException e) {
						try {
							throw new IllegalStateException("Error: File could not be saved under "
									+ newFile.getCanonicalPath() + ".");
						} catch (final IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}

					final int index = fileName.lastIndexOf('.');
					final String fileExtension = fileName.substring(index + 1, fileName.length());

					if (fileType == FileType.CSV) {
						ReplayFileBean bean = new ReplayFileBean(name, uploadFolder
								+ System.getProperty("file.separator") + fileName, eventTypeName, FileType.CSV);
						ReplayerContainer.addFileBean(category, bean);
						page.getFeedbackPanel().success("Upload of " + fileName + " successful. (CSV)");
					} else if (fileType == FileType.XML_ZIP) {
						ReplayFileBean bean = new ReplayFileBean(name, uploadFolder
								+ System.getProperty("file.separator") + fileName, eventTypeName, FileType.XML_ZIP);
						ReplayerContainer.addFileBean(category, bean);
						page.getFeedbackPanel().success("Upload of " + fileName + " successful. (XML (ZIP))");
					} else {
						page.getFeedbackPanel().error("File type not supported.");
					}

				} else {
					page.getFeedbackPanel().error("File not found.");
				}
			}
		};
		layoutForm.setMultiPart(true);
		this.add(layoutForm);

		addUploader();
		addCategoryTextField();
		addRadioChoiceForFileType();
		addEventTypeDropDown();
		addNameTextField();
	}

	private void addCategoryTextField() {
		final TextField<String> categoryTextField = new TextField<String>("categoryTextField", new Model<String>());
		categoryTextField.setOutputMarkupId(true);
		categoryTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				category = categoryTextField.getModelObject();
			}
		});
		layoutForm.add(categoryTextField);
	}

	private void addNameTextField() {
		final TextField<String> nameTextField = new TextField<String>("nameTextField", new Model<String>());
		nameTextField.setOutputMarkupId(true);
		nameTextField.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				name = nameTextField.getModelObject();
			}
		});
		layoutForm.add(nameTextField);
	}

	private void addEventTypeDropDown() {
		final List<String> eventTypes = EapEventType.getAllTypeNames();
		if (!eventTypes.isEmpty()) {
			eventTypeName = eventTypes.get(0);
		}
		final DropDownChoice<String> eventTypeDropDownChoice = new DropDownChoice<String>("eventTypeDropDownChoice",
				new Model<String>(), eventTypes);
		eventTypeDropDownChoice.add(new AjaxFormComponentUpdatingBehavior("onchange") {
			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				eventTypeName = eventTypeDropDownChoice.getModelObject();
			}
		});
		if (!eventTypes.isEmpty()) {
			eventTypeDropDownChoice.setModelObject(eventTypes.get(0));
		}
		eventTypeDropDownChoice.setOutputMarkupId(true);
		layoutForm.add(eventTypeDropDownChoice);
	}

	private void addRadioChoiceForFileType() {
		final RadioChoice<FileType> fileTypeRadioChoice = new RadioChoice<FileType>("fileTypeChoice",
				new Model<FileType>(), Arrays.asList(FileType.values()));
		fileTypeRadioChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				fileType = fileTypeRadioChoice.getModelObject();
			}
		});
		fileTypeRadioChoice.setModelObject(fileType);
		fileTypeRadioChoice.setOutputMarkupId(true);
		this.layoutForm.add(fileTypeRadioChoice);
	}

	private void addUploader() {

		fileUpload = new FileUploadField("fileUpload");
		layoutForm.add(fileUpload);
	}

}
