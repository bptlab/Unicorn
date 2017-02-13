/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.csv;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.utils.TempFolderUtil;
import de.hpi.unicorn.utils.XMLUtils;

public class CSVExporter {

	public File generateExportFile(final EapEventType eventType, final List<EapEvent> events) {
		// create file
		if (eventType.isHierarchical()) {
			return null;
		}
		final File file = new File(TempFolderUtil.getFolder() + System.getProperty("file.separator")
				+ eventType.getTypeName() + "export.csv");
		try {
			final FileWriter writer = new FileWriter(file, false);
			final List<String> attributeNames = eventType.getNonHierarchicalAttributeExpressionsWithoutTimestamp();
			writer.write(eventType.getTimestampName() + ";");
			for (final String attributeName : attributeNames) {
				writer.write(attributeName + ";");
			}
			for (final EapEvent event : events) {
				writer.write(System.getProperty("line.separator"));
				writer.write(XMLUtils.getXMLDate(event.getTimestamp()) + ";");
				final Map<String, String> values = event.getValuesForExport();
				for (final String attributeName : attributeNames) {
					writer.write(values.get(attributeName) + ";");
				}
			}
			// write stream to file
			writer.flush();
			// close the stream
			writer.close();
		} catch (final IOException e1) {
			e1.printStackTrace();
		}
		return file;
	}
}
