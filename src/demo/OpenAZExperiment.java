package demo;


public class OpenAZExperiment extends AbstractDynamicAccessControl {

	@Override
	public Object parseRequest(String inputRequest, RequestInputFormat requestFormat) {
		return null;
	}

	@Override
	public void loadPolicies() {

	}

	@Override
	public Object evaluateRequest(Object request) {
		return null;
	}

	@Override
	public PolicyChangeType checkDynamicPolicyBeforeResponse(Object response) {
		return null;
	}

	@Override
	protected Object handlePolicyChange(Object request, Object response) {
		// TODO Auto-generated method stub
		return null;
	}

}
