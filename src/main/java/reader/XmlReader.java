package reader;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.CaseFormat;
import com.tsb.tsdib.ws.bean.eai.common.EaiHeaderBean;
import com.tsb.tsdib.ws.bean.eai.common.HostHeaderBean;

import enums.TagEnum;
import model.DocRowModel;
import model.ElementObject;

public class XmlReader {

	private List<ElementObject> columns = new ArrayList<ElementObject>();

	private EaiHeaderBean eaiHeader = null;
	private HostHeaderBean hostHeader = null;
	private HostHeaderBean hostHeaderBean = null;

	public HostHeaderBean getHostHeader() {
		return hostHeader;
	}

	public EaiHeaderBean getEaiHeader() {
		return eaiHeader;
	}

	public HostHeaderBean getHostHeaderBean() {
		return hostHeaderBean;
	}

	public void getData(String file, TagEnum tag) throws Exception {

		Document doc = getDocumentFromFile(file);

		DocReader csv = new DocReader();

		csv.readFromCVS(file.replace(".xml", ".csv"));

		csv.showData();

		Map<String, DocRowModel> csvData = csv.getDb();

		NodeList nList = doc.getElementsByTagName(tag.getTagName());

		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				NodeList childList = eElement.getChildNodes();

				System.out.println("node name:" + nNode.getNodeName());

				for (int childTemp = 0; childTemp < childList.getLength(); childTemp++) {
					Node childNode = childList.item(childTemp);

					if (isSaveTag(childNode, tag)) {
						Element eChildNode = (Element) childNode;
						columns.add(parseElementObject(eChildNode, getDocByNode(tag, eChildNode, csvData)));
					}
				}
			}
		}
	}

	private DocRowModel getDocByNode(TagEnum tag, Element element, Map<String, DocRowModel> csvData) {
		return csvData.get(element.getTagName());
	}

	private boolean isSaveTag(Node node, TagEnum tag) {

		if (node.getNodeType() != Node.ELEMENT_NODE) {
			return false;
		}
		switch (tag) {
		case REQUEST:
			return node.getNodeName().startsWith(REQUEST_TAG_PREFIX);
		case RESPONSE_SINGLE:
			return node.getNodeName().startsWith(RESPONSE_TAG_PREFIX) && !"OUT-RECORD".equals(node.getNodeName());
		case RESPONSE_MULTIPLE_RECORD:
			return node.getNodeName().startsWith(RESPONSE_TAG_PREFIX);
		}
		return false;
	}

	private ElementObject parseElementObject(Element element, DocRowModel doc) throws Exception {

		if (doc == null) {
			throw new Exception("doc is not found.");
		}

		ElementObject object = new ElementObject();

		object.setBeanName(convertToProperty(element.getTagName()));
		object.setCname(doc.getCname());
		object.setColumnAttribute(doc.getColumnAttribute());
		object.setDefaultValue(doc.getDefaultValue());
		object.setDesc(doc.getDesc());
		object.setInParam(doc.isInParam());
		object.setParamName(StringUtils.isBlank(doc.getParamName()) ? object.getBeanName() : doc.getParamName());
		object.setTagName(element.getTagName());
		object.setTestValue(element.getTextContent());

		return object;
	}

	private String convertToProperty(String tagName) {
		String name = "";
		if (tagName.startsWith(REQUEST_TAG_PREFIX)) {
			name = tagName.replaceFirst(REQUEST_TAG_PREFIX, "");
		} else if (tagName.startsWith(RESPONSE_TAG_PREFIX)) {
			name = tagName.replaceFirst(RESPONSE_TAG_PREFIX, "");
		}

		return CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, name);
	}

	private static Document getDocumentFromFile(String file)
			throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(file);

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);

		doc.getDocumentElement().normalize();
		return doc;
	}

	public List<ElementObject> getColumns() {
		return columns;
	}

	private static final String REQUEST_TAG_PREFIX = "IN-";
	private static final String RESPONSE_TAG_PREFIX = "OUT-";

	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		XmlReader xmlReader = new XmlReader();

		xmlReader.parseHeader("xml/WDGF_DIGACMSQ_Req.xml");

		// xmlReader.getData("xml/WDGF_DIGACMSQ_Req.xml", "Request_Data");

		// for (ParameterObject column : xmlReader.columns) {
		// System.out.println(column);
	}

	public void parseHeader(String file) throws ParserConfigurationException, SAXException, IOException,
			IllegalAccessException, InvocationTargetException, NoSuchMethodException {

		Document doc = getDocumentFromFile(file);

		eaiHeader = new EaiHeaderBean();
		NodeList nList = doc.getElementsByTagName("EAI_Header");
		for (int temp = 0; temp < nList.getLength(); temp++) {
			Node nNode = nList.item(temp);
			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				NodeList childList = eElement.getChildNodes();
				System.out.println("node name =" + nNode.getNodeName());

				for (int childTemp = 0; childTemp < childList.getLength(); childTemp++) {
					Node childNode = childList.item(childTemp);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String preoperty = convertHeaderToProperty(childNode.getNodeName());
						PropertyUtils.setSimpleProperty(eaiHeader, preoperty, childNode.getTextContent());
					}
				}
			}
		}

		NodeList hostList = doc.getElementsByTagName("Host_Header");

		hostHeader = new HostHeaderBean();
		for (int temp = 0; temp < hostList.getLength(); temp++) {
			Node nNode = hostList.item(temp);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {
				Element eElement = (Element) nNode;
				System.out.println("node name" + nNode.getNodeName());
				NodeList childList = eElement.getChildNodes();
				for (int childTemp = 0; childTemp < childList.getLength(); childTemp++) {
					Node childNode = childList.item(childTemp);
					if (childNode.getNodeType() == Node.ELEMENT_NODE) {
						String property = convertHeaderToProperty(childNode.getNodeName().toLowerCase());
						try {
							PropertyUtils.setSimpleProperty(hostHeader, property, childNode.getTextContent());
						} catch (Exception e) {
							System.out.println(childNode.getNodeName() + ":" + property
									+ "is not found the setter in HostHeaderBean");
						}
					}
				}
			}
		}
	}

	private String convertHeaderToProperty(String tagName) {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, tagName.replace("EAI", "eai"));
	}
}
