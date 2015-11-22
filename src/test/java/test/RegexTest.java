package test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;

public class RegexTest {

	private Pattern pattern = Pattern.compile("^(\\w)\\((\\d+)\\)");

	@Test
	public void test() {

		Matcher m = pattern.matcher("X(20)");

		if (m.find()) {
			String matchedText = m.group(0);

			String x = m.group(1);
			String x2 = m.group(2);
			int matchedFrom = m.start();
			int matchedTo = m.end();

			System.out.println("Text:" + matchedText + ",From:" + matchedFrom + ",To:" + matchedTo);
			System.out.println(x + x2);
		} else {
			System.out.println("didn't match");
		}
	}

}
