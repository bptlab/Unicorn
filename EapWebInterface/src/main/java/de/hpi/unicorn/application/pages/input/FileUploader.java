/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.application.pages.input;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.json.JSONException;

import de.hpi.unicorn.application.pages.AbstractEapPage;
import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.eventhandling.Broker;
import de.hpi.unicorn.importer.csv.CSVImporter;
import de.hpi.unicorn.importer.edifact.EdifactImporter;
import de.hpi.unicorn.importer.excel.ExcelImporter;
import de.hpi.unicorn.importer.excel.FileNormalizer;
import de.hpi.unicorn.importer.xml.XMLParser;
import de.hpi.unicorn.importer.xml.XMLParsingException;
import de.hpi.unicorn.utils.TempFolderUtil;

public class FileUploader extends AbstractEapPage {

	private static final long serialVersionUID = 1L;

	private FileUploadField fileUpload;

	public FileUploader() {
		super();

		final Form<Void> uploadForm = new Form<Void>("uploadForm") {

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit() {
				final FileUpload uploadedFile = FileUploader.this.fileUpload.getFileUpload();
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
						// info("Saved file: " + fileName);
					} catch (final IOException e) {
						try {
							throw new IllegalStateException("Error: File could not be saved under "
									+ newFile.getCanonicalPath() + ".");
						} catch (final IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
					// File newFile = null;
					// try {
					// newFile = uploadedFile.writeToTempFile();
					// } catch (IOException e) {
					// throw new
					// IllegalStateException("Error: File could not be saved under "
					// + newFile.getAbsolutePath() + ".");
					// }

					final int index = fileName.lastIndexOf('.');
					final String fileExtension = fileName.substring(index + 1, fileName.length());
					// create Excel- or CSV-Events
					if (fileExtension.toLowerCase().equals("xls") || fileExtension.toLowerCase().equals("csv")) {
						try {
							final PageParameters pageParameters = new PageParameters();
							pageParameters.add("filePath", newFile.getAbsolutePath());
							if (FileUploader.noEventTypesFound(pageParameters)) {
								this.setResponsePage(ExcelEventTypeCreator.class, pageParameters);
							} else {
								this.setResponsePage(ExcelEventTypeMatcher.class, pageParameters);
							}
						} catch (final Exception e) {
							this.error(e.getMessage());
						}
						// create event type from xsd
					} else if (fileExtension.toLowerCase().equals("xsd")) {
						try {
							final PageParameters pageParameters = new PageParameters();
							pageParameters.add("filePath", newFile.getAbsolutePath());
							this.setResponsePage(new XSDEventTypeCreator(pageParameters));
						} catch (final Exception e) {
							this.error(e.getMessage());
						}
						// create XML-Event
					} else if (fileExtension.toLowerCase().equals("xml")) {
						List<EapEvent> uploadedEvents;
						try {
							uploadedEvents = XMLParser.generateEventsFromXML(newFile.getAbsolutePath());
							for (final EapEvent uploadedEvent : uploadedEvents) {
								Broker.getEventImporter().importEvent(uploadedEvent);
							}
							this.info("Saved " + uploadedEvents.size() + " event(s) from " + fileName);
						} catch (final XMLParsingException e) {
							this.error(e.getMessage());
						}
					} else if (fileExtension.toLowerCase().equals("txt") || fileExtension.toLowerCase().equals("edi")) {
						List<EapEvent> uploadedEvents;
						try {
							uploadedEvents = EdifactImporter.getInstance().generateEventFromEdifact(
									newFile.getAbsolutePath());
							for (final EapEvent uploadedEvent : uploadedEvents) {
								if (!Broker.getEventAdministrator().getAllEventTypes()
										.contains(uploadedEvent.getEventType())) {
									Broker.getEventAdministrator().importEventType(uploadedEvent.getEventType());
								}
								Broker.getEventImporter().importEvent(uploadedEvent);
							}
							this.info("Saved " + uploadedEvents.size() + " event(s) from " + fileName);
						} catch (final XMLParsingException e) {
							this.error(e.getMessage());
						} catch (final Exception e) {
							this.error(e.getMessage());
						}
					}

				} else {
					this.error("File not found");
				}
			}
		};

		uploadForm.setMultiPart(true);
		uploadForm.add(this.fileUpload = new FileUploadField("fileUpload"));
		this.add(uploadForm);
	}

	public static boolean noEventTypesFound(final PageParameters pageParameters) {
		FileNormalizer fileNormalizer;
		final String fileName = pageParameters.get("filePath").toString();
		final int index = fileName.lastIndexOf('.');
		final String fileExtension = fileName.substring(index + 1, fileName.length());

		fileNormalizer = (fileExtension.toLowerCase().contains("xls")) ? new ExcelImporter() : new CSVImporter();

		final List<String> attributeNames = fileNormalizer.getColumnTitlesFromFile(pageParameters.get("filePath")
				.toString());
		final List<String> trimmedAttributeNames = new ArrayList<String>();
		for (final String attributeName : attributeNames) {
			trimmedAttributeNames.add(attributeName.replace(" +", "_"));
		}
		return EapEventType.findMatchingEventTypesForNonHierarchicalAttributes(trimmedAttributeNames,
				ExcelEventTypeCreator.GENERATED_TIMESTAMP_COLUMN_NAME).isEmpty();
	}
}
