/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.importer.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.collection.EventTransformationElement;
import de.hpi.unicorn.utils.TempFolderUtil;

public class XMLExporter {

	public File generateExportFile(final EapEvent event) {
		final Document doc = this.convertToXML(event);
		// //System.out.println(doc);
		final TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = null;
		try {
			transformer = transformerFactory.newTransformer();
		} catch (final TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		final DOMSource source = new DOMSource(doc);
		final File file = new File(TempFolderUtil.getFolder() + System.getProperty("file.separator")
				+ event.getEventType().getTypeName() + "export.xml");
		final StreamResult result = new StreamResult(file);
		try {
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			transformer.transform(source, result);
		} catch (final TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}

	public Document convertToXML(final EapEvent event) {
		final String eventTypeName = event.getEventType().getTypeName();
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		final Document doc = builder.newDocument();
		final Element element = doc.createElement(eventTypeName);
		element.setAttribute("xmlns", "");
		element.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		element.setAttribute("xsi:noNamespaceSchemaLocation", eventTypeName + ".xsd");
		final Node timestampNode = doc.importNode(event.getTimestampAsNode().getFirstChild(), true);
		element.appendChild(timestampNode);
		final List<EventTransformationElement<String, Serializable>> treeRootElements = event.getValueTree()
				.getTreeRootElements();
		for (final EventTransformationElement<String, Serializable> rootElement : treeRootElements) {
			final Node rootNode = doc.importNode(rootElement.getNodeWithChildnodes().getFirstChild(), true);
			element.appendChild(rootNode);
		}
		doc.appendChild(element);
		return doc;

	}

	public File generateZipWithXMLFiles(final List<EapEvent> events) {
		final String eventType = events.get(0).getEventType().getTypeName();
		final File file = new File(TempFolderUtil.getFolder() + System.getProperty("file.separator") + eventType
				+ "XMLexport.zip");
		try {
			final ZipOutputStream zip = new ZipOutputStream(new FileOutputStream(file));
			for (int i = 0; i < events.size(); i++) {
				// add leading zeros to keep events sorted in the right order
				final ZipEntry ze = new ZipEntry(eventType
						+ String.format("%0" + (Math.log10(events.size()) + 1) + "d", events.get(i).getID()) + ".xml");
				// events.get(i).getValues().get("index")) + ".xml");
				// final ZipEntry ze = new ZipEntry(eventType
				// + String.format("%0" + (Math.log10(events.size()) + 1) + "d",
				// i) + ".xml");
				zip.putNextEntry(ze);
				final File xmlFile = this.generateExportFile(events.get(i));
				final Path path = Paths.get(xmlFile.getPath());
				final byte[] data = Files.readAllBytes(path);
				zip.write(data);
				zip.closeEntry();
			}
			zip.close();
		} catch (final IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return file;
	}
}
