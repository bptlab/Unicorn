package de.hpi.unicorn.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/*
 * Loads a fixed JSON from file-system and enables parsing and transformation of route coordinates into a string for different purposes.
 */
public class CoordinatesHelper {

	public static void main(String[] args) {
		String content = "";
		try {
			content = new Scanner(new File("D:\\studium\\eap\\PTVcalculateRouteAnswer-AMS-COQ.json")).useDelimiter(
					"\\Z").next();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		InputStream jsonStream = new ByteArrayInputStream(content.getBytes(Charset.defaultCharset()));
		final StringBuilder s = new StringBuilder("name, latitude, longitude\n");
		JsonReader reader = Json.createReader(jsonStream);
		JsonObject root = reader.readObject();
		if (!root.containsKey("routes")) {
			System.out.println("No routes");
		} else {
			JsonArray routes = root.getJsonArray("routes");
			if (routes.size() >= 1) {
				JsonObject firstRoute = routes.getJsonObject(0);
				if (firstRoute.containsKey("polygon")) {
					JsonArray points = firstRoute.getJsonObject("polygon").getJsonArray("points");
					for (int i = 0; i < points.size(); i++) {
						JsonObject point = points.getJsonObject(i);
						if (point.containsKey("y")) {
							s.append(Math.floor(point.getJsonNumber("y").doubleValue() * 1000) / 1000);
							// s.append(point.getJsonNumber("y").doubleValue());
							s.append(",");
						}
						if (point.containsKey("x")) {
							s.append(Math.floor(point.getJsonNumber("x").doubleValue() * 1000) / 1000);
							// s.append(point.getJsonNumber("x").doubleValue());
						}
						s.append(";");
					}
				}
			}
		}
		System.out.println(s.toString());

	}

}
