/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.excel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

/**
 * This class centralizes common methods for the import of files.
 */
public abstract class FileNormalizer implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Returns the titles as list for files with tabular content.
	 * 
	 * @param filePath
	 * @return
	 */
	public abstract List<String> getColumnTitlesFromFile(String filePath);

	/**
	 * Imports events from a file for a preview to decide, which parts of the
	 * data should be user for the event creation.
	 * 
	 * @param filePath
	 * @param selectedColumnTitles
	 * @return
	 */
	public abstract List<ImportEvent> importEventsForPreviewFromFile(String filePath, List<String> selectedColumnTitles);

	/**
	 * Imports data from a file and creates events from this data.
	 * 
	 * @param filePath
	 * @param selectedAttributes
	 * @return
	 * @throws IllegalArgumentException
	 */
	public List<EapEvent> importEventsFromFile(final String filePath, final List<TypeTreeNode> selectedAttributes)
			throws IllegalArgumentException {
		final String timeStampColumn = this.getTimestampColumnFromAttribute(selectedAttributes);
		return this.importEventsFromFile(filePath, selectedAttributes, timeStampColumn);
	}

	/**
	 * Imports data from a file and creates events from this data.
	 * 
	 * @param filePath
	 * @param selectedAttributes
	 * @param timestamp
	 * @return
	 */
	public abstract List<EapEvent> importEventsFromFile(String filePath, List<TypeTreeNode> selectedAttributes,
			String timestamp);

	/**
	 * Tries to identify a timestamp from the given attributes.
	 * 
	 * @param attributes
	 * @return
	 */
	protected String getTimestampColumnFromAttribute(final List<TypeTreeNode> attributes) {
		final ArrayList<String> columnTitles = new ArrayList<String>();
		for (final TypeTreeNode attribute : attributes) {
			if (attribute.getType() == AttributeTypeEnum.DATE) {
				columnTitles.add(attribute.getName());
			} else if (attribute.getType() == AttributeTypeEnum.FLOAT) {
				columnTitles.add(attribute.getName());
			}
		}
		return this.getTimestampColumn(columnTitles);
	}

	/**
	 * Tries to identify a timestamp column from the given attributes.
	 * 
	 * @param columnTitles
	 * @return
	 */
	protected String getTimestampColumn(final List<String> columnTitles) {
		for (int i = 0; i < columnTitles.size(); i++) {
			final String actualColumnTitle = columnTitles.get(i);
			for (final TimeStampNames columnName : TimeStampNames.values()) {
				if (columnName.toString().equals(actualColumnTitle.toUpperCase())) {
					return actualColumnTitle;
				}
			}
		}
		return null;
	}
}
