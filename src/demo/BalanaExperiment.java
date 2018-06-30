package demo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.finder.AttributeFinder;
import org.wso2.balana.finder.AttributeFinderModule;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import org.wso2.balana.samples.image.filtering.HeathCareAttributeFinderModule;

public abstract class BalanaExperiment extends AbstractDynamicAccessControl {
	private final Balana balana;
	protected boolean needChange = true;
	
	PDP pdp;
	PDPConfig pdpConfig;
	
	public BalanaExperiment() {
		this("experiments/health_care.healthCarePsPatientRecord.xml");
	}
	
	public BalanaExperiment(String policyPath) {
		balana = Balana.getInstance();
		pdpConfig = balana.getPdpConfig();
		
		AttributeFinder attributeFinder = pdpConfig.getAttributeFinder();
		List<AttributeFinderModule> finderModules = attributeFinder.getModules();
		finderModules.add(new HeathCareAttributeFinderModule());
		attributeFinder.setModules(finderModules);
		
		pdpConfig.getAttributeFinder().setModules(finderModules);
		setPolicyLocationToPdpConfig(policyPath);
		
		this.pdp = new PDP(this.pdpConfig);
	}

	protected void updatePolicies(String policyLocation, boolean filtering) {
		setPolicyLocationToPdpConfig(policyLocation);
		loadPolicies();
		if (filtering) {
			addAction(ProcessAction.FilterPolicies);
		} else {
			addAction(ProcessAction.LoadPolicies);
		}
	}

	private void setPolicyLocationToPdpConfig(String policyLocation) {
		Set<String> locations = new HashSet<>();
		locations.add(policyLocation);
		Set<PolicyFinderModule> policyFinderModules = new HashSet();
		FileBasedPolicyFinderModule fileBasedPolicyFinderModule = new FileBasedPolicyFinderModule(locations);
		policyFinderModules.add(fileBasedPolicyFinderModule);
		this.pdpConfig.getPolicyFinder().setModules(policyFinderModules);
	}

	@Override
	public Object parseRequest(String inputRequest, RequestInputFormat requestFormat) {
		if (requestFormat != RequestInputFormat.XML) {
			return null;
		}
		
		try {
			return RequestCtxFactory.getFactory().getRequestCtx(inputRequest.replaceAll(">\\s+<", "><"));
		} catch (ParsingException e) {
			return null;
		}
	}

	@Override
	public void loadPolicies() {
		this.pdpConfig.getPolicyFinder().init();
	}

	@Override
	public Object evaluateRequest(Object request) {
		return pdp.evaluate((RequestCtx) request);
	}
	
	@Override
	protected PolicyChangeType checkDynamicPolicyBeforeResponse(Object response) {
		// System.out.println(((ResponseCtx) response).encode());
		if (needChange) {
			needChange = false;
			return policyChangedOnce(response);
		}
		return null;
	}

	protected abstract PolicyChangeType policyChangedOnce(Object response);

	protected void printExperiment(Object response) {
		// System.out.println(((ResponseCtx) response).encode());
		System.out.println(sum);

		Map<ProcessAction, Integer> map = new HashMap<AbstractDynamicAccessControl.ProcessAction, Integer>();
		for (ProcessAction ac : actionsQueue) {
			Integer currentCount = map.get(ac);
			if (currentCount == null) {
				currentCount = 1;
			} else {
				currentCount++;
			}
			map.put(ac, currentCount);
		}

		System.out.println(actionsQueue);
		System.out.println(map);
	}

}
