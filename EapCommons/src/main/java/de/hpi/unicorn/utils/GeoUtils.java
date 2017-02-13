/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.geotools.referencing.GeodeticCalculator;

/**
 * This class provides helper methods for geo calculations.
 * 
 * @author abaumgrass, mhewelt
 * 
 */
public class GeoUtils {

	private static double earthRadius = 6371; // in km

	/**
	 * 
	 * Returns the distance between two coordinates given as longitude and
	 * latitude
	 * 
	 * @param firstLongitude
	 * @param firstLatitude
	 * @param secondLongitude
	 * @param secondLatitude
	 * 
	 * @return distance (round to long) in meters
	 */
	public static Long getDistance(final String firstLongitude, final String firstLatitude,
			final String secondLongitude, final String secondLatitude) {
		return GeoUtils.getDistance(Double.valueOf(firstLongitude), Double.valueOf(firstLatitude),
				Double.valueOf(secondLongitude), Double.valueOf(secondLatitude));
	}

	/**
	 * 
	 * Returns the distance between two coordinates given as longitude and
	 * latitude
	 * 
	 * @param firstLongitude
	 * @param firstLatitude
	 * @param secondLongitude
	 * @param secondLatitude
	 * 
	 * @return distance (round to long)
	 */
	public static Long getDistance(final Double firstLongitude, final Double firstLatitude,
			final String secondLongitude, final String secondLatitude) {
		return GeoUtils.getDistance(firstLongitude, firstLatitude, Double.valueOf(secondLongitude),
				Double.valueOf(secondLatitude));
	}

	/**
	 * 
	 * Returns the distance between two coordinates given as longitude and
	 * latitude
	 * 
	 * @param firstLongitude
	 * @param firstLatitude
	 * @param secondLongitude
	 * @param secondLatitude
	 * 
	 * @return distance (round to long)
	 */
	public static Long getDistance(final String firstLongitude, final String firstLatitude,
			final Double secondLongitude, final Double secondLatitude) {
		return GeoUtils.getDistance(Double.valueOf(firstLongitude), Double.valueOf(firstLatitude), secondLongitude,
				secondLatitude);
	}

	/**
	 * 
	 * Returns the distance between two coordinates given as longitude and
	 * latitude
	 * 
	 * @param firstLongitude
	 * @param firstLatitude
	 * @param secondLongitude
	 * @param secondLatitude
	 * 
	 * @return distance (round to long)
	 */
	public static Long getDistance(final Double firstLongitude, final Double firstLatitude,
			final Double secondLongitude, final Double secondLatitude) {
		Double distance = 0.0;
		final GeodeticCalculator cal = new GeodeticCalculator();
		cal.setStartingGeographicPoint(firstLongitude, firstLatitude);
		cal.setDestinationGeographicPoint(secondLongitude, secondLatitude);
		distance = cal.getOrthodromicDistance();

		final int totalmeters = distance.intValue();
		final int km = totalmeters / 1000;
		float remaining_cm = (float) (distance - totalmeters) * 10000;
		remaining_cm = Math.round(remaining_cm);

		// System.out.println("Distance = " + km + "km " + meters + "m " + cm +
		// "cm");

		return distance.longValue();
	}

	public static class Coord {
		public double lat, lng;
		public String name;

		public Coord(double lat, double lng) {
			this.lat = lat;
			this.lng = lng;
		}

		public Coord(double lat, double lng, String name) {
			this(lat, lng);
			this.name = name;
		}

		public String toString() {
			return String.format(Locale.ENGLISH, "%s at <%f,%f>", name, lat, lng);
		}
	}

	/**
	 * Calculates the distance in meters between two coordinates using Haversine
	 * formula.
	 * 
	 * @param p1
	 * @param p2
	 * @return distance in meters
	 */
	public static double distance(Coord p1, Coord p2) {
		double phi1 = Math.toRadians(p1.lat);
		double phi2 = Math.toRadians(p2.lat);
		double deltaPhi = Math.toRadians(p1.lat - p2.lat);
		double deltaLambda = Math.toRadians(p1.lng - p2.lng);

		double a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) + Math.cos(phi1) * Math.cos(phi2)
				* Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		double distance = earthRadius * c;

		return distance * 1000;
	}

	/**
	 * Calculates the distance in km between two coordinates given as String
	 * pairs of latitude / longitude.
	 * 
	 * @param lat1
	 *            - latitude of first coordinate
	 * @param lng1
	 *            - longitude of first coordinate
	 * @param lat2
	 *            - latitude of second coordinate
	 * @param lng2
	 *            - longitude of second coordinate
	 * @return distance in km
	 */
	public static double distance(String lat1, String lng1, String lat2, String lng2) {
		return distance(new Coord(Double.parseDouble(lat1), Double.parseDouble(lng1)),
				new Coord(Double.parseDouble(lat2), Double.parseDouble(lng2)));
	}

	public static double distance(String lat1, String lng1, Double lat2, Double lng2) {
		return distance(new Coord(Double.parseDouble(lat1), Double.parseDouble(lng1)), new Coord(lat2, lng2));
	}

	public static double distance(Double lat1, Double lng1, Double lat2, Double lng2) {
		return distance(new Coord(lat1, lng1), new Coord(lat2, lng2));
	}

	/**
	 * Determines whether the Coord {@literal between} lies between
	 * {@literal p1} and {@literal p2} by comparing their latitude and
	 * longitude. Unfortunately, this fails in the case that {@literal p1} and
	 * {@literal p2} lie in the same quadrant, e.g. top-left, considering
	 * {@literal between} as the origin of a coordinate system.
	 * 
	 * @param p1
	 * @param p2
	 * @param between
	 * @return
	 */
	public static boolean inBetween(Coord p1, Coord p2, Coord between) {
		return (p1.lat < between.lat && between.lat < p2.lat) || (p1.lat > between.lat && between.lat > p2.lat)
				|| (p1.lng < between.lng && between.lng < p2.lng) || (p1.lng > between.lng && between.lng > p2.lng);
	}

	public static boolean inBetween(Double p1Lat, Double p1Lon, Double p2Lat, Double p2Lon, Double bLat, Double bLon) {
		return inBetween(new Coord(p1Lat, p1Lon), new Coord(p2Lat, p2Lon), new Coord(bLat, bLon));
	}

	public static boolean inBetween(String p1Lat, String p1Lon, String p2Lat, String p2Lon, String bLat, String bLon) {
		return inBetween(Double.parseDouble(p1Lat), Double.parseDouble(p1Lon), Double.parseDouble(p2Lat),
				Double.parseDouble(p2Lon), Double.parseDouble(bLat), Double.parseDouble(bLon));
	}

	public static double sumOfDistances(Coord[] coords, int step) {
		double sum = 0;
		for (int i = 0; i < coords.length - 1; i += step) {
			if (i + step >= coords.length) {
				double distance = distance(coords[i], coords[coords.length - 1]);
				// System.out.println(String.format("Distance between %s and %s: %.0f meter",
				// coords[i].name, coords[coords.length-1].name, distance));
				sum += distance;
			} else {
				double distance = distance(coords[i], coords[i + step]);
				// System.out.println(String.format("Distance between %s and %s: %.0f meter",
				// coords[i].name, coords[i+step].name, distance));
				sum += distance;
			}
		}
		return sum;
	}

	/**
	 * Select a subset of coordinates from an array with minimal error.
	 * Iterative implementation.
	 * 
	 * @param coords
	 * @return
	 */
	public static Coord[] optimizeCoordSelection(Coord[] coords, double epsilon) {

		// initialize result list, add first coordinate
		List<Coord> result = new ArrayList<GeoUtils.Coord>();
		result.add(coords[0]);

		for (int i = 0; i < coords.length;) {
			Coord selected = coords[i];
			double summedDistance = 0;
			double directDistance = 0;
			for (int j = i + 1; j < coords.length; j++) {
				Coord jth = coords[j];
				summedDistance += distance(coords[j - 1], coords[j]);
				directDistance = distance(selected, jth);
				if ((summedDistance - directDistance) > epsilon) {
					// select coordinate j - 1
					result.add(coords[j - 1]);
					i = j - 1; // progress outer loop
					break; // break inner loop
				}
				if (j == coords.length - 1) {
					// select last coordinate
					result.add(coords[j]);
					i = coords.length; // break outer loop
					break; // break inner loop
				}
			}
		}
		return result.toArray(new Coord[0]);
	}
}
