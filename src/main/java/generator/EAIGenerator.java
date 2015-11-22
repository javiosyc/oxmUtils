package generator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang3.StringUtils;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaDocSource;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.CaseFormat;
import com.tsb.tsdib.ws.bean.ActualBean;
import com.tsb.tsdib.ws.bean.eai.common.EaiBean;
import com.tsb.tsdib.ws.bean.eai.common.EaiHeaderBean;
import com.tsb.tsdib.ws.bean.eai.common.HostHeaderBean;
import com.tsb.tsdib.ws.bean.eai.common.ParamBean;

import enums.TagEnum;
import model.ElementObject;
import reader.XmlReader;

public class EAIGenerator {

	private static HostHeaderBean hostHeaderBeanFromXML;
	private static EaiHeaderBean eaiHeaderBeanFromXML;
	private String EAI_HEADER_BIND_NAME = "EAI_Header";
	private String EAI_HEADER_FIELD_NAME = "eaiHeaderBean";

	private String HOST_HEADER_BIND_NAME = "Host_Header";
	private String HOST_HEADER_FIELD_NAME = "hostHeaderBean";

	private String REQUEST_DATA_BIND_NAME = "Request_Data";
	private String REQUEST_DATA_FIELD_NAME = "requestDataBean";

	private String RESPONSE_DATA_BIND_NAME = "Response_Data";
	private String RESPONSE_DATA_FIELD_NAME = "responseDataBean";

	private String ROOT_TAG = null;

	private String service_name;

	private String default_package = null;
	private String default_service_package = null;

	private String headerBean = null;

	private String hostHeaderBean = null;

	private String outRecordTag = null;

	private String outRecordBeansName = null;

	private List<ElementObject> requsetDataColumns = null;

	private List<ElementObject> responseDataColumns = null;

	private List<ElementObject> outRecordDataColumns = null;

	private String package_name = null;

	private String requestDataBean = null;

	private String responseDataBean = null;
	private String outRecordDataBean = null;

	private List<String> outResponseDataBeanAttributes = new ArrayList<String>();
	private String version;
	private String author;
	private String company;
	private String copyright;

	public void parseXML() throws Exception {
		parseXMLFromRequestFile();
		parseXMLFromResponsetFile();
	}

	public void createCode() throws Exception {
		File xmldir = new File("output/" + service_name.toLowerCase() + "/xml");
		xmldir.mkdirs();

		createMappingXML();
		createEAIJavaCodes();
	}

	private void createEAIJavaCodes() throws IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException {

		JavaClassSource requestBean = generateJavaBean("Request" + service_name + "Bean", requsetDataColumns,
				"getBeanName");

		JavaClassSource responseBean = generateJavaBean("Response" + service_name + "Bean", responseDataColumns,
				"getBeanName");

		if (outResponseDataBeanAttributes.size() > 0)
			generateJavaBean("OutRecord" + service_name + "Bean", outRecordDataColumns, "getBeanName");

		JavaClassSource actualBean = generateActualBean(ActualBean.class, responseBean, responseDataColumns,
				"getParamName");

		JavaClassSource paramBean = generateParamBean(ParamBean.class, requestBean, requsetDataColumns, "getParamName");

		JavaClassSource eaiRequestBean = generateEAIBean("EaiRequest" + service_name + "Bean", EaiBean.class,
				requestBean, "requestDataBean");
		JavaClassSource eaiResponseBean = generateEAIBean("EaiResponse" + service_name + "Bean", EaiBean.class,
				responseBean, "responseDataBean");

		generateService(eaiRequestBean, eaiResponseBean, requestBean, responseBean, paramBean, actualBean);

	}

	private JavaClassSource generateService(JavaClassSource eaiRequestBean, JavaClassSource eaiResponseBean,
			JavaClassSource requestBean, JavaClassSource responseBean, JavaClassSource paramBean,
			JavaClassSource actualBean) {

		JavaClassSource javaClass = Roaster.parse(JavaClassSource.class,
				"public class Eai" + service_name + "Service extends BaseEaiService< " + eaiRequestBean.getName() + ", "
						+ eaiResponseBean.getName() + "," + paramBean.getName() + "," + actualBean.getName() + " >{}");

		javaClass.setPackage(default_service_package);

		addImportClass(javaClass, eaiRequestBean, eaiResponseBean, requestBean, responseBean, paramBean, actualBean);

		addStaticFields(javaClass);

		implementGetEaiCodeMethod(javaClass);
		implementSendMethod(javaClass, responseBean, paramBean, actualBean);

		implementExcludeErrorCodeMethod(javaClass);
		implementGetSuccessHostReturnCode(javaClass);

		implementGetEaiRequestBeanMethod(javaClass, eaiRequestBean, requestBean, paramBean);

		implementGetEaiResponseBeanMethod(javaClass, eaiResponseBean);

		BufferedWriter output = null;
		try {
			File dir = new File("output/" + service_name.toLowerCase() + "/service/");
			dir.mkdirs();
			File file = new File(
					"output/" + service_name.toLowerCase() + "/service/Eai" + service_name + "Service.java");
			output = new BufferedWriter(new FileWriter(file));
			output.write(javaClass.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return javaClass;
	}

	private JavaClassSource generateParamBean(Class<ParamBean> extendSuperType, JavaClassSource requestBean,
			List<ElementObject> elementObjects, String methodName) throws IllegalAccessException,
					IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);

		javaClass.setPackage(package_name).setName("Eai" + service_name + "ParamBean").extendSuperType(extendSuperType);

		for (ElementObject elementObject : elementObjects) {

			if (elementObject.isInParam()) {
				String propertyName = (String) ElementObject.class.getMethod(methodName).invoke(elementObject);
				javaClass.addProperty("String", propertyName);

				javaClass.getProperty(propertyName).getField().getJavaDoc().setFullText(elementObject.getCname());
			}
		}

		BufferedWriter output = null;
		try {
			File file = new File(
					"output/" + service_name.toLowerCase() + "/" + "Eai" + service_name + "ParamBean" + ".java");
			output = new BufferedWriter(new FileWriter(file));
			output.write(javaClass.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return javaClass;
	}

	private void createMappingXML() throws ParserConfigurationException, TransformerException {

		Document doc = getNewDocument();

		Element root = createRootElement(doc);

		buildRequestElement(doc, root);

		buildResponseElement(doc, root);

		generateXMLFile(doc);
	}

	private void generateXMLFile(Document doc) throws TransformerException {
		TransformerFactory transformerFactory = TransformerFactory.newInstance();
		Transformer transformer = transformerFactory.newTransformer();

		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(new File(
				"output/" + service_name.toLowerCase() + "/xml/mapping-" + service_name.toLowerCase() + ".xml"));

		transformer.transform(source, result);
	}

	private void buildResponseElement(Document doc, Element root) {
		root.appendChild(doc.createTextNode("\n"));
		Comment comment = doc
				.createComment(" ================================= Response ================================== ");
		root.appendChild(comment);

		root.appendChild(createEaiResponseElement(doc));

		root.appendChild(createResponseBeanElement(doc));

		if (outRecordDataColumns.size() > 0)
			root.appendChild(createOutRecordBeanElement(doc));
	}

	private Element buildRequestElement(Document doc, Element root) {
		root.appendChild(doc.createTextNode("\n"));

		Comment comment = doc
				.createComment(" ================================= Request ================================== ");
		root.appendChild(comment);

		root.appendChild(createEaiRequestElement(doc));
		root.appendChild(createRequestBeanElement(doc));

		return root;
	}

	private Document getNewDocument() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		return docBuilder.newDocument();
	}

	private Element createRootElement(Document doc) {
		Element root = doc.createElement(ROOT_TAG);
		doc.appendChild(root);

		return root;
	}

	public void init(String service_name) throws Exception {
		this.service_name = service_name;

		package_name = default_package + "." + this.service_name.toLowerCase();

		requestDataBean = package_name + ".Request" + this.service_name + "Bean";

		responseDataBean = package_name + ".Response" + this.service_name + "Bean";

		outRecordDataBean = package_name + ".OutRecord" + this.service_name + "Bean";

	}

	private static List<ElementObject> getColumnsFormXML(String file, TagEnum tag) throws Exception {

		XmlReader reader = new XmlReader();

		reader.getData(file, tag);

		reader.parseHeader(file);

		eaiHeaderBeanFromXML = reader.getEaiHeader();

		hostHeaderBeanFromXML = reader.getHostHeader();

		return reader.getColumns();
	}

	private JavaClassSource generateActualBean(Class<ActualBean> extendSuperType, JavaClassSource requestBean,
			List<ElementObject> elementObjects, String methodName) throws IllegalAccessException,
					IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(package_name).setName("Actual" + service_name + "Bean").extendSuperType(extendSuperType);

		for (ElementObject elementObject : elementObjects) {

			if (elementObject.isInParam()) {
				String propertyName = (String) ElementObject.class.getMethod(methodName).invoke(elementObject);
				javaClass.addProperty("String", propertyName);

				javaClass.getProperty(propertyName).getField().getJavaDoc().setFullText(elementObject.getCname());
			}
		}

		BufferedWriter output = null;
		try {
			File file = new File("output/" + service_name.toLowerCase() + "/Actual" + service_name + "Bean" + ".java");
			output = new BufferedWriter(new FileWriter(file));
			output.write(javaClass.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return javaClass;
	}

	private Element createOutRecordBeanElement(Document doc) {

		Element outRecordBean = doc.createElement("class");
		outRecordBean.setAttribute("name", outRecordDataBean);

		for (ElementObject elementObject : outRecordDataColumns) {
			outRecordBean.appendChild(
					createBindingField(doc, elementObject.getBeanName(), "string", elementObject.getTagName()));
		}
		return outRecordBean;
	}

	private Element createResponseBeanElement(Document doc) {

		Element responseBean = doc.createElement("class");
		responseBean.setAttribute("name", responseDataBean);

		if (outRecordDataColumns.size() > 0) {
			responseBean.appendChild(createOutResponseData(doc));
		}

		for (ElementObject elementObject : responseDataColumns) {
			responseBean.appendChild(
					createBindingField(doc, elementObject.getBeanName(), "string", elementObject.getTagName()));
		}

		return responseBean;
	}

	private Element createOutResponseData(Document doc) {
		Element field = doc.createElement("field");
		field.setAttribute("name", outRecordBeansName);
		field.setAttribute("collection", "arraylist");

		field.setAttribute("type", outRecordDataBean);

		Element bind_XML = doc.createElement("bind-xml");
		bind_XML.setAttribute("name", outRecordTag);
		bind_XML.setAttribute("node", "element");

		field.appendChild(bind_XML);
		return field;
	}

	private Element createEaiResponseElement(Document doc) {
		Element eauResponse = doc.createElement("class");
		eauResponse.setAttribute("name", package_name + ".EaiResponse" + service_name + "Bean");
		eauResponse.appendChild(createMapToElement(doc));
		eauResponse.appendChild(createBindingField(doc, EAI_HEADER_FIELD_NAME, headerBean, EAI_HEADER_BIND_NAME));
		eauResponse.appendChild(createBindingField(doc, HOST_HEADER_FIELD_NAME, hostHeaderBean, HOST_HEADER_BIND_NAME));
		eauResponse.appendChild(
				createBindingField(doc, RESPONSE_DATA_FIELD_NAME, responseDataBean, RESPONSE_DATA_BIND_NAME));
		return eauResponse;
	}

	private JavaClassSource generateJavaBean(String name, List<ElementObject> elementObjects, String methodName)
			throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException,
			SecurityException {
		final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(package_name).setName(name);

		for (ElementObject elementObject : elementObjects) {
			javaClass.addProperty("String", (String) ElementObject.class.getMethod(methodName).invoke(elementObject));
		}

		addJavaClassDefaultComment(javaClass);

		BufferedWriter output = null;
		try {
			File file = new File("output/" + service_name.toLowerCase() + "/" + name + ".java");
			output = new BufferedWriter(new FileWriter(file));
			output.write(Roaster.format(javaClass.toString()));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return javaClass;
	}

	private void addJavaClassDefaultComment(JavaClassSource javaClass) {
		JavaDocSource<?> javaDoc = javaClass.getJavaDoc();

		javaDoc.addTagValue("@version", version);
		javaDoc.addTagValue("@author", author);
		javaDoc.addTagValue("@company", company);
		javaDoc.addTagValue("@copyright", copyright);
	}

	private Element createRequestBeanElement(Document doc) {
		Element requestBean = doc.createElement("class");
		requestBean.setAttribute("name", requestDataBean);

		for (ElementObject elementObject : requsetDataColumns) {
			requestBean.appendChild(
					createBindingField(doc, elementObject.getBeanName(), "string", elementObject.getTagName()));
		}

		return requestBean;
	}

	private Element createEaiRequestElement(Document doc) {
		Element eaiRequestBean = doc.createElement("class");
		eaiRequestBean.setAttribute("name", package_name + ".EaiRequest" + service_name + "Bean");

		eaiRequestBean.appendChild(createMapToElement(doc));
		eaiRequestBean.appendChild(createBindingField(doc, EAI_HEADER_FIELD_NAME, headerBean, EAI_HEADER_BIND_NAME));
		eaiRequestBean
				.appendChild(createBindingField(doc, HOST_HEADER_FIELD_NAME, hostHeaderBean, HOST_HEADER_BIND_NAME));
		eaiRequestBean
				.appendChild(createBindingField(doc, REQUEST_DATA_FIELD_NAME, requestDataBean, REQUEST_DATA_BIND_NAME));

		return eaiRequestBean;
	}

	private static Element createBindingField(Document doc, String name, String type, String bindXMLName) {
		Element field = doc.createElement("field");
		field.setAttribute("name", name);
		field.setAttribute("type", type);

		Element bind_XML = doc.createElement("bind-xml");

		bind_XML.setAttribute("name", bindXMLName);
		bind_XML.setAttribute("node", "element");
		field.appendChild(bind_XML);

		return field;
	}

	private static Element createMapToElement(Document doc) {
		Element mapTo = doc.createElement("map-to");
		mapTo.setAttribute("xml", "EAI");
		return mapTo;
	}

	private void parseXMLFromRequestFile() throws Exception {
		requsetDataColumns = getColumnsFormXML("xml/WDGF_" + this.service_name + "_Req.xml", TagEnum.REQUEST);

	}

	private void parseXMLFromResponsetFile() throws Exception {
		responseDataColumns = getColumnsFormXML("xml/WDGF_" + this.service_name + "_Rsp.xml", TagEnum.RESPONSE_SINGLE);
		outRecordDataColumns = getColumnsFormXML("xml/WDGF_" + this.service_name + "_Rsp.xml",
				TagEnum.RESPONSE_MULTIPLE_RECORD);
	}

	public EAIGenerator() throws Exception {
		Properties prop = new Properties();
		InputStream input = null;

		try {
			input = EAIGenerator.class.getClassLoader().getResourceAsStream("eai.properties");
			if (input == null) {
				System.out.println("Sorry, unable to find eai.properties");
				return;
			}

			// load a properties file
			prop.load(input);

			default_package = prop.getProperty("default_package");

			default_service_package = prop.getProperty("default_service_package");

			headerBean = prop.getProperty("headerBean");

			hostHeaderBean = prop.getProperty("hostHeaderBean");

			outRecordTag = prop.getProperty("outRecordTag");

			outRecordBeansName = prop.getProperty("outRecordBeansName");

			ROOT_TAG = prop.getProperty("rootTag");

			version = prop.getProperty("version");

			author = prop.getProperty("author");

			company = prop.getProperty("company");

			copyright = prop.getProperty("copyright");

		} catch (Exception ex) {
			throw ex;
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static void main(String[] args) throws Exception {

		List<String> eais = new ArrayList<String>();

		if (args != null) {
			for (String arg : args) {
				eais.add(arg);
			}
		}

		// eais.add("DIGACMSQ");
		// eais.add("DIGEVTAM");
		// eais.add("DIGFNDCM");
		// eais.add("DIGFNDHQ");
		eais.add("DIGTXCAN");
		// eais.add("DIGTXRTN");

		EAIGenerator generator = new EAIGenerator();

		for (String eaiName : eais) {
			generator.init(eaiName);
			generator.parseXML();
			generator.createCode();
		}
	}

	private JavaClassSource implementGetSuccessHostReturnCode(JavaClassSource javaClass) {

		javaClass.addMethod().setProtected().setName("getSuccessHostReturnCode").setReturnType("String[]")
				.setBody("return new String[] {\"\"};");
		return javaClass;
	}

	private JavaClassSource implementGetEaiResponseBeanMethod(JavaClassSource javaClass,
			JavaClassSource eaiResponseBean) {

		javaClass.addMethod().setProtected().setName("getEaiResponseBean").setReturnType(eaiResponseBean.getName())
				.setBody("return new " + eaiResponseBean.getName() + "();").addAnnotation("Override");

		return javaClass;
	}

	private void implementGetEaiRequestBeanMethod(JavaClassSource javaClass, JavaClassSource eaiRequestBean,
			JavaClassSource requestBean, JavaClassSource paramBean) {
		javaClass.addMethod("@Override protected " + eaiRequestBean.getName() + " getEaiRequestBean( Long sequence, "
				+ paramBean.getName() + " paramBean ) " + eaiRequestBean.getName() + " eaiRequestBean = new "
				+ eaiRequestBean.getName() + "();"
				+ "eaiRequestBean.setEaiHeaderBean( getEeaiHeaderBean( sequence ) ); "
				+ "eaiRequestBean.setHostHeaderBean( getHostHeaderBean() ); "
				+ "eaiRequestBean.setRequestDataBean( getRequestDataBean( paramBean ) );" + "return  eaiRequestBean; ");

		javaClass.addMethod("private EaiHeaderBean getEeaiHeaderBean( Long sourceSeq )"
				+ "EaiHeaderBean bean = new EaiHeaderBean();" + "bean.setMsgName( getEaiCode() );"
				+ "bean.setSourceChannel( SOURCE_CHANNEL );" + "bean.setDestinationChannel( DESTINATION_CHANNEL );"
				+ "bean.setTransactionId( getEaiCode() );" + " bean.setSourceSeq( String.valueOf( sourceSeq ) );"
				+ "bean.setEaiReturnCode( \"\" );" + "bean.setHostReturnCode( \"\" );"
				+ "bean.setEaiReturnCode( \"\" ); return bean;");

		javaClass.addMethod().setPrivate().setName("getHostHeaderBean").setReturnType("HostHeaderBean")
				.setBody("return new HostHeaderBean();");

		String body = requestBeanSetByParam("requestBean", "paramBean");

		javaClass.addMethod().setPrivate().setName("getRequestDataBean").setReturnType(requestBean.getName())
				.setParameters(paramBean.getName() + " paramBean")
				.setBody(requestBean.getName() + " requestBean = new " + requestBean.getName() + "();\n\n" + body

						+ "return requestBean;");

	}

	private String requestBeanSetByParam(String variableName, String paramVariableName) {

		StringBuilder builder = new StringBuilder();

		String paramVariableTemplate = variableName + ".set%s( StringUtils.%sPad(" + paramVariableName
				+ ".get%s(),%s,'%s');\n";

		String docTemplate = variableName + ".set%s( StringUtils.%sPad(%s,%s,%s));\n";

		for (ElementObject elementObject : requsetDataColumns) {

			Map<String, String> columnAttribute = parseColumnAttribute(elementObject.getColumnAttribute());

			String name = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, elementObject.getBeanName());

			if (elementObject.isInParam()) {
				String paramName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, elementObject.getParamName());
				StringUtils.leftPad("1", 1);
				builder.append(String.format(paramVariableTemplate, name, columnAttribute.get("type"), paramName,
						columnAttribute.get("len"), columnAttribute.get("padChar")));
			} else {
				String defaultValue = elementObject.getDefaultValue();

				if (defaultValue.startsWith("request")) {
					builder.append(getSetMethod(docTemplate, name, defaultValue.replace("request", variableName),
							columnAttribute));
				} else if (defaultValue.startsWith("parameter")) {
					builder.append(getSetMethod(docTemplate, name, defaultValue.replace("parameter", paramVariableName),
							columnAttribute));
				} else {
					builder.append(getSetMethod(docTemplate, name, "\"" + elementObject.getDefaultValue() + "\"",
							columnAttribute));
				}
			}
		}

		return builder.toString();
	}

	private String getSetMethod(String template, String name, String defaultValue,
			Map<String, String> columnAttribute) {
		return String.format(template, name, columnAttribute.get("type"), defaultValue, columnAttribute.get("len"),
				columnAttribute.get("padChar"));
	}

	private void implementExcludeErrorCodeMethod(JavaClassSource javaClass) {
		javaClass.addMethod("@Override protected String[] excludeErrorCode() { return new String[]{};}");
	}

	private void implementGetEaiCodeMethod(JavaClassSource javaClass) {
		javaClass.addMethod(
				"/** (non-Javadoc)\n*\n*\n@see com.tsb.tsdib.ws.service.eai.BaseEaiService#getEaiCode()*/@Override public String getEaiCode() { return SYS_ID; }");
	}

	private void addStaticFields(JavaClassSource javaClass) {
		javaClass.addField("private static final String SYS_ID = \"" + service_name.toUpperCase() + "\"");
		javaClass.addField("private static final String SOURCE_CHANNEL = \"" + "DIGT" + "\"");
		javaClass.addField("private static final String DESTINATION_CHANNEL = \"" + "ACP" + "\"");
	}

	private void addImportClass(JavaClassSource javaClass, JavaClassSource eaiRequestBean,
			JavaClassSource eaiResponseBean, JavaClassSource requestBean, JavaClassSource responseBean,
			JavaClassSource paramBean, JavaClassSource actualBean) {

		javaClass.addImport(eaiRequestBean);
		javaClass.addImport(eaiResponseBean);

		javaClass.addImport(requestBean);
		javaClass.addImport(responseBean);
		javaClass.addImport(paramBean);
		javaClass.addImport(actualBean);

		javaClass.addImport("com.tsb.tsdib.ws.bean.eai.common.EaiHeaderBean");
		javaClass.addImport("com.tsb.tsdib.ws.bean.eai.common.HostHeaderBean");
		javaClass.addImport("org.apache.commons.lang3.StringUtils");
	}

	private void implementSendMethod(JavaClassSource javaClass, JavaClassSource responseBean, JavaClassSource paramBean,
			JavaClassSource actualBean) {
		javaClass.addMethod(
				"/** (non-Javadoc)\n*\n*\n@see com.tsb.tsdib.ws.service.eai.BaseEaiService#excludeErrorCode()*/"
						+ "@Override public " + actualBean.getName() + " send( Long sequence, " + paramBean.getName()
						+ " paramBean ) {" + " " + responseBean.getName()
						+ " responseBean = doEai( sequence, paramBean ).getResponseDataBean();" + "return parseResponse"
						+ service_name + "Bean( responseBean ); }");

		javaClass.addMethod("private " + actualBean.getName() + " parseResponse" + service_name + "Bean( "
				+ responseBean.getName() + " responseBean ) {" + actualBean.getName() + " actualBean = new "
				+ actualBean.getName() + "();" + "return actualBean;}");

	}

	private JavaClassSource generateEAIBean(String name, Class<?> extendSuperType, JavaClassSource propertyClass,
			String propertyName) {

		final JavaClassSource javaClass = Roaster.create(JavaClassSource.class);
		javaClass.setPackage(package_name).setName(name).extendSuperType(extendSuperType);

		if (propertyClass != null)
			javaClass.addProperty(propertyClass, propertyName);

		BufferedWriter output = null;
		try {
			File file = new File("output/" + service_name.toLowerCase() + "/" + name + ".java");
			output = new BufferedWriter(new FileWriter(file));
			output.write(javaClass.toString());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null)
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return javaClass;
	}

	private Map<String, String> parseColumnAttribute(String column) {

		Map<String, String> result = new HashMap<String, String>();
		Pattern pattern = Pattern.compile("^(\\w)\\((\\d+)\\)");
		Matcher matcher = pattern.matcher(column);

		String padChar = "";

		if (matcher.find()) {
			String type = matcher.group(1);

			if ("X".equals(type)) {
				type = "right";
				padChar = "\"\"";
			} else if ("9".equals(type)) {
				type = "left";
				padChar = "\"0\"";
			} else {
				throw new RuntimeException();
			}

			result.put("type", type);
			result.put("len", StringUtils.strip(matcher.group(2), "0"));
			result.put("padChar", padChar);
		}
		return result;

	}
}
