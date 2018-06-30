package demo.old;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.SerializationUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class PAP implements Serializable {

	List<PolicySet> policySetList = new ArrayList<>();
	int ApplicationConstraintIndex = 0;
	int attributeTypeIndex = 0;
	// Hash map
	HashMap<String, HashSet<Pair<String, Integer>>> attTypeAttValuesMap = new HashMap<String, HashSet<Pair<String, Integer>>>();
	Map<String, Integer> attributeTypeIndexMap = new LinkedHashMap<>();

	Map<Integer, ApplicationConstraint> acMap = new LinkedHashMap<>();

	PAP backup = null;

	PAP() {

	}

	private static PAP clone(PAP from) {
		return (PAP) SerializationUtils.clone(from);
	}

	boolean removeAttributeType(String attributeType) {
		return removeAttributeType(attributeType, true);
	}

	boolean removeAttributeType(String attributeType, boolean commit) {
		if (!commit) {
			keepBackup();
		}
		attributeTypeIndexMap.remove(attributeType);
		HashSet<Pair<String, Integer>> set = attTypeAttValuesMap.remove(attributeType);
		
		for (Pair<String, Integer> pair : set) {
			ApplicationConstraint ac = acMap.remove(pair.b);
			Policy pol = ac.policy;
			if (pol == null) {
				throw new UnsupportedOperationException();
			}
			
			pol.acs.remove(ac);
			for (Target target : pol.targets) {
				pol.targets.remove(target);
				break;
			}

			addDecisionConstraint(pol);
		}
		
		for (PolicySet policySet : policySetList) {
			for (ApplicationConstraint ac : policySet.constraintPolicy.acs) {
				if (ac.attributeType.compareTo(attributeType) == 0) {
					policySet.constraintPolicy.acs.remove(ac);
					break;
				}
			}
		}
		return true;
	}

	boolean removeApplicationConstraint(Integer acIndex) {
		return removeApplicationConstraint(acIndex, true);
	}

	boolean removeApplicationConstraint(Integer acIndex, boolean commit) {
		if (!commit) {
			keepBackup();
		}
		ApplicationConstraint aCons = acMap.remove(acIndex);
		Policy pol = aCons.policy;
		if (pol == null) {
			throw new UnsupportedOperationException();
		}
		pol.acs.remove(aCons);
	
		attTypeAttValuesMap.get(aCons.attributeType).remove(Pair.of(aCons.getAttributeValue(), aCons.index));
		if (attTypeAttValuesMap.get(aCons.attributeType).isEmpty()) {
			attTypeAttValuesMap.remove(aCons.attributeType);
	
			attributeTypeIndexMap.remove(aCons.attributeType);
		}
	
		for (Target t : pol.targets) {
			if (t.attributeType.compareTo(aCons.attributeType) == 0) {
				pol.targets.remove(t);
				break;
			}
		}

		addDecisionConstraint(pol);
	
		return true;
	}

	ApplicationConstraint modifyApplicationConstraint(Integer acIndex, String newAttributeValue) {
		return modifyApplicationConstraint(acIndex, newAttributeValue, true);
	}

	ApplicationConstraint modifyApplicationConstraint(Integer acIndex, String newAttributeValue, boolean commit) {
		if (!commit) {
			keepBackup();
		}
	
		ApplicationConstraint aCons = acMap.get(acIndex);
		Policy pol = aCons.policy;
		if (pol == null) {
			throw new UnsupportedOperationException();
		}
	
		aCons.setAttributeValue(newAttributeValue);
	
		attTypeAttValuesMap.get(aCons.attributeType).remove(Pair.of(aCons.getAttributeValue(), aCons.index));
		if (!attTypeAttValuesMap.get(aCons.attributeType).contains(Pair.of(aCons.getAttributeValue(), aCons.index))) {
			attTypeAttValuesMap.get(aCons.attributeType).add(Pair.of(newAttributeValue, aCons.index));
		}
	
		for (Target t : pol.targets) {
			if (t.attributeType.compareTo(aCons.attributeType) == 0) {
				t.attributeValue = newAttributeValue;
			}
		}
	
		return aCons;
	}

	ApplicationConstraint addApplicationConstraint(Policy p, Target target, boolean commit) {
		if (!commit) {
			keepBackup();
		}
	
		ApplicationConstraint ac = new ApplicationConstraint();
		ac.index = ApplicationConstraintIndex++;
		target.name = "ac" + ac.index;
	
		String operator = "E"; // Belong to a set operator
	
		if (target.attributeValue.compareToIgnoreCase("true") == 0
				|| target.attributeValue.compareToIgnoreCase("false") == 0) {
			operator = "=";
			target.attributeValue = "\"" + target.attributeValue + "\"";
			ac.content = target.attributeType + " " + operator + " " + target.attributeValue;
		} else {
			ac.content = target.attributeValue + " " + operator + " " + target.attributeType;
		}
	
		ac.content += " --- Function Id :" + target.functionId;
		ac.attributeType = target.attributeType;
		ac.setAttributeValue(target.attributeValue);
		p.acs.add(ac);
		ac.policy = p;
		acMap.put(ac.index, ac);
	
		manageTarget(target, ac.index);
	
		return ac;
	}

	private void keepBackup() {
		if (backup == null) {
			backup = clone(this);
		}
	}

	Integer getIndexOfAttributeType(String attributeType) {
		return attributeTypeIndexMap.get(attributeType);
	}

	ApplicationConstraint addApplicationConstraint(PolicySet ps, Target target) {
		ApplicationConstraint ac = new ApplicationConstraint();
		ac.index = ApplicationConstraintIndex++;
		target.name = "ac" + ac.index;
		ac.content = target.attributeValue + " E " + target.attributeType + " --- Function Id :"
				+ target.functionId;
		ps.ac = ac;
		ac.policySet = ps;
		ac.attributeType = target.attributeType;
		ac.setAttributeValue(target.attributeValue);
		acMap.put(ac.index, ac);

		manageTarget(target, ac.index);

		return ac;
	}

	ApplicationConstraint addApplicationConstraint(Policy p, Target target) {
		return addApplicationConstraint(p, target, true);
	}

	private void manageTarget(Target target, int acIndex) {
		if (!attributeTypeIndexMap.keySet().contains(target.attributeType)) {
			attributeTypeIndexMap.put(target.attributeType, attributeTypeIndex++);

			// add to map first time
			attTypeAttValuesMap.put(target.attributeType, new HashSet<Pair<String, Integer>>());
		}

		attTypeAttValuesMap.get(target.attributeType).add(Pair.of(target.attributeValue, acIndex));
	}

	ApplicationConstraint addDecisionConstraint(Policy policy) {
		if (policy.targets.size() == 0) {
			ApplicationConstraint ac = new ApplicationConstraint();
			ac.index = ApplicationConstraintIndex++;
			ac.content = policy.effect;
			policy.acs.add(ac);
			ac.policy = policy;
			acMap.put(ac.index, ac);

			return ac;
		}
		return null;
	}

	Policy generateConstraintPolicy(PolicySet ps) {
		Policy pol = new Policy(this);
		Set<String> attributeTypesOfPolicySet = new HashSet<>();
		for (Policy policy : ps.policies) {
			attributeTypesOfPolicySet.addAll(policy.getAttributeTypeSet());
		}
		ApplicationConstraint ac = new ApplicationConstraint();
		ac.index = ApplicationConstraintIndex++;
		ac.content = ps.target.attributeType;
		ac.attributeType = ps.target.attributeType;
		ac.setAttributeValue(ps.target.attributeValue);
		pol.acs.add(ac);
		ac.policy = pol;
		acMap.put(ac.index, ac);

		for (String at : attributeTypesOfPolicySet) {
			ApplicationConstraint ac1 = new ApplicationConstraint();
			ac1.index = ApplicationConstraintIndex++;
			ac1.content = at;
			ac1.attributeType = at;
			pol.acs.add(ac1);
			ac1.policy = pol;
			acMap.put(ac1.index, ac1);
		}

		pol.ps = ps;
		ps.constraintPolicy = pol;

		return pol;
	}

	boolean hasAttributeType(String attributeType) {
		return attributeTypeIndexMap.keySet().contains(attributeType);
	}

	int numberOfAttributeTypes() {
		return attributeTypeIndexMap.keySet().size();
	}

	void printAllAttributeValuesByTypes() {
		System.out.println("\nMap\n");
		// Print map to test
		for (String k : attTypeAttValuesMap.keySet()) {
			System.out.println("AttributeType: " + k);
			for (Pair<String, Integer> pair : attTypeAttValuesMap.get(k)) {
				System.out.println("\t Key value : " + pair.a);
			}
		}
	}

	void rollback() {
		this.attributeTypeIndex = backup.attributeTypeIndex;
		this.ApplicationConstraintIndex = backup.ApplicationConstraintIndex;
		this.attTypeAttValuesMap = new LinkedHashMap<>(backup.attTypeAttValuesMap);
		this.attributeTypeIndexMap = new HashMap<>(backup.attributeTypeIndexMap);
		this.acMap = new HashMap<>(backup.acMap);
		this.policySetList = backup.policySetList;

		backup = null;
	}

	void commit() {
		this.backup = null;
	}

	DefaultHandler createParserHandler() {
		DefaultHandler handler = new DefaultHandler() {
			boolean isPolicySet = false;
			boolean isPolicy = false;
			boolean isAttValue = false;
			boolean isAttDesignator = false;
			boolean isRule = false;
			boolean isStartTarget = false;
			boolean isFirstApply = true;
			Target currentTarget = null;
			Policy currentPolicy = null;
			String currentFunctionId = null;
			PolicySet currentPolicySet = null;
	
			// This function runs when parser hit open tag e.g <PolicySet
			public void startElement(String uri, String localName, String qName, Attributes attributes)
					throws SAXException {
	
				if (qName.equalsIgnoreCase("PolicySet")) {
					if (ReadXMLFile.debug)
						System.out.println("Start Element :" + qName);
					isPolicySet = true;
				}
	
				if (qName.equalsIgnoreCase("Policy")) {
					if (ReadXMLFile.debug)
						System.out.println("Start Element :" + qName);
					isPolicy = true;
					// Create new policy
					currentPolicy = new Policy(PAP.this);
					currentPolicy.ps = currentPolicySet;
					currentPolicy.targets = new ArrayList<Target>();
					currentPolicySet.policies.add(currentPolicy);
				}
	
				if (qName.equalsIgnoreCase("Match")) {
					String functionId = attributes.getValue("MatchId");
					isStartTarget = true;
					if (functionId.lastIndexOf(":")>0)
						currentFunctionId = 
								functionId.substring(functionId.lastIndexOf(":") + 1, functionId.length());
					else
						currentFunctionId = functionId;

					// Create new Target
					currentTarget = new Target();

					if (currentPolicy == null) {
						currentPolicySet = new PolicySet(PAP.this);
						PAP.this.policySetList.add(currentPolicySet);
					}
	
				}
	
				if (qName.equalsIgnoreCase("Condition")) {
					isStartTarget = true;
					isFirstApply = false;
					// Create new Target
					currentTarget = new Target();
					
					if (currentPolicy == null) {
						currentPolicySet = new PolicySet(PAP.this);
						PAP.this.policySetList.add(currentPolicySet);
					}
				}
	
				if (qName.equalsIgnoreCase("Apply")) {
					if (!isFirstApply) {
						isFirstApply = true;
	
						String functionId = attributes.getValue("FunctionId");
	
						if (functionId.lastIndexOf(":") > 0)
							currentFunctionId = functionId.substring(functionId.lastIndexOf(":") + 1,
									functionId.length());
						else
							currentFunctionId = functionId;
					}
				}
	
				if (qName.equalsIgnoreCase("AttributeValue")) {
					if (ReadXMLFile.debug)
						System.out.println("Start Element :" + qName);
					if (isStartTarget)
						isAttValue = true;
				}
	
				if (qName.equalsIgnoreCase("AttributeDesignator")) {
					if (ReadXMLFile.debug)
						System.out.println("Start Element :" + qName);
					if (isStartTarget) {
						String attributeId = attributes.getValue("AttributeId");
						if (ReadXMLFile.debug)
							System.out.println("AttributeId : " + attributeId);
	
						// if (attributeId.lastIndexOf(":")>0)
						// currentTarget.attributeId =
						// attributeId.substring(attributeId.lastIndexOf(":") + 1,
						// attributeId.length());
						// else
						currentTarget.attributeType = attributeId;
					}
	
				}
	
				if (qName.equalsIgnoreCase("Rule")) {
					if (ReadXMLFile.debug)
						System.out.println("Start Element :" + qName);
					String effect = attributes.getValue("Effect");
					if (ReadXMLFile.debug)
						System.out.println("Effect : " + effect);
					isRule = true;
					currentPolicy.effect = effect;
				}
			}
	
			// This function runs when parser hit close tag e.g </PolicySet>
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (ReadXMLFile.debug && (isPolicySet || isPolicy || isAttValue || isAttDesignator || isRule))
					System.out.println("End Element :" + qName);
	
				if (isPolicy && qName.equalsIgnoreCase("Policy")) {
					currentPolicy = null;
				}
	
				if ((qName.equalsIgnoreCase("Condition") || qName.equalsIgnoreCase("Match")) && isStartTarget) {
					isStartTarget = false;
	
					if (currentFunctionId != null) {
						currentTarget.functionId = currentFunctionId;
						currentFunctionId = null;
					}
					// Special case for target in policy set
					if (currentPolicy != null)
						currentPolicy.targets.add(currentTarget);
					else {
						currentPolicySet.target = currentTarget;
					}
				}
	
				if (isPolicySet && qName.equalsIgnoreCase("PolicySet") && currentPolicySet != null) {
					currentPolicySet.constraintPolicy = PAP.this.generateConstraintPolicy(currentPolicySet);
					currentPolicySet = null;
				}
			}
	
			// This function runs to get data between open and close tag
			public void characters(char ch[], int start, int length) throws SAXException {
	
				if (isAttValue && isStartTarget) {
					if (ReadXMLFile.debug)
						System.out.println("Attribute Value Name : " + new String(ch, start, length));
					isAttValue = false;
					currentTarget.attributeValue = new String(ch, start, length);
				}
			}
		};
		return handler;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (PolicySet ps : policySetList) {
			sb.append(ps.toString());
			sb.append("\n");
		}

		return sb.toString();
	}
}
