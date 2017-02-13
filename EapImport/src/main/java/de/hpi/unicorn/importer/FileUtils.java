/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * centralize file manipulation
 */
public class FileUtils {

	/**
	 * returns file name "/tmp/abc.txt" returns "abc"
	 */
	public static String getFileNameWithoutExtension(final String filePath) {
		final File file = new File(filePath);
		String fileName = file.getName();
		fileName = fileName.replace(" ", "_");
		final int index = fileName.lastIndexOf('.');
		return fileName.substring(0, index);
	}

	/**
	 * reads file and concatenate content to a string
	 */
	public static String getFileContentAsString(final String filePath) throws IOException {
		final StringBuffer fileData = new StringBuffer(1000);
		final BufferedReader reader = new BufferedReader(new FileReader(filePath));
		char[] buf = new char[1024];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			final String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
		reader.close();
		return fileData.toString();
	}

	/**
	 * save StreamResult to file given in path
	 */
	public static void writeResultToFile(final StreamResult result, final String path) {
		final String fileString = result.getWriter().toString();

		try {
			final BufferedWriter out = new BufferedWriter(new FileWriter(path));
			out.write(fileString);
			out.close();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * returns Document from StreamResult
	 */
	public static Document createDocumentFromResult(final StreamResult result) {
		Document document = null;
		final String fileString = result.getWriter().toString();
		try {
			final DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			final StringReader reader = new StringReader(fileString);
			final InputSource inputSource = new InputSource(reader);
			document = parser.parse(inputSource);
		} catch (final SAXException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		return document;
	}

}
