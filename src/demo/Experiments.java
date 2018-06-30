
package demo;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;

import demo.AbstractDynamicAccessControl.PolicyChangeType;
import demo.AbstractDynamicAccessControl.RequestInputFormat;

public class Experiments {

	public static void main(String[] args) throws Exception {
//		List<String> requestList = new ArrayList<>();
//		String folder = "src/test/resources/testsets/healthcare/xml-requests";
//		File file = new File(folder);
//
//		for (File child : file.listFiles()) {
//			StringBuilder sb = new StringBuilder();
//			BufferedReader br = new BufferedReader(new FileReader(child));
//			for (Iterator<String> itor = br.lines().iterator(); itor.hasNext();) {
//				String line = itor.next();
//				sb.append(line).append("\n");
//			}
//			requestList.add(sb.toString());
//			br.close();
//		}
		
		args = new String[] {"true", "false", "UPDATE_POLICY_INSERT_CONDITION"};
		
		BalanaExperiment.COUNT_FIRST_EVALUATION = Boolean.valueOf(args[0]);
		
		BalanaExperiment.HANDLE_POLICY_CHANGE_TURN_ON = Boolean.valueOf(args[1]);
		
		PolicyChangeType changeType = PolicyChangeType.valueOf(args[2]);
		
		smokeTestSimpleRequest();

		switch (changeType) {
		case DELETE_POLICY:
			testDeletePolicy();
			break;
		case INSERT_POLICY:
			testInsertPolicy();
			break;
		case UPDATE_POLICY_DELETE_CONDITION:
			testDeleteCondition();
			break;
		case UPDATE_POLICY_EDIT_CONDITION:
			testEditCondition();
			break;
		case UPDATE_POLICY_INSERT_CONDITION:
			testInsertCondition();
			break;
		default:
			break;
		}
		
		// testDeletePolicy();
		// testInsertPolicy();
		// testDeleteCondition();
		// testEditCondition();
		// testInsertCondition();
		
	}
	
	private static String readRequest(final String filename) throws FileNotFoundException, IOException {
		BufferedReader br = new BufferedReader(new FileReader(filename));
		StringBuilder sb = new StringBuilder();
		for (Iterator<String> itor = br.lines().iterator(); itor.hasNext();) {
			String line = itor.next();
			sb.append(line).append("\n");
		}
		br.close();
		String requestStr = sb.toString();
		return requestStr;
	}

	private static void smokeTestSimpleRequest() throws Exception {
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				return null;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) throws Exception {
				return response;
			}

		};
		balanaExperiment.evaluate(readRequest("experiments/TestSimpleRequest/Request.1.xml"), RequestInputFormat.XML);
	}
	
	private static void testContinueA() throws FileNotFoundException, IOException, Exception {
		BalanaExperiment balanaExp = new BalanaExperiment("ContinueA.xml") {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				return null;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) throws Exception {
				return response;
			}

		};
		Object response = balanaExp.evaluate(readRequest("testcase/continue-a-xacml3/TestSimpleRequest/Request.1.xml"), RequestInputFormat.XML);
		balanaExp.printExperiment(response);
	}

	private static void testDeletePolicy() throws Exception {
		//final String newPolicy = "testcase/continue-a-xacml3/TestDeletePolicy/continue-a-xacml3.xml";
		//final String newPolicy = "testcase/GEYSERS/TestDeletePolicy/GEYSERS.xml";
		final String newPolicy = "testcase/KMarket/TestDeletePolicy/KMarket.xml";
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				
				updatePolicies(newPolicy, false);

				return PolicyChangeType.DELETE_POLICY;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) throws Exception {
				updatePolicies(getPolicyShort(newPolicy), true);

				long start = System.nanoTime();
				Object ret = evaluateRequest(request);
				long endTime = System.nanoTime();
				sum += (endTime - start);
				addAction(ProcessAction.EvaluateRequest);
				return ret;
			}
			
		};
		//Object response = balanaExperiment.evaluate(readRequest("testcase/continue-a-xacml3/TestDeletePolicy/Request.7.xml"), RequestInputFormat.XML);
		//Object response = balanaExperiment.evaluate(readRequest("testcase/GEYSERS/TestDeletePolicy/Request.7.xml"), RequestInputFormat.XML);
		Object response = balanaExperiment.evaluate(readRequest("testcase/KMarket/TestDeletePolicy/Request.7.xml"), RequestInputFormat.XML);
		balanaExperiment.printExperiment(response);
	}
	
	private static void testInsertPolicy() throws Exception {
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				//updatePolicies( "testcase/continue-a-xacml3/TestInsertPolicy/continue-a-xacml3.xml", false);
				//updatePolicies( "testcase/GEYSERS/TestInsertPolicy/GEYSERS.xml", false);
				updatePolicies( "testcase/KMarket/TestInsertPolicy/KMarket.xml", false);
				return PolicyChangeType.INSERT_POLICY;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) {
				long start = System.nanoTime();
				Object ret = evaluateRequest(request);
				long endTime = System.nanoTime();
				sum += (endTime - start);
				addAction(ProcessAction.EvaluateRequest);
				return ret;
			}
			
		};
		
		//Object response = balanaExperiment.evaluate(readRequest("testcase/continue-a-xacml3/TestInsertPolicy/Request.5.xml"), RequestInputFormat.XML);
		//Object response = balanaExperiment.evaluate(readRequest("testcase/GEYSERS/TestInsertPolicy/Request.5.xml"), RequestInputFormat.XML);
		Object response = balanaExperiment.evaluate(readRequest("testcase/KMarket/TestInsertPolicy/Request.5.xml"), RequestInputFormat.XML);
		balanaExperiment.printExperiment(response);
	}
	
	private static void testDeleteCondition() throws Exception {
		//final String newPolicy = "testcase/continue-a-xacml3/TestDeleteCondition/continue-a-xacml3.xml";
		//final String newPolicy = "testcase/GEYSERS/TestDeleteCondition/DeleteConditionTest.xml";
		final String newPolicy = "testcase/KMarket/TestDeleteCondition/KMarket.xml";
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				updatePolicies( newPolicy, false );
				// There's only one policy left for evaluate
				
				return PolicyChangeType.UPDATE_POLICY_DELETE_CONDITION;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) {
				updatePolicies(getPolicyShort(newPolicy), true);
				long start = System.nanoTime();
				Object ret = evaluateRequest(request);
				long endTime = System.nanoTime();
				sum += (endTime - start);
				addAction(ProcessAction.EvaluateRequest);
				return ret;
			}
			
		};
		
		//Object response = balanaExperiment.evaluate(readRequest("testcase/continue-a-xacml3/TestDeleteCondition/Request.11.xml"), RequestInputFormat.XML);
		//Object response = balanaExperiment.evaluate(readRequest("testcase/GEYSERS/TestDeleteCondition/Request.11.xml"), RequestInputFormat.XML);
		Object response = balanaExperiment.evaluate(readRequest("testcase/KMarket/TestDeleteCondition/Request.11.xml"), RequestInputFormat.XML);
		balanaExperiment.printExperiment(response);
	}
	
	private static void testEditCondition() throws Exception {
		//final String newPolicy = "testcase/continue-a-xacml3/TestEditCondition/contunue-a-xacml3.xml";
		//final String newPolicy = "testcase/GEYSERS/TestEditCondition/EditConditionTest.xml";
		final String newPolicy = "testcase/KMarket/TestEditCondition/KMarket.xml";
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				updatePolicies( newPolicy, false);
				// There's only one policy left for evaluate
				
				return PolicyChangeType.UPDATE_POLICY_EDIT_CONDITION;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) {
				updatePolicies(getPolicyShort(newPolicy), true);
				long start = System.nanoTime();
				Object ret = evaluateRequest(request);
				long endTime = System.nanoTime();
				sum += (endTime - start);
				addAction(ProcessAction.EvaluateRequest);
				return ret;
			}
			
		};
		
		//Object response = balanaExperiment.evaluate(readRequest("testcase/continue-a-xacml3/TestEditCondition/Request.12.xml"), RequestInputFormat.XML);
		//Object response = balanaExperiment.evaluate(readRequest("testcase/GEYSERS/TestEditCondition/Request.12.xml"), RequestInputFormat.XML);
		Object response = balanaExperiment.evaluate(readRequest("testcase/KMarket/TestEditCondition/Request.12.xml"), RequestInputFormat.XML);
		balanaExperiment.printExperiment(response);
	}
	
	private static void testInsertCondition() throws Exception {
		//final String newPolicy = "testcase/continue-a-xacml3/TestInsertCondition/InsertConditionTest.xml";
		//final String newPolicy = "testcase/GEYSERS/TestInsertCondition/InsertConditionTest.xml";
		final String newPolicy = "testcase/KMarket/TestInsertCondition/KMarket.xml";
		BalanaExperiment balanaExperiment = new BalanaExperiment() {

			@Override
			protected PolicyChangeType policyChangedOnce(Object response) {
				updatePolicies(newPolicy, false);
				// There's only one policy left for evaluate
				
				return PolicyChangeType.UPDATE_POLICY_INSERT_CONDITION;
			}

			@Override
			protected Object handlePolicyChange(Object request, Object response) throws Exception {
				updatePolicies(getPolicyShort(newPolicy), true);
				
				Object newRequest = rewrite(request);
				long start = System.nanoTime();
				Object ret = evaluateRequest(newRequest);
				long endTime = System.nanoTime();
				sum += (endTime - start);
				addAction(ProcessAction.EvaluateRequest);
				return ret;
			}
			
			protected Object rewrite(Object request) throws FileNotFoundException, IOException {
				//Object newRequest = parseRequest(readRequest("testcase/continue-a-xacml3/TestInsertCondition/Request.5.xml"), RequestInputFormat.XML);
				//Object newRequest = parseRequest(readRequest("testcase/GEYSERS/TestInsertCondition/Request.5.xml"), RequestInputFormat.XML);
				Object newRequest = parseRequest(readRequest("testcase/KMarket/TestInsertCondition/Request.5.xml"), RequestInputFormat.XML);
				addAction(ProcessAction.RewriteRequest);
				return newRequest;
			}

			
		};
		
		//Object response = balanaExperiment.evaluate(readRequest("testcase/continue-a-xacml3/TestInsertCondition/Request.13.xml"), RequestInputFormat.XML);
		//Object response = balanaExperiment.evaluate(readRequest("testcase/GEYSERS/TestInsertCondition/Request.13.xml"), RequestInputFormat.XML);
		Object response = balanaExperiment.evaluate(readRequest("testcase/KMarket/TestInsertCondition/Request.13.xml"), RequestInputFormat.XML);
		balanaExperiment.printExperiment(response);
	}

	private static String getPolicyShort(String newPolicy) {
		return newPolicy.replace(".xml", "-short.xml");
	}
}
