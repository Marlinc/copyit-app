package net.mms_projects.copy_it.resource_loading;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class AndroidResourceLoader implements ResourceLoaderInterface {

	private static Map<String, String> strings;

	@Override
	public String getString(String name) {
		if (strings == null) {
			loadStrings();
		}
		if (isReference(name)) {
			if (getReferenceType(name).equals("string")) {
				return getString(resolveReference(name));
			}
			return "Invalid reference type " + getReferenceType(name)
					+ " for reference " + name;
		}
		String text = strings.get(name);
		if (text == null) {
			return null;
		}
		if (isReference(text)) {
			if (getReferenceType(text).equals("string")) {
				return getString(resolveReference(text));
			}
			return "Invalid reference type " + getReferenceType(text)
					+ " for reference " + name;
		}
		return text;
	}

	@Override
	public String getString(String name, Object... formatArgs) {
		String raw = getString(name);
		return String.format(raw, formatArgs);
	}

	protected void loadStrings() {
		strings = new HashMap<String, String>();

		InputStream stream = AndroidResourceLoader.class
				.getResourceAsStream(getResourceName("values/strings.xml"));
		InputSource inputSource = new InputSource(stream);

		try {
			parseStrings(inputSource);
		} catch (XPathExpressionException exception) {
			// TODO Auto-generated catch block
			exception.printStackTrace();
		}
	}

	protected void parseStrings(InputSource source)
			throws XPathExpressionException {
		XPath xpath = XPathFactory.newInstance().newXPath();

		Node root = (Node) xpath.evaluate("/", source, XPathConstants.NODE);

		NodeList xmlStrings = null;

		xmlStrings = (NodeList) xpath.evaluate("//string", root,
				XPathConstants.NODESET);
		for (int i = 0; i < xmlStrings.getLength(); ++i) {
			String key = (String) xpath.evaluate("//string[" + (i + 1)
					+ "]/@name", root, XPathConstants.STRING);
			String value = xmlStrings.item(i).getTextContent();

			strings.put(key, value);
		}
	}

	public String getResourceName(String name) {
		String basePath = "/";
		return basePath + name;
	}

	public boolean isReference(String reference) {
		return reference.startsWith("@");
	}

	public String getReferenceType(String reference) {
		String[] data = reference.split("/");
		return data[0].substring(1);
	}

	public String resolveReference(String reference) {
		String[] data = reference.split("/");
		return data[1];
	}

}
