package test;

import org.junit.Test;

import enums.TagEnum;
import model.ElementObject;
import reader.XmlReader;

public class XmlReaderTester {

	@Test
	public void getData() throws Exception {
		XmlReader reader = new XmlReader();
		// reader.getData("xml/WDGF_DIGFNDMI_Req.xml", TagEnum.REQUEST);
		// reader.getData("xml/WDGF_DIGFNDMI_Req.xml", TagEnum.REQUEST);
		reader.getData("xml/WDGF_DIGFNDMI_Rsp.xml", TagEnum.RESPONSE_SINGLE);
		for (ElementObject param : reader.getColumns()) {
			System.out.println(param);
		}
	}
}
