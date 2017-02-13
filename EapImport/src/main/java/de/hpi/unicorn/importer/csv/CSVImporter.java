/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.csv;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.importer.excel.FileNormalizer;
import de.hpi.unicorn.importer.excel.ImportEvent;
import de.hpi.unicorn.utils.DateUtils;

/**
 * Adapter for CSV Files
 */
public class CSVImporter extends FileNormalizer {

	private static final long serialVersionUID = 1L;
	private final char maskChar = '"';
	private char separator = ';';

	@Override
	public List<String> getColumnTitlesFromFile(final String filePath) {
		String line;
		String[] split;
		try {
			final FileReader file = new FileReader(filePath);
			final BufferedReader data = new BufferedReader(file);

			line = data.readLine();
			// if seperator is defined replace default seperator (;) and move
			// one row forward
			if (line.startsWith("sep=")) {
				split = line.split("=");
				this.separator = split[1].charAt(0);
				line = data.readLine();
			}
			return this.getElementsFromLine(line);
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public List<ImportEvent> importEventsForPreviewFromFile(final String filePath,
			final List<String> selectedColumnTitles) {
		String line;
		String[] split;
		List<String> lineElements;
		final List<ImportEvent> eventList = new ArrayList<ImportEvent>();
		final List<Integer> selectedColumnIndexes = new ArrayList<Integer>();
		final List<String> columnTitles = this.getColumnTitlesFromFile(filePath);
		final String timestampName = this.getTimestampColumn(columnTitles);

		try {
			final FileReader file = new FileReader(filePath);
			final BufferedReader data = new BufferedReader(file);

			line = data.readLine();
			// if separator is defined replace default separator (;) and move
			// one row forward
			if (line.startsWith("sep=")) {
				split = line.split("=");
				this.separator = split[1].charAt(0);
				line = data.readLine();
			}
			lineElements = this.getElementsFromLine(line);
			for (int i = 0; i < lineElements.size(); i++) {
				if (selectedColumnTitles.contains(lineElements.get(i))) {
					selectedColumnIndexes.add(i);
				}
			}
			while ((line = data.readLine()) != null) {
				lineElements = this.getElementsFromLine(line);
				eventList.add(this.generateImportEventFromRow(lineElements, columnTitles, selectedColumnIndexes,
						timestampName));
			}
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return eventList;
	}

	@Override
	public List<EapEvent> importEventsFromFile(final String filePath, final List<TypeTreeNode> selectedAttributes,
			final String timestamp) {
		String line;
		String[] split;
		List<String> lineElements;
		final List<EapEvent> eventList = new ArrayList<EapEvent>();
		final List<Integer> selectedColumnIndexes = new ArrayList<Integer>();
		final List<String> columnTitles = this.getColumnTitlesFromFile(filePath);
		final List<String> selectedColumnTitles = new ArrayList<String>();
		final int timeStampColumnIndex = columnTitles.indexOf(timestamp);

		for (final TypeTreeNode attribute : selectedAttributes) {
			if (attribute.getEventType() != null) {
				if (!attribute.isTimestamp())
					selectedColumnTitles.add(attribute.getAttributeExpression());
			} else {
				selectedColumnTitles.add(attribute.getName());
			}
		}

		try {
			final FileReader file = new FileReader(filePath);
			final BufferedReader data = new BufferedReader(file);

			line = data.readLine();
			// if seperator is defined replace default seperator (;) and move
			// one row forward
			if (line.contains("sep")) {
				split = line.split("=");
				separator = split[1].charAt(0);
				line = data.readLine();
			}
			// split = line.split(Character.toString(separator));
			split = getElementsFromLine(line).toArray(new String[0]);
			for (int i = 0; i < split.length; i++) {
				if (selectedColumnTitles.contains(split[i])) {
					selectedColumnIndexes.add(i);
				}
			}
			while ((line = data.readLine()) != null) {
				// split = line.split(Character.toString(separator));
				lineElements = this.getElementsFromLine(line);
				eventList.add(this.generateEventFromRow(lineElements, columnTitles, selectedAttributes,
						selectedColumnIndexes, timeStampColumnIndex));
			}
		} catch (final IOException e) {
			e.printStackTrace();
		}
		return eventList;
	}

	private ImportEvent generateImportEventFromRow(final List<String> rowSplit, final List<String> columnTitles,
			final List<Integer> selectedColumnIndexes, final String timestampName) {
		Date timestamp = null;
		final int timestampColumnIndex = columnTitles.indexOf(timestampName);
		if (timestampColumnIndex > -1) {
			// try {
			timestamp = DateUtils.parseDate(rowSplit.get(timestampColumnIndex));
			// } catch (ParseException e) {
			// e.printStackTrace();
			// }
		}
		final Map<String, Serializable> values = this.generateValues(rowSplit, columnTitles, selectedColumnIndexes,
				timestampColumnIndex);
		final ImportEvent event = new ImportEvent(timestamp, values, timestampName, new Date());
		return event;

	}

	private EapEvent generateEventFromRow(final List<String> rowSplit, final List<String> columnTitles,
			final List<TypeTreeNode> selectedAttributes, final List<Integer> selectedColumnIndexes,
			final int timestampColumnIndex) {
		// Default value
		Date timestamp = new Date();
		if (timestampColumnIndex > -1) {
			// try {
			timestamp = DateUtils.parseDate(rowSplit.get(timestampColumnIndex));
			// } catch (ParseException e) {
			// e.printStackTrace();
			// }
		}

		final Map<String, Serializable> values = this.generateValues(rowSplit, columnTitles, selectedAttributes,
				selectedColumnIndexes, timestampColumnIndex);
		final EapEvent event = new EapEvent(timestamp, values);
		return event;
	}

	private Map<String, Serializable> generateValues(final List<String> rowSplit, final List<String> columnTitles,
			final List<Integer> selectedColumnIndexes, final int timeStampColumnIndex) {
		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		for (final int i : selectedColumnIndexes) {
			if (i != timeStampColumnIndex) {
				values.put(columnTitles.get(i), rowSplit.get(i));
			}
		}
		return values;
	}

	private Map<String, Serializable> generateValues(final List<String> rowSplit, final List<String> columnTitles,
			final List<TypeTreeNode> selectedAttributes, final List<Integer> selectedColumnIndexes,
			final int timeStampColumnIndex) {
		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		AttributeTypeEnum attributeType = null;
		for (final int i : selectedColumnIndexes) {
			if (i != timeStampColumnIndex) {
				final String attributeName = columnTitles.get(i);
				for (final TypeTreeNode attribute : selectedAttributes) {
					if (attribute.getAttributeExpression().equals(attributeName)) {
						attributeType = attribute.getType();
					}
				}
				Serializable attributeValue = rowSplit.get(i);
				if (attributeType != null) {
					if (attributeType.equals(AttributeTypeEnum.DATE)) {
						attributeValue = DateUtils.parseDate(rowSplit.get(i));
					} else if (attributeType.equals(AttributeTypeEnum.INTEGER)) {
						attributeValue = Long.parseLong(rowSplit.get(i));
					} else if (attributeType.equals(AttributeTypeEnum.FLOAT)) {
						attributeValue = Double.parseDouble(rowSplit.get(i));
					}else {
						attributeValue = rowSplit.get(i);
					}
				}
				values.put(attributeName, attributeValue);
			}
		}
		return values;
	}

	private List<String> getElementsFromLine(final String line) {
		final List<String> elements = new ArrayList<String>();
		boolean entryMasked = false;
		char lastCharacter = 0;
		char currentCharacter = 0;
		StringBuilder currentElement = new StringBuilder();
		for (int i = 0; i < line.length(); i++) {
			currentCharacter = line.charAt(i);
			if (currentCharacter == this.maskChar) {
				if (entryMasked = true) {
					entryMasked = false;
				}
				if (currentElement.length() == 0) {
					entryMasked = true;
				}
			} else if (currentCharacter == this.separator && !entryMasked) {
				String currentElementString = currentElement.toString();
				if (lastCharacter == this.maskChar) {
					currentElementString = currentElement.substring(1, (currentElement.length() - 1));
				}
				elements.add(currentElementString);
				currentElement = new StringBuilder();
				currentCharacter = 0;
				continue;
			}
			lastCharacter = currentCharacter;
			currentElement.append(currentCharacter);
		}
		// Letztes Element auch noch ausgeben
		String currentElementString = currentElement.toString();
		if (lastCharacter == this.maskChar) {
			currentElementString = currentElement.substring(1, (currentElement.length() - 1));
		}
		elements.add(currentElementString);
		// TODO: Maskierungszeichen und Whitespaces entfernen
		return elements;
	}

	public void setSeparator(char separator) {
		this.separator = separator;
	}

}
