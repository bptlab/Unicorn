/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.bpmn;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.xml.sax.SAXException;

import de.hpi.unicorn.application.components.form.ExternalPage;
import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.application.pages.input.bpmn.model.ProcessModelProvider;
import de.hpi.unicorn.application.pages.process.modal.ProcessEditorModal;
import de.hpi.unicorn.application.pages.simulator.BPMNSimulationPanel;
import de.hpi.unicorn.application.pages.simulator.SimulationPanel;
import de.hpi.unicorn.bpmn.element.AbstractBPMNElement;
import de.hpi.unicorn.bpmn.element.BPMNProcess;
import de.hpi.unicorn.importer.xml.BPMNParser;
import de.hpi.unicorn.process.CorrelationProcess;

/**
 * This panel allows the upload and visualisation of a BPMN process model from a
 * BPMN2.0-XML file. Furthermore it is possible to simulate this process with
 * the {@link SimulationPanel}.
 * 
 * @author micha
 * @author benni
 */
public class BPMNProcessUploadPanel extends Panel {

	private static final long serialVersionUID = 1L;
	private FileUploadField fileUpload;
	private ProcessEditorModal processEditorModal;
	private ArrayList<String> processNameList;
	private BPMNProcess processModel;
	private DropDownChoice<String> processSelect;
	private WarnOnExitForm uploadForm;
	private Button uploadButton;
	private AjaxButton saveModelButton;
	private AjaxButton deleteModelButton;
	private CorrelationProcess process;
	private AjaxButton cancelButton;
	private AjaxButton saveChangesButton;
	private TextField<String> bpmnProcessNameInput;
	private String fileNameWithoutExtension;
	private String bpmnProcessNameInputValue;
	private final AbstractEapPage abstractEapPage;
	private ArrayList<IColumn<AbstractBPMNElement, String>> columns;
	private DefaultDataTable<AbstractBPMNElement, String> processModelTable;
	private final ProcessModelProvider processModelProvider;
	private final ExternalPage externalPage;
	private BPMNSimulationPanel simulationPanel;
	private List<Component> targets;

	/**
	 * This is the constructor for a panel, which allows the upload and
	 * visualisation of a BPMN process model from a BPMN2.0-XML file.
	 * Furthermore it is possible to simulate this process with the
	 * {@link SimulationPanel}.
	 * 
	 * @param id
	 * @param abstractEapPage
	 */
	@SuppressWarnings("unchecked")
	public BPMNProcessUploadPanel(final String id, final AbstractEapPage abstractEapPage) {
		super(id);
		this.abstractEapPage = abstractEapPage;
		this.processModelProvider = new ProcessModelProvider();

		final Form<Void> layoutForm = new Form<Void>("layoutForm");
		this.add(layoutForm);

		this.addProcessSelect(layoutForm);

		this.addUploadForm();

		this.createProcessEditModalWindow(layoutForm);

		this.addResultForm();
		// TODO:
		this.externalPage = new ExternalPage("iframe", "http://localhost:8181/signaviocore/p/explorer");
		this.externalPage.setOutputMarkupId(true);
		this.add(this.externalPage);
		this.createTargetList(abstractEapPage);
	}

	private void createTargetList(final AbstractEapPage abstractEapPage) {
		this.targets = new ArrayList<Component>();
		this.targets.add(this.deleteModelButton);
		this.targets.add(this.saveChangesButton);
		this.targets.add(this.saveModelButton);
		this.targets.add(this.cancelButton);
		this.targets.add(abstractEapPage.getFeedbackPanel());
		this.targets.add(this.processModelTable);
		this.targets.add(this.bpmnProcessNameInput);
		this.targets.add(this.uploadButton);
		this.targets.add(this.fileUpload);
	}

	private void addResultForm() {
		final Form<Void> resultForm = new Form<Void>("resultForm");
		this.add(resultForm);
		resultForm.add(this.addProcessModelTable());

		this.saveModelButton = (new AjaxButton("saveModel") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				if (BPMNProcessUploadPanel.this.process.getBpmnProcess() != null) {
					BPMNProcessUploadPanel.this.process.getBpmnProcess().remove();
				}
				if (BPMNProcessUploadPanel.this.bpmnProcessNameInputValue == null
						|| BPMNProcessUploadPanel.this.bpmnProcessNameInputValue.isEmpty()) {
					BPMNProcessUploadPanel.this.bpmnProcessNameInputValue = BPMNProcessUploadPanel.this.fileNameWithoutExtension;
				}
				if (!BPMNProcessUploadPanel.this.containsOtherProcessSameBPMNProcessName(
						BPMNProcessUploadPanel.this.process, BPMNProcessUploadPanel.this.bpmnProcessNameInputValue)) {
					BPMNProcessUploadPanel.this.processModel
							.setName(BPMNProcessUploadPanel.this.bpmnProcessNameInputValue);
					BPMNProcessUploadPanel.this.process.setBpmnProcess(BPMNProcessUploadPanel.this.processModel);
					BPMNProcessUploadPanel.this.processModel.save();
					BPMNProcessUploadPanel.this.process.save();
					BPMNProcessUploadPanel.this.saveModelButton.setVisible(false);
					BPMNProcessUploadPanel.this.cancelButton.setVisible(false);
					BPMNProcessUploadPanel.this.saveChangesButton.setVisible(true);
					BPMNProcessUploadPanel.this.deleteModelButton.setVisible(true);
					BPMNProcessUploadPanel.this.abstractEapPage.getFeedbackPanel().success("Saved process!");
				} else {
					BPMNProcessUploadPanel.this.abstractEapPage.getFeedbackPanel().error(
							"Another process has the same BPMN process name!");
				}
				BPMNProcessUploadPanel.this.addTargets(target);
			}

		});
		this.saveModelButton.setVisible(false);
		resultForm.add(this.saveModelButton);
		this.saveModelButton.setOutputMarkupId(true);
		this.saveModelButton.setOutputMarkupPlaceholderTag(true);

		this.cancelButton = (new AjaxButton("cancel") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				BPMNProcessUploadPanel.this.processModel = BPMNProcessUploadPanel.this.process.getBpmnProcess();
				BPMNProcessUploadPanel.this.processModelProvider
						.setProcessModel(BPMNProcessUploadPanel.this.processModel);
				if (BPMNProcessUploadPanel.this.processModel != null) {
					BPMNProcessUploadPanel.this.deleteModelButton.setVisible(true);
					BPMNProcessUploadPanel.this.saveChangesButton.setVisible(true);
				} else {
					BPMNProcessUploadPanel.this.deleteModelButton.setVisible(false);
					BPMNProcessUploadPanel.this.saveChangesButton.setVisible(false);
				}
				BPMNProcessUploadPanel.this.cancelButton.setVisible(false);
				BPMNProcessUploadPanel.this.saveModelButton.setVisible(false);
				BPMNProcessUploadPanel.this.addTargets(target);
			}
		});
		this.cancelButton.setVisible(false);
		resultForm.add(this.cancelButton);
		this.cancelButton.setOutputMarkupId(true);
		this.cancelButton.setOutputMarkupPlaceholderTag(true);

		this.saveChangesButton = (new AjaxButton("saveChanges") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				if (BPMNProcessUploadPanel.this.bpmnProcessNameInputValue == null
						|| BPMNProcessUploadPanel.this.bpmnProcessNameInputValue.isEmpty()) {
					BPMNProcessUploadPanel.this.bpmnProcessNameInputValue = BPMNProcessUploadPanel.this.fileNameWithoutExtension;
				}
				BPMNProcessUploadPanel.this.processModel.setName(BPMNProcessUploadPanel.this.bpmnProcessNameInputValue);
				// TODO: geändertes Modell einlesen

				BPMNProcessUploadPanel.this.processModel.save();
				BPMNProcessUploadPanel.this.process.save();
			}
		});
		this.saveChangesButton.setVisible(false);
		this.saveChangesButton.setEnabled(false);
		resultForm.add(this.saveChangesButton);
		this.saveChangesButton.setOutputMarkupId(true);
		this.saveChangesButton.setOutputMarkupPlaceholderTag(true);

		this.deleteModelButton = (new AjaxButton("deleteModel") {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(final AjaxRequestTarget target, final Form<?> form) {
				if (BPMNProcessUploadPanel.this.process.getBpmnProcess() != null) {
					BPMNProcessUploadPanel.this.process.getBpmnProcess().remove();
				}

				BPMNProcessUploadPanel.this.abstractEapPage.getFeedbackPanel().error("No process model exists!");
				BPMNProcessUploadPanel.this.processModelProvider.setProcessModel(null);
				BPMNProcessUploadPanel.this.deleteModelButton.setVisible(false);
				BPMNProcessUploadPanel.this.saveChangesButton.setVisible(false);

				BPMNProcessUploadPanel.this.addTargets(target);
			}
		});
		this.deleteModelButton.setVisible(false);
		resultForm.add(this.deleteModelButton);
		this.deleteModelButton.setOutputMarkupId(true);
		this.deleteModelButton.setOutputMarkupPlaceholderTag(true);
	}

	private void addUploadForm() {
		this.uploadForm = new WarnOnExitForm("uploadForm") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				final FileUpload uploadedFile = BPMNProcessUploadPanel.this.fileUpload.getFileUpload();
				if (uploadedFile != null) {
					// String uploadFolder;

					// if (System.getProperty("os.name").contains("Windows")) {
					// uploadFolder = "C:\\temp\\";
					// }
					// else {
					// File _uploadFolder = new
					// File(System.getProperty("user.dir"));
					// _uploadFolder.mkdirs();
					// try {
					// uploadFolder = _uploadFolder.getCanonicalPath();
					// } catch (IOException e) {
					// uploadFolder = "/tmp/";
					// e.printStackTrace();
					// }
					// }
					//
					final String fileName = uploadedFile.getClientFileName();
					// File newFile = new File(uploadFolder + "/"+fileName);
					//
					// if (newFile.exists()) {
					// newFile.delete();
					// }
					//
					// try {
					// newFile.createNewFile();
					// uploadedFile.writeTo(newFile);
					// // info("Saved file: " + fileName);
					// } catch (IOException e) {
					// try {
					// throw new
					// IllegalStateException("Error: File could not be saved under "
					// + newFile.getCanonicalPath() +".");
					// } catch (IOException e1) {
					// // TODO Auto-generated catch block
					// e1.printStackTrace();
					// }
					// }
					File newFile = null;
					try {
						newFile = uploadedFile.writeToTempFile();
					} catch (final IOException e) {
						throw new IllegalStateException("Error: File could not be saved under "
								+ newFile.getAbsolutePath() + ".");
					}

					final int index = fileName.lastIndexOf('.');
					BPMNProcessUploadPanel.this.fileNameWithoutExtension = fileName.substring(0, index);
					final String fileExtension = fileName.substring(index + 1, fileName.length());
					if (fileExtension.toLowerCase().contains("xml") || fileExtension.toLowerCase().contains("bpmn")) {
						try {
							BPMNProcessUploadPanel.this.processModel = BPMNParser.generateProcessFromXML(newFile);
						} catch (final IOException e) {
							this.error("File could not be read, parsing error: " + e.getMessage());
						} catch (final SAXException e) {
							this.error("File is not valid: " + e.getMessage());
						}
						// BPM2XMLToSignavioXMLConverter signavioConverter =
						// new BPM2XMLToSignavioXMLConverter(newFile);
						// String newFileName =
						// signavioConverter.generateSignavioXMLFromBPM2XML();
						BPMNProcessUploadPanel.this.processModelProvider
								.setProcessModel(BPMNProcessUploadPanel.this.processModel);
						BPMNProcessUploadPanel.this.cancelButton.setVisible(true);
						BPMNProcessUploadPanel.this.saveModelButton.setVisible(true);
						BPMNProcessUploadPanel.this.deleteModelButton.setVisible(false);
						BPMNProcessUploadPanel.this.saveChangesButton.setVisible(false);
						// externalPage.setURL(pathToCoreComponents +
						// newFileName);
					} else {
						this.error("No XML");
					}
					BPMNProcessUploadPanel.this.uploadButton.setEnabled(false);

				} else {
					this.error("File not found");
				}

				if (BPMNProcessUploadPanel.this.simulationPanel != null) {
					BPMNProcessUploadPanel.this.simulationPanel.updateMonitoringPoints(null);
				}
			}
		};

		this.uploadForm.add(this.fileUpload = new FileUploadField("fileUpload"));
		this.uploadForm.setMultiPart(true);
		this.fileUpload.setOutputMarkupId(true);
		this.fileUpload.setEnabled(false);
		this.add(this.uploadForm);

		this.uploadButton = new Button("upload");
		this.uploadButton.setEnabled(false);
		this.uploadButton.setOutputMarkupId(true);
		this.uploadForm.add(this.uploadButton);

		this.bpmnProcessNameInput = new TextField<String>("bpmnProcessNameInput", new PropertyModel(this,
				"bpmnProcessNameInputValue"));
		this.bpmnProcessNameInput.setOutputMarkupId(true);

		this.bpmnProcessNameInput.add(new OnChangeAjaxBehavior() {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				BPMNProcessUploadPanel.this.bpmnProcessNameInputValue = ((TextField<String>) this.getComponent())
						.getModelObject();
			}
		});

		this.uploadForm.add(this.bpmnProcessNameInput);
	}

	private void addProcessSelect(final Form<Void> layoutForm) {
		this.processNameList = new ArrayList<String>();
		for (final CorrelationProcess process : CorrelationProcess.findAll()) {
			this.processNameList.add(process.getName());
		}

		this.processSelect = new DropDownChoice<String>("processSelect", new Model<String>(), this.processNameList);
		this.processSelect.setOutputMarkupId(true);
		this.processSelect.add(new AjaxFormComponentUpdatingBehavior("onchange") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				BPMNProcessUploadPanel.this.fileUpload.setEnabled(true);
				BPMNProcessUploadPanel.this.uploadButton.setEnabled(true);
				BPMNProcessUploadPanel.this.process = CorrelationProcess.findByName(
						BPMNProcessUploadPanel.this.processSelect.getChoices().get(
								Integer.parseInt(BPMNProcessUploadPanel.this.processSelect.getValue()))).get(0);
				BPMNProcessUploadPanel.this.processModel = BPMNProcessUploadPanel.this.process.getBpmnProcess();
				BPMNProcessUploadPanel.this.processModelProvider
						.setProcessModel(BPMNProcessUploadPanel.this.processModel);

				if (BPMNProcessUploadPanel.this.processModel != null) {
					BPMNProcessUploadPanel.this.deleteModelButton.setVisible(true);
					BPMNProcessUploadPanel.this.saveChangesButton.setVisible(true);
					BPMNProcessUploadPanel.this.bpmnProcessNameInputValue = BPMNProcessUploadPanel.this.processModel
							.getName();
					if (BPMNProcessUploadPanel.this.simulationPanel != null) {
						BPMNProcessUploadPanel.this.simulationPanel.updateMonitoringPoints(target);
					}
				}
				BPMNProcessUploadPanel.this.addTargets(target);
			}
		});

		layoutForm.add(this.processSelect);
	}

	@SuppressWarnings("unchecked")
	private Component addProcessModelTable() {
		this.columns = new ArrayList<IColumn<AbstractBPMNElement, String>>();
		this.columns.add(new AbstractColumn<AbstractBPMNElement, String>(new Model("Element")) {

			@Override
			public void populateItem(final Item cellItem, final String componentId, final IModel rowModel) {
				final String elementName = ((AbstractBPMNElement) rowModel.getObject()).toString();
				cellItem.add(new Label(componentId, elementName));
			}
		});
		this.columns.add(new PropertyColumn<AbstractBPMNElement, String>(Model.of("Predecessors"), "predecessors"));
		this.columns.add(new PropertyColumn<AbstractBPMNElement, String>(Model.of("Successors"), "successors"));
		this.columns.add(new PropertyColumn<AbstractBPMNElement, String>(Model.of("Monitoring Points"),
				"monitoringPoints"));

		this.processModelTable = new DefaultDataTable<AbstractBPMNElement, String>("processModelElements",
				this.columns, this.processModelProvider, 40);
		this.processModelTable.setOutputMarkupId(true);
		this.processModelTable.setOutputMarkupPlaceholderTag(true);

		return this.processModelTable;
	}

	private void createProcessEditModalWindow(final Form<Void> layoutForm) {
		this.processEditorModal = new ProcessEditorModal("processEditorModal", this.processSelect);
		this.add(this.processEditorModal);

		layoutForm.add(new AjaxLink<Void>("showProcessEditModal") {

			private static final long serialVersionUID = 1L;

			@Override
			public void onClick(final AjaxRequestTarget target) {
				BPMNProcessUploadPanel.this.processEditorModal.show(target);
			}
		});
	}

	public String getSelectedProcessName() {
		return this.processSelect.getModelObject();
	}

	public CorrelationProcess getProcess() {
		return this.process;
	}

	public BPMNProcess getProcessModel() {
		return this.processModel;
	}

	public void setSimulationPanel(final BPMNSimulationPanel simulationPanel) {
		this.simulationPanel = simulationPanel;
		this.targets.add(simulationPanel.getMonitoringPointTable());
	}

	private void addTargets(final AjaxRequestTarget target) {
		for (final Component targetComponent : this.targets) {
			target.add(targetComponent);
		}
	}

	/**
	 * Searches for BPMN processes with the same, but a different containing
	 * process.
	 * 
	 * @param process
	 * @param bpmnProcessName
	 * @return
	 */
	private boolean containsOtherProcessSameBPMNProcessName(final CorrelationProcess process,
			final String bpmnProcessName) {
		final List<BPMNProcess> bpmnProcesses = BPMNProcess.findByName(bpmnProcessName);
		for (final BPMNProcess bpmnProcess : bpmnProcesses) {
			if (!CorrelationProcess.findByBPMNProcess(bpmnProcess).equals(process)) {
				return true;
			}
		}
		return false;
	}
}
