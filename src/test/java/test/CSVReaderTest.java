package test;

import java.io.IOException;

import org.junit.Test;

import reader.DocReader;

public class CSVReaderTest {

	@Test
	public void test() throws IOException {
		DocReader reader = new DocReader();

		reader.readFromCVS("xml/WDGF_DIGFNDMI_Req.csv");

		reader.showData();
	}
}
