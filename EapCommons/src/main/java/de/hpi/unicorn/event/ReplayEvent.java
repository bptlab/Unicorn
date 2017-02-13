package de.hpi.unicorn.event;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

public class ReplayEvent extends EapEvent {

	private static final long serialVersionUID = 7590794952828117149L;
	private Long offset;

	public ReplayEvent() {
		super();
	}

	public ReplayEvent(EapEvent e) {
		this(e.getEventType(), e.getTimestamp(), e.getValues());
	}

	public ReplayEvent(EapEventType eventType, Date timestamp, Map<String, Serializable> values) {
		super(eventType, timestamp, values);
	}

	public void setOffset(Long offset) {
		this.offset = offset;
	}

	/**
	 * Returns the offset of this ReplayEvent. Prints a warning and returns 0 if
	 * offset was not set before.
	 * 
	 * @return the offset
	 */
	public Long getOffset() {
		if (offset == null) {
			System.out.println("Offset is not set, return 0");
			return 0l;
		} else {
			return offset;
		}
	}

	/**
	 * Returns the timestamp of this ReplayEvent as milliseconds
	 * 
	 * @return Long
	 */
	public Long getTime() {
		return timestamp.getTime();
	}

}
