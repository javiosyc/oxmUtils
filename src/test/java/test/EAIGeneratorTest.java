package test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import generator.EAIGenerator;

public class EAIGeneratorTest {

	@Test
	public void test() throws Exception {

		List<String> eais = new ArrayList<String>();

		// eais.add("DIGACMSQ");
		// eais.add("DIGEVTAM");
		// eais.add("DIGFNDCM");
		// eais.add("DIGFNDHQ");
		// eais.add("DIGTXCAN");
		eais.add("DIGFNDMI");
		// eais.add("DIGTXRTN");

		EAIGenerator generator = new EAIGenerator();

		for (String eaiName : eais) {
			generator.init(eaiName);
			generator.parseXML();
			generator.createCode();
		}
	}
}
