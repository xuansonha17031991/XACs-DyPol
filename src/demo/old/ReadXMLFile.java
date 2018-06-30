package demo.old;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.helpers.DefaultHandler;

public class ReadXMLFile {
	private static final String REQ_SEPARATOR = ";";
	static final boolean debug = false;
	static final PAP pap = new PAP();

	public static void main(String argv[]) throws FileNotFoundException, IOException {
//		testChinhSuaChinhSach();
		testEvaluate();
	}

	public static void testChinhSuaChinhSach() throws FileNotFoundException, IOException {
		preparation();
		try (BufferedReader br = new BufferedReader(new FileReader("testcases/chinh-sua-chinh-sach.csv"))) {
			boolean firstDone = false;
			for (String line; (line = br.readLine()) != null;) {
				if (!firstDone) {
					firstDone = true;
					continue;
				}
				String[] items = line.split(",");
				int idx = 0;
				String requestString = items[idx++];
				Integer policyId = Integer.valueOf(items[idx++]);
				String newAt = items[idx++];
				String newAc = items[idx++];
				String outputReq = items[idx++];
				String outputPolicy = items[idx++];
				String pass = items[idx++];
				String result = items[idx++];

				Request req = parseQuery(requestString);
				Response res = evaluateQuery(req.data);
				assert res.result.equals(result);
				assert res.relatedPolicy.id == policyId;
				
				Target target = new Target();
				target.attributeType = newAt;
				target.attributeValue = newAc;
				target.functionId = "string-equal";
				res.relatedPolicy.targets.add(target);
				pap.addApplicationConstraint(res.relatedPolicy, target, false);
				System.err.println(res.relatedPolicy.export());
				assert res.relatedPolicy.export().equals(outputPolicy);

				res = evaluateQuery(req.data);
				System.err.println(res.result);
				
				Request newReq = rewriteQuery(res.relatedPolicy, req);
				System.err.println(newReq);
				assert newReq.toString().equals(outputReq);
				System.err.println(evaluateQuery(newReq.data).result);

				pap.rollback();
			}
		}
	}

	private static Request rewriteQuery(Policy relatedPolicy, Request req) {
		String newAt = null;
		Request newReq = new Request();
		newReq.data = new LinkedHashMap<>(req.data);
		for (String attributeType : relatedPolicy.getAttributeTypeSet()) {
			if (!req.data.keySet().contains(attributeType)) {
				newAt = attributeType;
				break;
			}
		}

		String newValue = "*";
		newReq.data.put(newAt, newValue);
		return newReq;
	}

	public static void testChange() {
		preparation();

		pap.removeApplicationConstraint(15, false);
		System.err.println(pap);
		pap.rollback();

		pap.removeAttributeType("role", false);
		System.err.println(pap);
		pap.rollback();
		
		pap.removeAttributeType("isConflicted", false);
		System.err.println(pap);
		pap.rollback();

		pap.modifyApplicationConstraint(15, "write", false);
		System.err.println(pap);
		pap.rollback();
	}

	public static void testEvaluate() throws FileNotFoundException, IOException {
		long startPerformanceTime = System.currentTimeMillis();

		long startTime = System.currentTimeMillis();
		preparation();

		// Print first part
		System.out.println(pap);
		
		// Print second part
		printApplicationSpace();


		pap.printAllAttributeValuesByTypes();

		long endTime = System.currentTimeMillis();
		long totalTime = endTime - startTime;
		System.out.println("Policy conversion time fraction: " + totalTime);

		// Read query
		ArrayList<HashMap<String, String>> queries = readQuery("request/Continue-a_test.txt");

		// Testing query
		evaluateQuery(startPerformanceTime, queries);
	}

	private static void printApplicationSpace() {
		for (PolicySet ps : pap.policySetList) {
			for (Policy p : ps.policies) {
				if (p.effect == null) {
					continue;
				}
				// Title
				System.out.println("(" + p.id + ") : <D, P, IN, NA>");

				// Finding set of deny or permit
				ArrayList<String> result = new ArrayList<String>();
				for (String id : pap.attributeTypeIndexMap.keySet()) {
					ArrayList<String> sameIdTargets = new ArrayList<String>();
					for (Target t : p.targets) {
						if (id.compareTo(t.attributeType) == 0)
							sameIdTargets.add(t.name);
					}
					result = decartesProduct(result, sameIdTargets);
				}

				// Deny set
				if (p.effect.compareTo("Deny") == 0) {
					if (p.targets.size() == 0)
						System.out.println("D : true");
					else {
						System.out.print("D : ");
						if (result.size() == 1)
							if (result.get(0).contains(" "))
								System.out.println("and (" + result.get(0) + ")");
							else
								System.out.println(result.get(0));
						else {
							System.out.print("or (");
							for (String s : result)
								System.out.print("and (" + s + ")");
							System.out.println(")");
						}
					}
				}
				else
					System.out.println("D : Empty");

				// Permit set
				if (p.effect.compareTo("Permit") == 0) {
					if (p.targets.size() == 0)
						System.out.println("P : true");
					else {
						System.out.print("P : ");
						if (result.size() == 1)
							if (result.get(0).contains(" "))
								System.out.println("and (" + result.get(0) + ")");
							else
								System.out.println(result.get(0));
						else {
							System.out.print("or (");
							for (String s : result)
								System.out.print("and (" + s + ")");
							System.out.println(")");
						}
					}
				} else
					System.out.println("P : Empty");

				// Indeterminate set
				if (p.targets.size() == 0)
					System.out.println("IN : Empty");
				else {
					ArrayList<String> tempResult = new ArrayList<String>();
					for (int idx = 1; idx < p.ps.constraintPolicy.acs.size(); idx++) {
						ApplicationConstraint ac = p.ps.constraintPolicy.acs.get(idx);
						for (Target t : p.targets) {
							if (ac.attributeType.compareTo(t.attributeType) == 0) {
								tempResult.add(ac.getName());
								break;
							}
						}
					}

					if (tempResult.size() == 0)
						System.out.println("IN : Empty");
					else if (tempResult.size() == 1)
						System.out.println("IN : " + tempResult.get(0));
						else
					{
						System.out.print("IN : or ( ");
						for (String s : tempResult)
							System.out.print(s + " ");
						System.out.println(")");
					}
				}

				// NA set
				if (p.targets.size() == 0)
					System.out.println("NA : Empty");
				else {
					System.out.println("NA : not ( " + p.ps.constraintPolicy.acs.get(0).getName() + ")");
				}

			}
		}
	}

	private static void preparation() {
		// Parser
		try {
			SAXParserFactory factory = SAXParserFactory.newInstance();
			SAXParser saxParser = factory.newSAXParser();
			DefaultHandler handler = pap.createParserHandler();
			saxParser.parse("data/continue-a-test.xml", handler);
		} catch (Exception e) {
			e.printStackTrace();
		}

		
		for (PolicySet ps : pap.policySetList) {
			// Special case policy : set target
			if (ps.target != null) {
				pap.addApplicationConstraint(ps, ps.target);
				if (debug) {
					System.out.println(pap.hasAttributeType(ps.target.attributeType));
					System.out.println(pap.numberOfAttributeTypes());
				}
			}

			for (Policy p : ps.policies) {
				Set<String> attributeTypes = new HashSet<>();
				for (Target t : p.targets) {
					pap.addApplicationConstraint(p, t);
					attributeTypes.add(t.attributeType);
				}

				pap.addDecisionConstraint(p);
			}
		}

	}

	private static ArrayList<HashMap<String, String>> readQuery(String fileName)
			throws IOException, FileNotFoundException {
		ArrayList<HashMap<String, String>> queries = new ArrayList<HashMap<String, String>>();
		long startConversionQueryTime = System.currentTimeMillis();
		try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
			for (String line; (line = br.readLine()) != null;) {
				queries.add(parseQuery(line).data);
			}
		}

		// Query conversion time fraction

		boolean testPrint = true;
		if (testPrint) {
			int n = 0;

			for (HashMap<String, String> q : queries) {
				n++;
				// if (n>3)
				// break;
				System.out.println(n);
				for (String k : q.keySet()) {
					System.out.println("\t\"" + k + "\":\"" + q.get(k));
				}
			}
		}
		long endConversionQueryTime = System.currentTimeMillis();
		long totalConversionQueryTime = endConversionQueryTime - startConversionQueryTime;
		System.out.println("total conversion request time: " + totalConversionQueryTime);
		return queries;
	}

	private static Request parseQuery(String line) {
		Request req = new Request();
		// process the line.
		String temp = line.substring(1, line.length() - 1);
		// System.out.println(temp);
		String[] kv = temp.split(REQ_SEPARATOR);
		HashMap<String, String> t = new HashMap<String, String>();
		for (String s : kv) {
			String[] kv2 = new String[2];
			int i = s.lastIndexOf("\":\"");
			kv2[0] = s.substring(1, i);
			kv2[1] = s.substring(i + 3, s.length() - 1);
			t.put(kv2[0], kv2[1]);
		}

		req.data = t;
		return req;
	}

	private static void evaluateQuery(long startPerformanceTime, ArrayList<HashMap<String, String>> queries) {
		int n = 0;
		long startQueryTime = System.currentTimeMillis();
		
		for (HashMap<String, String> q : queries) {
			n++;
			System.out.println("Query " + n);
			evaluateQuery(q);

			long endQueryTime = System.currentTimeMillis();
			long totalQueryTime = endQueryTime - startQueryTime;
			System.out.println("total rewrite request time: " + totalQueryTime);

		}

		long endPerformanceTime = System.currentTimeMillis();
		long totalPerformanceTime = endPerformanceTime - startPerformanceTime;
		System.out.println("total performance request time: " + totalPerformanceTime);
	}

	private static Response evaluateQuery(HashMap<String, String> q) {
		Response res = null;
		boolean applicable = false;
		for (PolicySet policySet : pap.policySetList) {
			if (q.keySet().contains(policySet.target.attributeType)
					&& q.get(policySet.target.attributeType).equals(policySet.target.attributeValue)) {
				res = evaluate(policySet, q);
				applicable = true;
			}
		}

		if (!applicable) {
			res = new Response();
			res.result = "NA";
		}
		return res;
	}

	private static Response evaluate(PolicySet resPolicySet, HashMap<String, String> q) {
		// Strategy: using First Application combination algorithm
		Response res = new Response();
		boolean applicable = false;
		for (String attributeType : q.keySet()) {
			if (resPolicySet.target.attributeType.equals(attributeType)
					&& resPolicySet.target.attributeValue.equals(q.get(attributeType))) {
				applicable = true;
				break;
			}
		}
		
		if (!applicable) {
			res.result = "NA";
			return res;
		}

		for (Policy p : resPolicySet.policies) {
			Pair<String, Boolean> evalPol = evaluateByPolicy(p, q);
			
			if (evalPol.b) {
				res.result = evalPol.a;
				res.relatedPolicy = p;
				return res;
			} else if (evalPol.a != null) {
				res.result = evalPol.a;
				res.relatedPolicy = p;
				return res;
			}
		}

		throw new UnsupportedOperationException();
	}

	private static Pair<String, Boolean> evaluateByPolicy(Policy p, HashMap<String, String> q) {
		boolean notPrintFalse = true;
		Set<String> attributeTypes = p.getAttributeTypeSet();
		if (attributeTypes.isEmpty()) {
			return Pair.of(p.effect, true);
		}
		if (q.keySet().size() <= attributeTypes.size()) {
			if (!notPrintFalse)
				System.out.println("Policy " + p.id + " IN: false");
			return Pair.of("IN", false);
		} else {
			boolean qatSatisfied = true;
			for (String at : attributeTypes) {
				boolean existQat = false;
				for (String qat : q.keySet()) {
					if (at.equals(qat)) {
						existQat = true;
						break;
					}
				}

				if (!existQat) {
					qatSatisfied = false;
					break;
				}
			}
			
			if (!qatSatisfied) {
				return Pair.of("IN", false);
			}
			
			Set<String> satisfiedAts = new HashSet<>();
			for (Target t : p.targets) {// target
				for (String k : q.keySet()) { // query
					if (k.equals(t.attributeType)) {
						if (t.functionId.equals("string-equal")) {
							if (q.get(k).equals(t.attributeValue)) {
								satisfiedAts.add(t.attributeType);
							}
							break;
						} else if (t.functionId == "integer-greater-than") {
							if (Integer.parseInt(q.get(k)) > Integer.parseInt(t.attributeValue)) {
								satisfiedAts.add(t.attributeType);
							}
							break;
						}
					}
				}
			}
			if (satisfiedAts.size() != attributeTypes.size()) {
				return Pair.of(null, false);
			}

			return Pair.of(p.effect, true);
		}
	}

	static ArrayList<String> decartesProduct(ArrayList<String> a, ArrayList<String> b) {
		ArrayList<String> c = new ArrayList<String>();
		if (a.size() == 0 && b.size() != 0)
			return b;
		if (b.size() == 0 && a.size() != 0)
			return a;
		for (String sa : a)
			for (String sb : b)
				c.add(sa + " " + sb);
		return c;
	}

}