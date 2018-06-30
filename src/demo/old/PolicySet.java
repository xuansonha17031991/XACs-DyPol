package demo.old;

import java.io.Serializable;
import java.util.ArrayList;

public class PolicySet implements Serializable {
	static int nextId = 0;
	final int id = nextId++;
	PAP pap;
	Target target;
	ArrayList<Policy> policies = new ArrayList<>();

	// Containing Corresponding target name of attIds
	Policy constraintPolicy;
	ApplicationConstraint ac;

	public PolicySet() {
	}

	public PolicySet(PAP pap) {
		this.pap = pap;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("PolicySet");
		sb.append("\n");
		sb.append("{");
		sb.append("\n");

		sb.append("\tat" + pap.getIndexOfAttributeType(target.attributeType) + ": " + target.attributeType);
		sb.append("\n");
		sb.append("\n");

		sb.append("\t" + ac);
		sb.append("\n");
		sb.append("\n");

		for (Policy pol : policies) {
			sb.append(pol);
		}
		
		sb.append(constraintPolicy);

		sb.append("}");
		sb.append("\n");
		return sb.toString();
	}
}