package demo;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDynamicAccessControl {
	public static boolean HANDLE_POLICY_CHANGE_TURN_ON = false;
	
	public static boolean COUNT_FIRST_EVALUATION = false;
	
	public enum PolicyChangeType {
		DELETE_POLICY,
		INSERT_POLICY,
		UPDATE_POLICY_DELETE_CONDITION,
		UPDATE_POLICY_EDIT_CONDITION,
		UPDATE_POLICY_INSERT_CONDITION
	}
	
	public enum RequestInputFormat {
		JSON,
		XML
	}
	
	public enum ProcessAction {
		ParseRequest,
		LoadPolicies,
		FilterPolicies,
		EvaluateRequest,
		RewriteRequest
	}

	protected abstract Object parseRequest(String inputRequest, RequestInputFormat requestFormat);
	protected abstract void loadPolicies();
	protected abstract Object evaluateRequest(Object request);
	protected abstract PolicyChangeType checkDynamicPolicyBeforeResponse(Object response);
	protected abstract Object handlePolicyChange(Object request, Object response) throws Exception;
	
	public long sum = 0;

	 public List<ProcessAction> actionsQueue = new ArrayList<>();
	
	public Object evaluate(String inputRequest, RequestInputFormat requestFormat) throws Exception {
		sum = 0;
		Object response = null;
		PolicyChangeType changeType = null;
		Object request = parseRequest(inputRequest, requestFormat);
		addAction(ProcessAction.ParseRequest);
		loadPolicies();
		addAction(ProcessAction.LoadPolicies);
		boolean first = true;
		do {
			long startTime = System.nanoTime();
			response = evaluateRequest(request);
			long endTime = System.nanoTime();
			sum += (endTime - startTime);
			if (first && !COUNT_FIRST_EVALUATION) {
				first = false;
				sum = 0;
			}
			addAction(ProcessAction.EvaluateRequest);
			changeType = checkDynamicPolicyBeforeResponse(response);
			
			if (HANDLE_POLICY_CHANGE_TURN_ON && changeType != null) {
				if (changeType == PolicyChangeType.INSERT_POLICY) {
					// Do nothing in this case because there are already the policy for
					// the request in the current response.
				} else {
					response = handlePolicyChange(request, response);
					// After handle policy change, the changeType is reset.
				}

				changeType = checkDynamicPolicyBeforeResponse(response);
			}
		} while (changeType != null);
		
		
		return response;
	}

	protected void addAction(ProcessAction action) {
		 actionsQueue.add(action);
	}
	
}
