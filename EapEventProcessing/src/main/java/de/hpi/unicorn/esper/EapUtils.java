/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.esper;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Node;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.EapEventType;

/**
 * This class contains methods that have been registered to Esper. This
 * registration takes place in the @see StreamProcessingAdapter. These methods
 * can be used in Esper queries.
 */
public class EapUtils {

	public static Date currentDate() {
		return new Date();
	}

	/**
	 * Formats a date as defined in the format string.
	 * 
	 * @param date
	 * @param format
	 * @return formatted date
	 */
	public static String formatDate(final Date date, final String format) {
		final SimpleDateFormat dfmt = new SimpleDateFormat(format);
		return dfmt.format(date);
	}

	/**
	 * Parse string to date using given format.
	 * 
	 * @param date
	 * @param format
	 * @return Date
	 * @throws ParseException
	 */
	public static Date parseDate(final String date, final String format) throws ParseException {
		final SimpleDateFormat dfmt = new SimpleDateFormat(format);
		return dfmt.parse(date);
	}

	/**
	 * Checks whether several Integer-Lists have common elements
	 * 
	 * @param collectionOfIDLists
	 * @return true if intersection is not empty
	 */
	public static boolean isIntersectionNotEmpty(final List<Integer>... collectionOfIDLists) {
		if (collectionOfIDLists == null || collectionOfIDLists.length == 0) {
			return false;
		}
		final List<List<Integer>> copyListOfIDLists = new ArrayList<List<Integer>>();
		for (final List<Integer> list : collectionOfIDLists) {
			copyListOfIDLists.add(new ArrayList<Integer>(list));
		}
		final List<Integer> retainedIDs = copyListOfIDLists.get(0);
		if (retainedIDs.isEmpty()) {
			return false;
		}
		for (final List<Integer> list : copyListOfIDLists) {
			if (list != null) {
				retainedIDs.retainAll(list);
			}
		}
		return !retainedIDs.isEmpty();
	}

	/**
	 * Returns the common elements of several Integer-Lists.
	 * 
	 * @param collectionOfIDLists
	 * @return common elements of lists
	 */
	public static List<Integer> getIntersection(final List<Integer>... collectionOfIDLists) {
		if (collectionOfIDLists == null || collectionOfIDLists.length == 0) {
			return new ArrayList<Integer>();
		}
		final List<List<Integer>> copyListOfIDLists = new ArrayList<List<Integer>>();
		for (final List<Integer> list : collectionOfIDLists) {
			copyListOfIDLists.add(new ArrayList<Integer>(list));
		}
		final List<Integer> retainedIDs = copyListOfIDLists.get(0);
		if (retainedIDs.isEmpty()) {
			return retainedIDs;
		}
		for (final List<Integer> list : copyListOfIDLists) {
			retainedIDs.retainAll(list);
		}
		return retainedIDs;
	}

	/**
	 * Transforms an attribute-value of an event to an integer value.
	 * 
	 * @param eventTypeName
	 * @param attributeExpression
	 * @param array
	 * @return integer value
	 */
	public static Integer integerValueFromEvent(final String eventTypeName, final String attributeExpression,
			final Object[] array) {
		final Serializable value = EapUtils.findValueByEventTypeAndAttributeExpressionsAndValues(eventTypeName,
				attributeExpression, array);
		if (value != null) {
			try {
				return (Integer) value;
			} catch (final ClassCastException cce) {
				return new Integer(value.toString());
			}
		}
		return null;
	}

	/**
	 * Transforms an attribute value of an event to a double value.
	 * 
	 * @param eventTypeName
	 * @param attributeExpression
	 * @param array
	 * @return
	 */
	public static Double doubleValueFromEvent(final String eventTypeName, final String attributeExpression,
			final Object[] array) {
		final Serializable value = EapUtils.findValueByEventTypeAndAttributeExpressionsAndValues(eventTypeName,
				attributeExpression, array);
		if (value != null) {
			try {
				return (Double) value;
			} catch (final ClassCastException cce) {
				return new Double(value.toString());
			}
		}
		return null;
	}

	/**
	 * Transforms an attribute value of an event to a string value.
	 * 
	 * @param eventTypeName
	 * @param attributeExpression
	 * @param array
	 * @return
	 */
	public static String stringValueFromEvent(final String eventTypeName, final String attributeExpression,
			final Object[] array) {
		final Serializable value = EapUtils.findValueByEventTypeAndAttributeExpressionsAndValues(eventTypeName,
				attributeExpression, array);
		if (value != null) {
			return value.toString();
		}
		return null;
	}

	/**
	 * Transforms an attribute value of an event to a date value.
	 * 
	 * @param eventTypeName
	 * @param attributeExpression
	 * @param array
	 * @return
	 */
	public static Date dateValueFromEvent(final String eventTypeName, final String attributeExpression,
			final Object[] array) {
		final Serializable value = EapUtils.findValueByEventTypeAndAttributeExpressionsAndValues(eventTypeName,
				attributeExpression, array);
		if (value != null) {
			try {
				return (Date) value;
			} catch (final ClassCastException cce) {
				final SimpleDateFormat sdfToDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try {
					return sdfToDate.parse(value.toString());
				} catch (final ParseException pe) {
					return null;
				}
			}
		}
		return null;
	}

	/**
	 * Returns attribute values of events with a certain event type and
	 * attribute expressions.
	 * 
	 * @param eventTypeName
	 * @param attributeExpression
	 * @param array
	 * @return
	 */
	private static Serializable findValueByEventTypeAndAttributeExpressionsAndValues(final String eventTypeName,
			final String attributeExpression, final Object[] array) {
		final Map<String, Serializable> attributeExpressionsAndValues = new HashMap<String, Serializable>();
		for (int i = 0; i < array.length; i = i + 2) {
			attributeExpressionsAndValues.put((String) array[i], (Serializable) array[i + 1]);
		}
		return EapEvent.findValueByEventTypeAndAttributeExpressionsAndValues(
				EapEventType.findByTypeName(eventTypeName), attributeExpression, attributeExpressionsAndValues);
	}

	/**
	 * Sums up the attribute values of certain attribute from each event of an
	 * event list.
	 * 
	 * @param events
	 * @param attributeName
	 * @return
	 */
	public static Integer sumFromEventList(final Node[] events, final String attributeName) {
		String[] attributeNameByLevels = { attributeName };
		if (attributeName.contains(".")) {
			attributeNameByLevels = attributeName.split(".");
		}
		Integer result = 0;
		if (events != null) {
			for (final Node event : events) {
				Node currentAttribute = event;
				for (final String attributeNameByLevel : attributeNameByLevels) {
					for (int j = 0; j < currentAttribute.getChildNodes().getLength(); j++) {
						final Node childNode = currentAttribute.getChildNodes().item(j);
						if (childNode.getNodeName().equals(attributeNameByLevel)) {
							currentAttribute = childNode;
							break;
						}
					}
				}
				try {
					result += new Integer(currentAttribute.getFirstChild().getTextContent());
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		}
		return result;
	}

	/**
	 * Checks if string is part of another string.
	 * 
	 * @param substring
	 * @param mainString
	 * @return
	 */
	public static boolean isSubstringOf(final String substring, final String mainString) {
		return mainString.contains(substring);
	}
}
