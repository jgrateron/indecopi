package com.fresco;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ParseXml {
    private Document doc;

    public ParseXml(InputStream inXml) throws ParserConfigurationException, SAXException, IOException {
        var dbFactory = DocumentBuilderFactory.newInstance();
        var dBuilder = dbFactory.newDocumentBuilder();
        doc = dBuilder.parse(inXml);
        doc.getDocumentElement().normalize();
    }

    public List<Node> listNode(String nodo) throws XPathExpressionException {
        var nodeList = nodeListValue(nodo);
        if (nodeList != null) {
            int length = nodeList.getLength();
            if (length > 0) {
                var list = new ArrayList<Node>();
                for (var i = 0; i < length; i++) {
                    list.add(nodeList.item(i));
                }
                return list;
            }
        }
        return Collections.emptyList();
    }

    private NodeList nodeListValue(String nodo) throws XPathExpressionException {
        var xPath = XPathFactory.newInstance().newXPath();
        var expr = xPath.compile(nodo);
        var result = expr.evaluate(doc, XPathConstants.NODESET);
        var nodesa = (NodeList) result;
        if (nodesa != null)
            if (nodesa.getLength() > 0) {
                return nodesa;
            }
        return null;
    }

    public String nodeValue(Node nodo) {
        if (nodo != null) {
            for (Node child = nodo.getFirstChild(); child != null; child = child.getNextSibling()) {
                if (child.getNodeType() == Node.TEXT_NODE) {
                    return child.getNodeValue();
                }
            }
        }
        return null;
    }
}
