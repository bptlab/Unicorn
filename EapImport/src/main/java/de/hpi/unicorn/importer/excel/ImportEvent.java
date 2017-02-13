/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.excel;

import java.io.Serializable;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import de.hpi.unicorn.event.EapEvent;

/**
 * This class produces events for a preview. The events know their import time
 * and their timestamp, if any.
 * 
 * @author micha
 */
public class ImportEvent extends EapEvent {

	private static final long serialVersionUID = 8101339625770879566L;
	private final Date importTime;
	private final String extractedTimestampName;

	/**
	 * Constructor to create a new {@link ImportEvent}.
	 * 
	 * @param extractedTimestamp
	 * @param values
	 * @param extractedTimestampName
	 * @param currentTimestamp
	 */
	public ImportEvent(final Date extractedTimestamp, final Map<String, Serializable> values,
			final String extractedTimestampName, final Date currentTimestamp) {
		super(extractedTimestamp, values);
		this.extractedTimestampName = extractedTimestampName;
		this.importTime = currentTimestamp;
	}

	/**
	 * Returns the time of importing this event into the platform.
	 * 
	 * @return
	 */
	public Date getImportTime() {
		return this.importTime;
	}

	/**
	 * Returns the time of creation of this event.
	 * 
	 * @return
	 */
	public String getExtractedTimestampName() {
		return this.extractedTimestampName;
	}

	@Override
	public String toString() {
		String eventText = "Import event with ID: " + this.ID + " time: " + this.timestamp + " import time: "
				+ this.importTime.toString();
		final Map<String, Serializable> values = this.getValues();
		final Iterator<String> valueIterator = values.keySet().iterator();
		while (valueIterator.hasNext()) {
			final String valueKey = valueIterator.next();
			eventText = eventText + " " + valueKey + ": " + values.get(valueKey);
		}
		return eventText;
	}

}
