/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.namespace.QName;
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
import javax.xml.xpath.XPathConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import de.hpi.unicorn.event.EapEvent;
import de.hpi.unicorn.event.collection.EventTransformationElement;
import de.hpi.unicorn.event.collection.TransformationTree;
import de.hpi.unicorn.process.CorrelationProcessInstance;

/**
 * Utils for XML Document manipulation and generation. Transforms EapEvents in
 * XML representation.
 */
public class XMLUtils {

	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	public enum XPathConstantsMapping {

		STRING(XPathConstants.STRING), DATE(DatatypeConstants.DATETIME), INTEGER(XPathConstants.NUMBER);

		private final QName qName;

		XPathConstantsMapping(final QName type) {
			this.qName = type;
		}

		public QName getQName() {
			return this.qName;
		}
	}

	public static Document stringToDoc(final String xml) {
		InputStream xsdInputStream;
		try {
			xsdInputStream = new ByteArrayInputStream(xml.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e1) {
			e1.printStackTrace();
			return null;
		}
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		// need document from xml for the xml parser
		Document doc = null;
		// System.out.println("received EventType: \n" + xml);
		try {
			doc = builder.parse(xsdInputStream);
		} catch (final SAXException e) {
			e.printStackTrace();
			return null;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}

		return doc;
	}

	/**
	 * transforms EapEvent into typed XML-Node.
	 * 
	 * @return XML Representation of the given EapEvent
	 */
	public static Node eventToNode(final EapEvent event) {
		final TransformationTree<String, Serializable> values = event.getValueTree();
		final DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
		domFactory.setNamespaceAware(true);
		DocumentBuilder builder = null;
		try {
			builder = domFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		// need document from XML for the XML parser
		final Document doc = builder.newDocument();
		final Element root = doc.createElement(event.getEventType().getTypeName());
		doc.appendChild(root);
		final Element time = doc.createElement(event.getEventType().getTimestampName());
		time.setTextContent(XMLUtils.getFormattedDate(event.getTimestamp()));
		root.appendChild(time);

		final List<Integer> processInstanceIDs = new ArrayList<Integer>();
		for (final CorrelationProcessInstance processInstance : event.getProcessInstances()) {
			processInstanceIDs.add(processInstance.getID());
		}
		final Element processInst = doc.createElement("ProcessInstances");
		processInst.setTextContent(processInstanceIDs.toString());
		root.appendChild(processInst);

		for (final EventTransformationElement<String, Serializable> element : values.getTreeRootElements()) {
			final Node importedNode = doc.importNode(element.getNodeWithChildnodes().getFirstChild(), true);
			root.appendChild(importedNode);
		}
		return doc;
	}

	public static String getFormattedDate(final Date date) {
		return XMLUtils.sdf.format(date);
	}

	/**
	 * Converts Date into XML-Tag with type xsd:dateTime
	 */
	public static String getXMLDate(final Date date) {
		final Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return DatatypeConverter.printDateTime(calendar);
	}

	/**
	 * Converts Integer into XML-Tag with type xsd:int
	 */
	public static String getXMLInteger(final Integer integer) {
		return DatatypeConverter.printInt(integer);
	}

	/**
	 * Converts Long into XML-Tag with type xsd:long
	 */
	public static String getXMLLong(final long value) {
		return DatatypeConverter.printLong(value);
	}

	/**
	 * Converts Double into XML-Tag with type xsd:float
	 */
	public static String getXMLDouble(final Double value) {
		return DatatypeConverter.printDouble(value);
	}

	public static void printDocument(final Document doc) {
		try {
			final TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer;
			transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
			transformer.setOutputProperty(OutputKeys.METHOD, "xml");
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
			transformer.transform(new DOMSource(doc), new StreamResult(new OutputStreamWriter(System.out, "UTF-8")));
		} catch (final TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (final UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (final TransformerException e) {
			e.printStackTrace();
		}
	}

}