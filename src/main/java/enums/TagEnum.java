package enums;

import com.google.common.base.CaseFormat;

public enum TagEnum {

	REQUEST("Request_Data"), RESPONSE_SINGLE("Response_Data"), RESPONSE_MULTIPLE_RECORD("OUT-RECORD");
	private String tagName;

	private TagEnum(String tagName) {
		this.tagName = tagName;
	}

	public String getTagName() {
		return tagName;
	}

	@Override
	public String toString() {
		return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, name());
	}

}
