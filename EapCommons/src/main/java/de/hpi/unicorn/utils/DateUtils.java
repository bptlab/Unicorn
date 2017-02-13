/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class centralizes methods to parse {@link Date}s.
 * 
 * @author micha
 */
public class DateUtils {

	private static DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	/**
	 * This method parses a {@link Date} from a string with the format
	 * dd.MM.yyyy kk:mm.
	 * 
	 * @param dateString
	 * @return
	 */
	public static Date parseDate(final String dateString) {
		Date date = null;
		DateFormat formatter;
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			date = formatter.parse(dateString);
		} catch (final ParseException d) {
			try {
				formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				date = formatter.parse(dateString);
			} catch (final ParseException e) {
				try {
					formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
					date = formatter.parse(dateString);
				} catch (final ParseException f) {
					try {
						formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						date = formatter.parse(dateString);
					} catch (final ParseException g) {
						try {
							formatter = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
							date = formatter.parse(dateString);
						} catch (final ParseException h) {
							try {
								formatter = new SimpleDateFormat("dd-MM-yyyy' 'HH:mm:ss");
								date = formatter.parse(dateString);
							} catch (final ParseException i) {
								try {
									formatter = new SimpleDateFormat("yyyy-MM-dd");
									date = formatter.parse(dateString);
								} catch (final ParseException j) {
									try {
										formatter = new SimpleDateFormat("dd.MM.yyyy kk:mm");
										date = formatter.parse(dateString);

									} catch (final ParseException k) {
										try {
											// Aug 6, 2014 3:52:29 PM
											formatter = new SimpleDateFormat("MMM d, yyyy h:mm:ss a");
											date = formatter.parse(dateString);
										} catch (final ParseException l) {
											try {
												// 2013-07-14T23:30:59+02:00
												formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
												date = formatter.parse(dateString);
											} catch (final ParseException m) {
												// System.out.println("hpi.eap.DateUtils - date not parseable: "
												// + dateString);
												try {
													long longDate = Long.parseLong(dateString);
													date = new Date(longDate * 1000);
												} catch (final NumberFormatException n) {
													n.printStackTrace();
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return date;
	}

	public static Date parseDurationAsDate(final String durationString) {
		Date date = null;
		DateFormat formatter;
		// formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
		try {
			formatter = new SimpleDateFormat("HH:mm:ss:SSS");
			date = formatter.parse(durationString);
		} catch (final ParseException d) {
			try {
				formatter = new SimpleDateFormat("HH:mm:ss");
				date = formatter.parse(durationString);
			} catch (final ParseException e) {
				try {
					formatter = new SimpleDateFormat("mm:ss");
					date = formatter.parse(durationString);
				} catch (final ParseException f) {
					// System.out.println("hpi.eap.DateUtils - date not parseable: "
					// + durationString);
					f.printStackTrace();
				}
			}
		}
		return date;
	}

	public static boolean isDate(final String dateString) {
		// return parseDate(dateString) != null;
		DateFormat formatter;
		try {
			formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
			formatter.parse(dateString);
			return true;
		} catch (final ParseException d) {
			try {
				formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
				formatter.parse(dateString);
				return true;
			} catch (final ParseException e) {
				try {
					formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
					formatter.parse(dateString);
					return true;
				} catch (final ParseException f) {
					try {
						formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
						formatter.parse(dateString);
						return true;
					} catch (final ParseException g) {
						try {
							formatter = new SimpleDateFormat("yyyy-MM-dd");
							formatter.parse(dateString);
							return true;
						} catch (final ParseException h) {
							try {
								formatter = new SimpleDateFormat("dd.MM.yyyy kk:mm");
								formatter.parse(dateString);
								return true;
							} catch (final ParseException i) {
								return false;
							}
						}
					}
				}
			}
		}
	}

	public static DateFormat getFormatter() {
		return DateUtils.formatter;
	}
}
