/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;
import de.hpi.unicorn.utils.DateUtils;

/**
 * This class imports events from an excel file.
 */
public class ExcelImporter extends FileNormalizer implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates an {@link HSSFWorkbook} from an excel file.
	 */
	private HSSFWorkbook loadWorkbook(final String fileName) {
		try {
			final InputStream myxls = new FileInputStream(fileName);
			return new HSSFWorkbook(myxls);
		} catch (final IOException e) {
			System.err.println("Workbook could not be load.");
			return null;
		}
	}

	@Override
	public ArrayList<String> getColumnTitlesFromFile(final String fileName) {
		final HSSFWorkbook workbook = this.loadWorkbook(fileName);
		final HSSFSheet firstSheet = workbook.getSheetAt(0);
		return this.generateColumnTitlesFromSheet(firstSheet);
	}

	/**
	 * Returns the column titles from the given workbook sheet.
	 */
	private ArrayList<String> generateColumnTitlesFromSheet(final HSSFSheet sheet) {
		final HSSFRow firstRow = sheet.getRow(0);
		if (firstRow != null) {
			final ArrayList<String> columnTitles = new ArrayList<String>();
			for (final Cell currentCell : firstRow) {
				columnTitles.add(currentCell.getStringCellValue().trim().replaceAll(" +", "_")
						.replaceAll("[^a-zA-Z0-9_]+", ""));
			}
			return columnTitles;
		} else {
			return new ArrayList<String>();
		}

	}

	@Override
	public List<ImportEvent> importEventsForPreviewFromFile(final String filePath,
			final List<String> selectedColumnTitles) throws IllegalArgumentException {

		final List<ImportEvent> eventList = new ArrayList<ImportEvent>();
		final List<String> columnTitles = this.getColumnTitlesFromFile(filePath);
		if (!columnTitles.isEmpty()) {
			final String timestampName = this.getTimestampColumn(selectedColumnTitles);
			final Iterator<Row> rowIterator = this.getRowIterator(filePath);

			// Eliminate headline row
			rowIterator.next();

			while (rowIterator.hasNext()) {
				final HSSFRow actualRow = (HSSFRow) rowIterator.next();
				if (!this.isRowEmpty(actualRow)) {
					eventList.add(this.generateImportEventFromRow(actualRow, selectedColumnTitles, columnTitles,
							timestampName));
				}
			}
		}
		return eventList;
	}

	@Override
	public List<EapEvent> importEventsFromFile(final String filePath, final List<TypeTreeNode> selectedAttributes,
			final String timestamp) {
		final List<EapEvent> eventList = new ArrayList<EapEvent>();
		final List<String> columnTitles = this.getColumnTitlesFromFile(filePath);
		final List<String> selectedColumnTitles = new ArrayList<String>();
		if (!columnTitles.isEmpty()) {

			for (final TypeTreeNode attribute : selectedAttributes) {
				selectedColumnTitles.add(attribute.getName());
			}

			final int timeStampColumnIndex = columnTitles.indexOf(timestamp);
			final Iterator<Row> rowIterator = this.getRowIterator(filePath);

			// Eliminate headline row
			rowIterator.next();

			while (rowIterator.hasNext()) {
				final HSSFRow actualRow = (HSSFRow) rowIterator.next();
				if (!this.isRowEmpty(actualRow)) {
					eventList.add(this.generateEventFromRow(actualRow, selectedColumnTitles, selectedAttributes,
							columnTitles, timeStampColumnIndex));
				}
			}
		}
		return eventList;
	}

	/**
	 * Generates an import event for preview from the given row. The difference
	 * to the normal creation is the treatment of the date.
	 * 
	 * @param actualRow
	 * @param selectedColumnTitles
	 * @param columnTitles
	 * @param timestampName
	 */
	private ImportEvent generateImportEventFromRow(final HSSFRow actualRow, final List<String> selectedColumnTitles,
			final List<String> columnTitles, final String timestampName) {
		Date timestamp;
		final int timestampColumnIndex = columnTitles.indexOf(timestampName);
		if (timestampColumnIndex > -1) {
			timestamp = actualRow.getCell(timestampColumnIndex).getDateCellValue();
		} else {
			timestamp = null;
		}
		final Map<String, Serializable> values = this.generateValueTree(actualRow, selectedColumnTitles, columnTitles,
				timestampColumnIndex);
		final ImportEvent event = new ImportEvent(timestamp, values, timestampName, new Date());
		return event;
	}

	/**
	 * Imports an event from the given row.
	 * 
	 * @param actualRow
	 * @param selectedColumnTitles
	 * @param selectedAttributes
	 * @param columnTitles
	 * @param timeStampColumnIndex
	 */
	private EapEvent generateEventFromRow(final HSSFRow actualRow, final List<String> selectedColumnTitles,
			final List<TypeTreeNode> selectedAttributes, final List<String> columnTitles, final int timeStampColumnIndex) {
		Date timestamp;
		// Falls kein TimeStamp gefunden wurde, wird die aktuelle Einlesezeit
		// verwendet
		if (timeStampColumnIndex > -1) {
			try {
				timestamp = actualRow.getCell(timeStampColumnIndex).getDateCellValue();
			} catch (final IllegalStateException e) {
				timestamp = new Date();
			}
		} else {
			timestamp = new Date();
		}
		final Map<String, Serializable> values = this.generateValueTree(actualRow, selectedColumnTitles,
				selectedAttributes, columnTitles, timeStampColumnIndex);
		final EapEvent event = new EapEvent(timestamp, values);
		return event;
	}

	private Iterator<Row> getRowIterator(final String filePath) {
		final HSSFWorkbook workbook = this.loadWorkbook(filePath);
		final HSSFSheet firstSheet = workbook.getSheetAt(0);
		final Iterator<Row> rowIterator = firstSheet.rowIterator();
		return rowIterator;
	}

	/**
	 * Returns true, if a {@link HSSFRow} has no values.
	 * 
	 * @param actualRow
	 */
	private boolean isRowEmpty(final HSSFRow actualRow) {
		boolean emptyRow = true;
		final Iterator<Cell> cellIterator = actualRow.cellIterator();
		while (cellIterator.hasNext()) {
			final Cell actualCell = cellIterator.next();
			emptyRow = (actualCell.getCellType() != Cell.CELL_TYPE_BLANK) ? false : true;
		}
		return emptyRow;
	}

	/**
	 * Returns a new {@link Map} from the given row with the values for the
	 * selected columns.
	 * 
	 * @param actualRow
	 * @param selectedColumnTitles
	 * @param columnTitles
	 * @param timeStampColumnIndex
	 * @return
	 */
	private Map<String, Serializable> generateValueTree(final HSSFRow actualRow,
			final List<String> selectedColumnTitles, final List<String> columnTitles, final int timeStampColumnIndex) {
		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		final Iterator<Cell> cellIterator = actualRow.cellIterator();
		while (cellIterator.hasNext()) {
			final Cell actualCell = cellIterator.next();
			final String attributeName = columnTitles.get(actualCell.getColumnIndex());
			if (selectedColumnTitles.contains(attributeName) && actualCell.getColumnIndex() != timeStampColumnIndex) {
				final Serializable attributeValue = this.getAttributeValue(null, actualCell);
				values.put(attributeName, attributeValue);

			}
		}
		return values;
	}

	/**
	 * Returns a new {@link Map} from the given row with the values for the
	 * selected columns. It is possible to specify the selected
	 * {@link TypeTreeNode}s.
	 * 
	 * @param actualRow
	 * @param selectedColumnTitles
	 * @param columnTitles
	 * @param timeStampColumnIndex
	 * @return
	 */
	private Map<String, Serializable> generateValueTree(final HSSFRow actualRow,
			final List<String> selectedColumnTitles, final List<TypeTreeNode> selectedAttributes,
			final List<String> columnTitles, final int timeStampColumnIndex) {
		final Map<String, Serializable> values = new HashMap<String, Serializable>();
		final Iterator<Cell> cellIterator = actualRow.cellIterator();
		AttributeTypeEnum attributeType = null;
		while (cellIterator.hasNext()) {
			final Cell actualCell = cellIterator.next();
			final String attributeName = columnTitles.get(actualCell.getColumnIndex());
			for (final TypeTreeNode attribute : selectedAttributes) {
				if (attribute.getName().equals(attributeName)) {
					attributeType = attribute.getType();
				}
			}
			if (selectedColumnTitles.contains(attributeName) && actualCell.getColumnIndex() != timeStampColumnIndex) {
				final Serializable attributeValue = this.getAttributeValue(attributeType, actualCell);
				values.put(attributeName, attributeValue);
			}
		}
		return values;
	}

	private Serializable getAttributeValue(final AttributeTypeEnum attributeType, final Cell actualCell) {
		Serializable attributeValue = null;

		if (attributeType == null) {
			switch (actualCell.getCellType()) {
			case Cell.CELL_TYPE_STRING:
				if (actualCell.toString().contains("0-0-0000 00:00")) {
					attributeValue = new Date(0);
				} else if (actualCell.toString().matches(".*[0-9]{2}:[0-9]{2}")) {
					attributeValue = DateUtils.parseDurationAsDate(actualCell.toString());
				} else {
					attributeValue = actualCell.getStringCellValue();
				}
				break;
			case Cell.CELL_TYPE_NUMERIC:
				double value = 0.0;
				try {
					value = actualCell.getNumericCellValue();
				} catch (final NumberFormatException e) {
					// e.printStackTrace();
				} catch (final IllegalStateException e2) {
					// e2.printStackTrace();
				}
				if (actualCell.toString().matches(".*E[0-9]{2}")) {
					attributeValue = Double.valueOf(value).longValue();
				} else if (!actualCell.getCellStyle().getDataFormatString().equals("General")
						&& this.isDateFormatted(actualCell)) {
					attributeValue = DateUtil.getJavaDate(Double.valueOf(value));
				} else if (actualCell.getCellStyle().getDataFormatString().equals("General")
						&& !this.isDateFormatted(actualCell)) {
					attributeValue = Double.valueOf(value);
				} else {
					attributeValue = Double.valueOf(value);
				}

			}
		} else {
			if (attributeType.equals(AttributeTypeEnum.DATE)) {
				if (actualCell.toString().contains("0-0-0000 00:00")) {
					attributeValue = new Date(0);
				} else {
					try {
						attributeValue = actualCell.getDateCellValue();
					} catch (final IllegalStateException e) {
						attributeValue = new Date(0);
					}
				}
			} else if (attributeType.equals(AttributeTypeEnum.FLOAT)) {
				switch (actualCell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					if (actualCell.toString().contains("0-0-0000 00:00")) {
						attributeValue = new Date(0);
					} else {
						attributeValue = Double.valueOf(actualCell.getStringCellValue());
					}
					break;
				case Cell.CELL_TYPE_NUMERIC:
					if (!actualCell.getCellStyle().getDataFormatString().equals("General")
							&& this.isDateFormatted(actualCell)) {
						final Double cellValue = new Double(actualCell.getNumericCellValue());
						attributeValue = DateUtil.getJavaDate(cellValue);
					} else if (actualCell.getCellStyle().getDataFormatString().equals("General")
							&& !this.isDateFormatted(actualCell)) {
						attributeValue = Double.valueOf(actualCell.getNumericCellValue());
					} else {
						attributeValue = new Date(0);
					}
				}
			} else if (attributeType.equals(AttributeTypeEnum.INTEGER)) {
				switch (actualCell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					try {
						attributeValue = Double.valueOf(actualCell.getStringCellValue()).longValue();
					} catch (final NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				case Cell.CELL_TYPE_NUMERIC:
					attributeValue = Double.valueOf(actualCell.getNumericCellValue()).longValue();
				}
			} else if (attributeType.equals(AttributeTypeEnum.STRING)) {
				attributeValue = actualCell.toString();
			}
		}
		return attributeValue;
	}

	private boolean isDateFormatted(final Cell actualCell) {
		boolean date = false;
		try {
			date = DateUtil.isCellDateFormatted(actualCell);
		} catch (final IllegalStateException e2) {
			// Nothing to do -> meaning cell is not date formatted
		}
		return date;
	}

}
