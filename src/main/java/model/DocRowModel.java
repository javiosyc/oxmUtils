package model;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class DocRowModel implements Comparable<DocRowModel> {
	private int type;
	private String name;
	private String columnAttribute;
	private String cname;
	private String desc;

	private boolean inParam;
	private String paramName;

	public String getParamName() {
		return paramName;
	}

	public void setParamName(String paramName) {
		this.paramName = paramName;
	}

	public boolean isInParam() {
		return inParam;
	}

	public void setInParam(boolean inParam) {
		this.inParam = inParam;
	}

	private String defaultValue;

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
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

	public String getColumnAttribute() {
		return columnAttribute;
	}

	public void setColumnAttribute(String columnAttribute) {
		this.columnAttribute = columnAttribute;
	}

	@Override
	public int compareTo(DocRowModel o) {
		return new CompareToBuilder().append(type, o.type).append(name, o.name).toComparison();
	}

	@Override
	public String toString() {
		return "DocRowModel [type=" + type + ", name=" + name + ", columnAttribute=" + columnAttribute + ", cname="
				+ cname + ", desc=" + desc + ", inParam=" + inParam + ", paramName=" + paramName + ", defaultValue="
				+ defaultValue + "]";
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getTagName() {
		StringBuilder builder = new StringBuilder();

		builder.append(this.type == 1 ? "IN-" : "OUT-");

		return builder.append(this.name).toString();
	}
}
