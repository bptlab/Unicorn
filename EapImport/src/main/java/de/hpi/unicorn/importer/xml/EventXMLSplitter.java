/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang.StringUtils;

import de.hpi.unicorn.importer.FileUtils;

public class EventXMLSplitter {

	private static final String DEFAULT_XSL_SPLITTER_FILE_PATH = "src/main/resources/splitXML.xsl";
	private static final String DEFAULT_XSL_MAX_DEPTH_FILE_PATH = "src/main/resources/getMaxDepth.xsl";
	private static final String DEFAULT_XSL_DUPLICATED_SIBLINGS_FILE_PATH = "src/main/resources/sameNameSiblings.xsl";

	public static List<String> simpleTransform(final String xmlString, final boolean isFilePath) {
		if (isFilePath) {
			try {
				return EventXMLSplitter.simpleTransform(FileUtils.getFileContentAsString(xmlString),
						EventXMLSplitter.DEFAULT_XSL_SPLITTER_FILE_PATH);
			} catch (final IOException e) {
				e.printStackTrace();
				return null;
			}
		} else {
			return EventXMLSplitter.simpleTransform(xmlString, EventXMLSplitter.DEFAULT_XSL_SPLITTER_FILE_PATH);
		}
	}

	private static List<String> simpleTransform(final String xmlString, final String xslFilePath) {
		final int levels = EventXMLSplitter.getMaxDepthOf(xmlString);
		List<String> resultingXMLEvents = new ArrayList<>();
		resultingXMLEvents.add(xmlString);

		// level 1 equals root node, must not be duplicated, so start at level 2
		for (int i = 2; i <= levels; i++) {
			final List<String> currentLevelXMLEvents = new ArrayList<>();
			for (final String xmlEvent : resultingXMLEvents) {
				final String[] multiplyDefinedAttributes = EventXMLSplitter.identifyMultiplyDefinedAttributes(xmlEvent,
						i);
				List<String> currentXMLEvents = new ArrayList<>();
				currentXMLEvents.add(xmlEvent);
				for (final String attributeTag : multiplyDefinedAttributes) {
					final List<String> newXMLEvents = new ArrayList<>();
					for (final String currentXMLEvent : currentXMLEvents) {
						final InputStream xsltFile = EventXMLSplitter
								.adjustXSLToAttributeTag(xslFilePath, attributeTag);
						final String transformationResult = EventXMLSplitter.performTransformation(xsltFile,
								currentXMLEvent);
						newXMLEvents.addAll(EventXMLSplitter.processTransformationResults(transformationResult));
					}
					currentXMLEvents = new ArrayList<>(newXMLEvents);
				}
				currentLevelXMLEvents.addAll(currentXMLEvents);
			}
			resultingXMLEvents = new ArrayList<>(currentLevelXMLEvents);
		}

		return resultingXMLEvents;
	}

	private static String performTransformation(final InputStream xsltFile, final String xmlString) {
		String transformationResult = null;
		try {
			System.setProperty("javax.xml.transform.TransformerFactory", "net.sf.saxon.TransformerFactoryImpl");
			final TransformerFactory tFactory = TransformerFactory.newInstance();
			final StreamSource source = new StreamSource(new StringReader(xmlString));
			final Transformer transformer = tFactory.newTransformer(new StreamSource(xsltFile));
			final Writer outWriter = new StringWriter();
			final StreamResult result = new StreamResult(outWriter);
			transformer.transform(source, result);
			transformationResult = outWriter.toString();
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return transformationResult;
	}

	private static int getMaxDepthOf(final String xmlString) {
		String depth = "0";
		try {
			depth = EventXMLSplitter.performTransformation(new FileInputStream(
					EventXMLSplitter.DEFAULT_XSL_MAX_DEPTH_FILE_PATH), xmlString);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
		}
		return Integer.valueOf(depth) - 1;
	}

	private static InputStream adjustXSLToAttributeTag(final String xsltPath, final String attributeTagString) {
		String xslFileContent = null;
		try {
			xslFileContent = FileUtils.getFileContentAsString(xsltPath);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		xslFileContent = xslFileContent.replaceAll("%toSplitBy", attributeTagString);
		try {
			return new ByteArrayInputStream(xslFileContent.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static String[] identifyMultiplyDefinedAttributes(final String xmlString, final int level) {
		final String attributesString = EventXMLSplitter.performTransformation(
				EventXMLSplitter.adjustSiblingFinderXSLToLevel(level), xmlString);
		final String[] attributes = attributesString.equals("") ? new String[0] : attributesString.split(",");
		return attributes;
	}

	private static InputStream adjustSiblingFinderXSLToLevel(final int level) {
		final String levelIndicator = "/*";
		final String parentChoser = "<xsl:value-of select='name(ancestor::*[%d])' />/";
		String xslFileContent = null;
		try {
			xslFileContent = FileUtils
					.getFileContentAsString(EventXMLSplitter.DEFAULT_XSL_DUPLICATED_SIBLINGS_FILE_PATH);
		} catch (final IOException e) {
			e.printStackTrace();
		}
		xslFileContent = xslFileContent.replaceAll("%level", StringUtils.repeat(levelIndicator, level - 1));
		String parentNodes = "";
		for (int i = level; i > 2; i--) {
			parentNodes += String.format(parentChoser, i - 2);
		}
		xslFileContent = xslFileContent.replaceAll("%parents", parentNodes);
		try {
			return new ByteArrayInputStream(xslFileContent.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static List<String> processTransformationResults(final String transformResult) {
		final String newXmlString = transformResult.replaceAll("<Event", "~<Event");
		final String[] xmlStrings = newXmlString.split("~");
		final List<String> resultingXMLEvents = new ArrayList<>();
		for (int i = 1; i < xmlStrings.length; i++) {
			resultingXMLEvents.add(xmlStrings[0] + xmlStrings[i]);
		}
		return resultingXMLEvents;
	}

}
