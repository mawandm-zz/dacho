package org.kakooge.dacho.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/**
 * An XML Utility class
 * @author mawandm
 *
 */
public final class XMLUtil {
	
	/**
	 * Generate a xml document from the specified file
	 * @param xmlFile
	 * @return
	 * @throws IOException
	 */
	public static Document getXmlDocument(final File xmlFile) throws IOException{
		final StringBuilder xmlContent = new StringBuilder();
		final BufferedReader reader = new BufferedReader(new FileReader(xmlFile));
		String line = null;
		while((line = reader.readLine())!=null)
			xmlContent.append(line);
		return getXmlDocument(xmlContent.toString());
	}
	
	/**
	 * Get a XML document from the specified content
	 * @param contentHtml the content
	 * @return the generated document
	 */
	public static Document getXmlDocument(String contentHtml) {
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
			return docBuilder.parse(new InputSource(new java.io.StringReader(
					contentHtml)));
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Sets the XML contents of this node to the given value
	 * @param node the node
	 * @param xml the new xml representation of this node
	 */
	public static void setOuterXML(Node node, String xml) {
		Document document = getXmlDocument(xml);
		Document dest_document = node.getOwnerDocument();
		Node dest_node = dest_document.importNode(document.getFirstChild(),
				true);
		Node pnode = node.getParentNode();
		pnode.replaceChild(dest_node, node);
	}

	/**
	 * Get the OuterXML of the specified Node
	 * @param node the node
	 * @param omitXmlDeclaration indicates if we should eliminate the <code>&lt;? xml ... ?&gt; </code>declaration
	 * @return the outer xmls
	 * @throws TransformerException
	 */
	public static String getOuterXML(Node node, String omitXmlDeclaration) throws TransformerException {

		TransformerFactory transformerFactory = TransformerFactory
				.newInstance();
		Transformer transformer = transformerFactory.newTransformer();
		transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, omitXmlDeclaration);
		StringWriter writer = new StringWriter();
		transformer.transform(new DOMSource(node), new StreamResult(writer));
		return writer.toString();
	}

	/**
	 * Evaluates the specified xpath expression
	 * @param doc the document on which the expressions is to be evaluated
	 * @param expression the xpath expression
	 * @param qname the QName
	 * @return returns the Node or Nodelist
	 * @throws XPathExpressionException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T evaluateXPath(Document doc, String expression, QName qname)
			throws XPathExpressionException {
		XPathFactory xPathfactory = XPathFactory.newInstance();
		XPath xpath = xPathfactory.newXPath();
		XPathExpression expr = xpath.compile(expression);
		return (T) expr.evaluate(doc, qname);
	}

	private XMLUtil() {}
}