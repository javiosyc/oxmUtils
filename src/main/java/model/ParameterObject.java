package model;

public class ParameterObject {

	private String name;
	private String desc;

	private String paramName;

	private String defaultValue;

	private String requestTagName;

	private String requestName;

	private boolean isInParam;

	private String testValue;

	public String getRequestName() {
		return requestName;
	}

	public String getTestValue() {
		return testValue;
	}

	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}

	public void setRequestName(String requestName) {
		this.requestName = requestName;
	}

	public boolean isInParam() {
		return isInParam;
	}

	public void setInParam(boolean isInParam) {
		this.isInParam = isInParam;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getRequestTagName() {
		return requestTagName;
	}

	public void setRequestTagName(String requestTagName) {
		this.requestTagName = requestTagName;
	}

	@Override
	public String toString() {
		return "ParameterObject [ name=" + name + ", desc=" + desc + ", paramName=" + paramName + ", defaultValue="
				+ defaultValue + ", requestTagName=" + requestTagName + ", requestName=" + requestName + ", isInParam="
				+ isInParam + ", testValue=" + testValue + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
