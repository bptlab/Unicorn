/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hpi.unicorn.event.EapEventType;
import de.hpi.unicorn.event.attribute.AttributeTypeEnum;
import de.hpi.unicorn.event.attribute.TypeTreeNode;

public class ConversionUtils {

	public static Map<String, String> getValuesConvertedToString(final Map<String, Serializable> valuesToBeConverted) {
		final Map<String, String> convertedValues = new HashMap<String, String>();
		for (final String key : valuesToBeConverted.keySet()) {
			final Serializable value = valuesToBeConverted.get(key);
			if (value != null) {
				if (value instanceof Date) {
					convertedValues.put(key, DateUtils.getFormatter().format(value));
				} else if (value instanceof Integer) {
					convertedValues.put(key, String.valueOf(value));
				} else if (value instanceof Double) {
					convertedValues.put(key, String.valueOf(value));
				} else {
					convertedValues.put(key, value.toString());
				}
			}
		}
		return convertedValues;
	}

	public static Map<String, Serializable> getValuesConvertedToSerializable(final EapEventType eventType,
			final Map<String, String> valuesToBeConverted) {
		final Map<String, Serializable> convertedValues = new HashMap<String, Serializable>();
		if (eventType != null) {
			for (String key : valuesToBeConverted.keySet()) {
				final String value = valuesToBeConverted.get(key);
				if (value != null && value != "") {
					key = key.trim().replaceAll(" +", "_").replaceAll("[^a-zA-Z0-9_.]+", "");
					final AttributeTypeEnum type = eventType.getValueTypeTree().getAttributeByExpression(key).getType();
					ConversionUtils.addConvertedValueToEvent(key, value, type, convertedValues);
				}
			}
		} else {
			for (final String key : valuesToBeConverted.keySet()) {
				convertedValues.put(key, valuesToBeConverted.get(key));
			}
		}
		return convertedValues;
	}

	public static Map<String, Serializable> getValuesConvertedToSerializable(final List<TypeTreeNode> attributes,
			final Map<String, String> valuesToBeConverted) {
		final Map<String, Serializable> convertedValues = new HashMap<String, Serializable>();
		if (attributes != null) {
			for (String key : valuesToBeConverted.keySet()) {
				final String value = valuesToBeConverted.get(key);
				if (value != null) {
					key = key.trim().replaceAll(" +", "_").replaceAll("[^a-zA-Z0-9_.]+", "");
					AttributeTypeEnum type = AttributeTypeEnum.STRING;
					for (final TypeTreeNode attribute : attributes) {
						if (attribute.getAttributeExpression().equals(key)) {
							type = attribute.getType();
							break;
						}
					}
					ConversionUtils.addConvertedValueToEvent(key, value, type, convertedValues);
				}
			}
		} else {
			for (final String key : valuesToBeConverted.keySet()) {
				convertedValues.put(key, valuesToBeConverted.get(key));
			}
		}
		return convertedValues;
	}

	private static void addConvertedValueToEvent(final String key, String value, final AttributeTypeEnum type,
			final Map<String, Serializable> values) {
		if (type != null) {
			switch (type) {
			case DATE:
				values.put(key, DateUtils.parseDate(value));
				break;
			case INTEGER:
				// replace non-numeric symbols in value String
				value = value.replaceAll("[^\\d-]", "");
				long longValue = 0;
				try {
					longValue = Long.parseLong(value);
				} catch (final NumberFormatException e) {
					e.printStackTrace();
					System.out.println("[ConversionUtils: parsing of attribute value failed (long), use 0");
				}
				values.put(key, longValue);
				break;
			case FLOAT:
				// replace non-numeric symbols in value String
				value = value.replaceAll("[^\\d.-]", "");
				double doubleValue = 0;
				try {
					doubleValue = Double.parseDouble(value);
				} catch (final NumberFormatException e) {
					e.printStackTrace();
					System.out.println("[ConversionUtils: parsing of attribute value failed (double), use 0");
				}
				values.put(key, doubleValue);
				break;
			default:
				values.put(key, value);
				break;
			}
		}
	}

	/**
	 * 
	 * @param eventType
	 * @param eventValues
	 * @return name of attribute with invalid value
	 */
	public static String validateEvent(final EapEventType eventType, final Map<String, String> eventValues) {
		for (final String key : eventValues.keySet()) {
			final TypeTreeNode attribute = eventType.getValueTypeTree().getAttributeByExpression(key);
			if (attribute == null) {
				return key;
			} else {
				final AttributeTypeEnum type = attribute.getType();
				if (type == null) {
					return key;
				} else {
					try {
						if (type != null) {
							final String value = eventValues.get(key);
							switch (type) {
							case DATE:
								final Date date = DateUtils.parseDate(value);
								if (date == null) {
									return key;
								}
								break;
							case INTEGER:
								Long.parseLong(value);
								break;
							case FLOAT:
								Double.parseDouble(value);
								break;
							default:
								break;
							}
						}
					} catch (final Exception e) {
						return key;
					}
				}
			}
		}
		return null;

	}
}
