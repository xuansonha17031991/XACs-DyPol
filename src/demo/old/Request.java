package demo.old;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public class Request {
	HashMap<String, String> data = new LinkedHashMap<>();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		boolean firstDone = false;
		sb.append("{");
		for (Entry<String, String> entry : data.entrySet()) {
			if (firstDone) {
				sb.append(";");
			} else {
				firstDone = true;
			}
			sb.append("\"" + entry.getKey() + "\":");
			sb.append("\"" + entry.getValue() + "\"");
		}
		sb.append("}");
		return sb.toString();
	}
}
