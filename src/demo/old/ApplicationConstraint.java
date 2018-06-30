package demo.old;

import java.io.Serializable;

public class ApplicationConstraint implements Serializable {
	int index;
	String content;
	String attributeType;
	private String attributeValue;
	Policy policy;
	PolicySet policySet;

	public ApplicationConstraint() {
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		if (this.content != null && this.attributeValue != null) {
			this.content = this.content.replaceAll(this.attributeValue, attributeValue); // Maybe incorrect.
		}
		this.attributeValue = attributeValue;

	}

	String getName() {
		return "ac" + index;
	}

	@Override
	public String toString() {
		return getName() + ": " + content;
	}
}
