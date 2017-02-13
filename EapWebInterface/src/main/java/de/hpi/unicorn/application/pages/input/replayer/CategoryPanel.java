/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input.replayer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormChoiceComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.CheckBoxMultipleChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;

import de.hpi.unicorn.application.components.form.WarnOnExitForm;
import de.hpi.unicorn.application.pages.input.replayer.EventReplayer.TimeMode;
import de.hpi.unicorn.application.pages.input.replayer.ReplayFileBean.FileType;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.ReplayEvent;
import de.hpi.unicorn.utils.DateUtils;
import de.hpi.unicorn.utils.TempFolderUtil;

/**
 * Panel representing the content panel for the first tab.
 */
public class CategoryPanel extends Panel {

	private static final long serialVersionUID = 573672364803879784L;
	private CheckBoxMultipleChoice<ReplayFileBean> eventsCheckBoxMultipleChoice;
	private FilesPanel panel;
	private List<ReplayFileBean> selectedFiles = new ArrayList<ReplayFileBean>();
	private TextField<String> scaleFactorInput;
	private String scaleFactor = "100";
	private TextField<String> alignedTimeInput;
	private String alignedTime = "2015-01-27 16:30:00";
	private TextField<String> fixedOffsetInput;
	private String fixedOffset = "-1";
	private List<ReplayFileBean> beans;
	private String category;
	protected TimeMode timeMode;

	public CategoryPanel(final String id, final FilesPanel panel, String category, List<ReplayFileBean> beans) {
		super(id);

		this.panel = panel;
		this.category = category;
		this.beans = beans;

		final Form<Void> form = new WarnOnExitForm("replayerForm");
		this.add(form);

		addSelectAllFilesButton(form);
		addEventsCheckBoxMultipleChoice(form);
		addRadioChoiceForTimeMode(form);
		addScaleFactorTextField(form);
		addAlignedTimeTextField(form);
		addFixedOffsetTextField(form);
		addReplayButton(form);
		addRemoveCategoryButton(form);
	}

	private void addRemoveCategoryButton(Form<Void> form) {
		final AjaxButton removeCategoryButton = new AjaxButton("removeCategoryButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);
				panel.removeCategory(target, category);
			}
		};

		form.add(removeCategoryButton);
	}

	private void addSelectAllFilesButton(Form<Void> form) {
		// confirm button
		final AjaxButton selectAllFilesButton = new AjaxButton("selectAllFilesButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);

				selectedFiles = beans;

				target.add(eventsCheckBoxMultipleChoice);
			}
		};

		form.add(selectAllFilesButton);
	}

	private void addEventsCheckBoxMultipleChoice(final Form<Void> layoutForm) {
		this.eventsCheckBoxMultipleChoice = new CheckBoxMultipleChoice<ReplayFileBean>("eventsCheckBoxMultipleChoice",
				new PropertyModel<List<ReplayFileBean>>(this, "selectedFiles"), beans);
		this.eventsCheckBoxMultipleChoice.setOutputMarkupId(true);
		layoutForm.add(this.eventsCheckBoxMultipleChoice);

		// this.eventsCheckGroup = new
		// CheckGroup<ReplayFileBean>("eventsCheckGroup",
		// new PropertyModel<List<ReplayFileBean>>(this, "selectedFiles"));
		// this.eventsCheckGroup.add(new CheckGroupSelector("groupSelector"));
		//
		// ListView<ReplayFileBean> files = new
		// ListView<ReplayFileBean>("beans", beans) {
		// @Override
		// protected void populateItem(ListItem<ReplayFileBean> item) {
		// item.add(new Label("name", item.getModelObject().toString()));
		// }
		// };
		// files.setReuseItems(true);
		// this.eventsCheckGroup.add(files);
		// this.eventsCheckGroup.setOutputMarkupId(true);
		// layoutForm.add(this.eventsCheckGroup);
	}

	private void addScaleFactorTextField(Form<Void> form) {
		this.scaleFactorInput = new TextField<String>("scaleFactorInput",
				new PropertyModel<String>(this, "scaleFactor"));
		this.scaleFactorInput.setOutputMarkupId(true);
		form.add(this.scaleFactorInput);
	}

	private void addAlignedTimeTextField(Form<Void> form) {
		this.alignedTimeInput = new TextField<String>("alignedTimeInput",
				new PropertyModel<String>(this, "alignedTime"));
		this.alignedTimeInput.setOutputMarkupId(true);
		form.add(this.alignedTimeInput);
	}

	private void addFixedOffsetTextField(Form<Void> form) {
		this.fixedOffsetInput = new TextField<String>("fixedOffsetInput",
				new PropertyModel<String>(this, "fixedOffset"));
		this.fixedOffsetInput.setOutputMarkupId(true);
		form.add(this.fixedOffsetInput);
	}

	private void addRadioChoiceForTimeMode(Form<Void> form) {
		final RadioChoice<TimeMode> timeModeRadioChoice = new RadioChoice<TimeMode>("timeModeChoice",
				new Model<TimeMode>(), Arrays.asList(TimeMode.values()));
		timeModeRadioChoice.add(new AjaxFormChoiceComponentUpdatingBehavior() {

			@Override
			protected void onUpdate(final AjaxRequestTarget target) {
				timeMode = timeModeRadioChoice.getModelObject();
			}
		});
		timeModeRadioChoice.setModelObject(TimeMode.values()[0]);
		timeModeRadioChoice.setOutputMarkupId(true);
		form.add(timeModeRadioChoice);
	}

	private void addReplayButton(Form<Void> form) {
		// confirm button
		final AjaxButton confirmButton = new AjaxButton("replayButton", form) {

			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit(final AjaxRequestTarget target, final Form form) {
				super.onSubmit(target, form);

				List<EapEvent> events = new ArrayList<EapEvent>();
				TreeSet<ReplayEvent> replayEvents = new TreeSet<ReplayEvent>(new Comparator<ReplayEvent>() {
					@Override
					public int compare(ReplayEvent e1, ReplayEvent e2) {
						long diff = e1.getOffset() - e2.getOffset();
						return (diff == 0l) ? -1 : (int) diff;
					}
				});

				List<List<EapEvent>> eventLists = new ArrayList<List<EapEvent>>();

				for (ReplayFileBean b : selectedFiles) {
					if (b.type == FileType.CSV) {

						if (timeMode == TimeMode.ALIGNED) {
							List<EapEvent> temp = panel.generateEventsFromCSV(b.filePath,
									EapEventType.findByTypeName(b.eventTypeName));
							eventLists.add(temp);
						} else {
							events.addAll(panel.generateEventsFromCSV(b.filePath,
									EapEventType.findByTypeName(b.eventTypeName)));
						}
					} else if (b.type == FileType.XML_ZIP) {
						String outputFolder = TempFolderUtil.getFolder() + System.getProperty("file.separator")
								+ b.name;
						File folder = new File(outputFolder);
						if (!folder.exists()) {
							folder.mkdir();
						} else {
							for (File f : folder.listFiles()) {
								f.delete();
							}
						}

						List<String> extractedXmlFilePaths = new ArrayList<String>();

						try {

							byte[] buffer = new byte[1024];

							ZipInputStream zis = new ZipInputStream(new FileInputStream(b.filePath));
							ZipEntry ze = zis.getNextEntry();

							while (ze != null) {
								String fileName = ze.getName();
								String path = outputFolder + File.separator + fileName;
								File newFile = new File(path);

								new File(newFile.getParent()).mkdirs();
								FileOutputStream fos = new FileOutputStream(newFile);

								int len;
								while ((len = zis.read(buffer)) > 0) {
									fos.write(buffer, 0, len);
								}

								fos.close();
								ze = zis.getNextEntry();

								extractedXmlFilePaths.add(path);
							}

							zis.closeEntry();
							zis.close();

						} catch (IOException e) {
							e.printStackTrace();
						}

						if (timeMode == TimeMode.ALIGNED) {
							List<EapEvent> temp = new ArrayList<EapEvent>();
							for (String path : extractedXmlFilePaths) {
								temp.addAll(panel.generateEventsFromXML(path,
										EapEventType.findByTypeName(b.eventTypeName)));
							}
							eventLists.add(temp);
						} else {
							for (String path : extractedXmlFilePaths) {
								events.addAll(panel.generateEventsFromXML(path,
										EapEventType.findByTypeName(b.eventTypeName)));
							}
						}
					}
					if (timeMode == TimeMode.ALIGNED_MULTIPLE) {
						TreeSet<ReplayEvent> rList = new TreeSet<ReplayEvent>(new Comparator<ReplayEvent>() {
							@Override
							public int compare(ReplayEvent e1, ReplayEvent e2) {
								long diff = e1.getTime() - e2.getTime();
								return (diff == 0l) ? -1 : (int) diff;
							}
						});
						for (EapEvent e : events) {
							addEventToReplayList(rList, e);
						}
						replayEvents.addAll(rList);
					}

				}

				Date simulationTimeInit = null;
				if (timeMode == TimeMode.ALIGNED || timeMode == TimeMode.ALIGNED_MULTIPLE) {
					simulationTimeInit = DateUtils.parseDate(alignedTime);
				}

				Long fOffset = null;
				if (Long.parseLong(fixedOffset) > 0) {
					fOffset = Long.parseLong(fixedOffset);
				}

				if (timeMode == TimeMode.ALIGNED_MULTIPLE) {
					panel.replayEventsWithMultipleAlignment(replayEvents, Integer.parseInt(scaleFactor), category,
							selectedFiles, timeMode, simulationTimeInit, fOffset);
				} else if (timeMode == TimeMode.ALIGNED) {
					for (List<EapEvent> e : eventLists) {
						panel.replayEvents(e, Integer.parseInt(scaleFactor), category, selectedFiles, timeMode,
								simulationTimeInit, fOffset);
					}
				} else {
					panel.replayEvents(events, Integer.parseInt(scaleFactor), category, selectedFiles, timeMode,
							simulationTimeInit, fOffset);
				}

				panel.getParentPage().getFeedbackPanel()
						.success("Replayer started for " + category + " - files: " + selectedFiles + ".");

				target.add(panel.getParentPage());
			}
		};

		form.add(confirmButton);
	}

	public void addEventToReplayList(TreeSet<ReplayEvent> replayList, EapEvent e) {
		ReplayEvent re = new ReplayEvent(e);
		ReplayEvent ceiling = replayList.ceiling(re);
		ReplayEvent floor = replayList.floor(re);
		// update the offsets
		if (ceiling != null)
			ceiling.setOffset(ceiling.getTime() - re.getTime());
		if (floor != null) {
			re.setOffset(re.getTime() - floor.getTime());
		} else {
			re.setOffset(0l);
		}
		replayList.add(re);
	}

};
