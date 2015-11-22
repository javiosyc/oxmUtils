package model;

public class ElementObject {
	/**
	 * tag名稱
	 */
	private String tagName;

	/**
	 * name in the requestBean or responseBean
	 */
	private String beanName;

	/**
	 * 中文名稱
	 */
	private String cname;

	/**
	 * 說明
	 */
	private String desc;

	/**
	 * the name in the paramBean or in the actualBean.
	 */
	private String paramName;

	/**
	 * 預設值
	 */
	private String defaultValue;

	/**
	 * 是否存在於 paramBean
	 */
	private boolean isInParam;

	/**
	 * 測試資料值
	 */
	private String testValue;

	/**
	 * 欄 位 屬 性
	 */
	private String columnAttribute;

	public String getTagName() {
		return tagName;
	}

	public void setTagName(String tagName) {
		this.tagName = tagName;
	}

	public String getColumnAttribute() {
		return columnAttribute;
	}

	public void setColumnAttribute(String columnAttribute) {
		this.columnAttribute = columnAttribute;
	}

	public String getBeanName() {
		return beanName;
	}

	public void setBeanName(String beanName) {
		this.beanName = beanName;
	}

	public String getCname() {
		return cname;
	}

	public void setCname(String cname) {
		this.cname = cname;
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

	public boolean isInParam() {
		return isInParam;
	}

	public void setInParam(boolean isInParam) {
		this.isInParam = isInParam;
	}

	public String getTestValue() {
		return testValue;
	}

	public void setTestValue(String testValue) {
		this.testValue = testValue;
	}

	@Override
	public String toString() {
		return "ElementObject [tagName=" + tagName + ", beanName=" + beanName + ", cname=" + cname + ", desc=" + desc
				+ ", paramName=" + paramName + ", defaultValue=" + defaultValue + ", isInParam=" + isInParam
				+ ", testValue=" + testValue + ", columnAttribute=" + columnAttribute + "]";
	}

}
