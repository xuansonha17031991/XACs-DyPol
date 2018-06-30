package demo.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Policy implements Serializable {

	Policy() {

	}

	Policy(PAP pap) {
		this.pap = pap;
	}

	PolicySet ps;
	PAP pap;
	static int nextId = 0;
	final int id = nextId++;
	public String effect;
	ArrayList<Target> targets = new ArrayList<>();
	List<ApplicationConstraint> acs = new ArrayList<>();
	
	Set<String> getAttributeTypeSet() {
		Set<String> res = new HashSet<>();
		for (Target target : targets) {
			res.add(target.attributeType);
		}
		return res;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\tPolicy: " + id);
		if (effect != null) {
			sb.append(" (Effect is " + effect + ")");
		}
		sb.append("\n");

		Set<String> atSet = getAttributeTypeSet();
		if (!atSet.isEmpty()) {
			sb.append("\t{");
			sb.append("\n");
			for (String attributeType : atSet) {
				sb.append("\t\tat" + pap.getIndexOfAttributeType(attributeType) + ": " + attributeType);
				sb.append("\n");
			}

			sb.append("\t}");
			sb.append("\n");
		}

		sb.append("\t{");
		sb.append("\n");
		for (ApplicationConstraint ac : acs) {
			sb.append("\t\t" + ac);
			sb.append("\n");
		}
		sb.append("\t}");
		sb.append("\n");
		
		return sb.toString();
	}

	public String export() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		Set<String> atSet = getAttributeTypeSet();
		if (!atSet.isEmpty()) {
			sb.append("{");
			boolean firstDone = false;
			for (String attributeType : atSet) {
				if (firstDone) {
					sb.append(";");
				} else {
					firstDone = true;
				}
				sb.append("at" + pap.getIndexOfAttributeType(attributeType) + ": " + attributeType);
			}
			sb.append("}");
		}

		sb.append("{");
		boolean firstDone = false;
		for (ApplicationConstraint ac : acs) {
			if (firstDone) {
				sb.append(";");
			} else {
				firstDone = true;
			}
			sb.append(ac);
		}
		sb.append("}");
		sb.append("}");
		return sb.toString();
	}
}
