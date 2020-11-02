package org.onosproject.net.intent.impl.installer;

import com.google.common.collect.Lists;
import org.onlab.util.Tools;
import org.onosproject.cfg.ComponentConfigService;
import org.onosproject.core.DefaultApplicationId;
import org.onosproject.net.DeviceId;
import org.onosproject.net.Link;
import org.onosproject.net.NetworkResource;
import org.onosproject.net.flow.DefaultFlowRule;
import org.onosproject.net.flow.FlowRule;
import org.onosproject.net.flow.FlowRuleOperations;
import org.onosproject.net.flow.FlowRuleOperationsContext;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.intent.FlowRuleIntent;
import org.onosproject.net.intent.Intent;
import org.onosproject.net.intent.IntentData;
import org.onosproject.net.intent.IntentExtensionService;
import org.onosproject.net.intent.IntentInstallCoordinator;
import org.onosproject.net.intent.IntentInstaller;
import org.onosproject.net.intent.IntentOperationContext;
import org.onosproject.net.intent.IntentStore;
import org.onosproject.net.intent.ObjectiveTrackerService;
import org.onosproject.net.intent.impl.IntentManager;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import java.util.List;

public class PolicyChecker {

	public List<FlowRule> flowRulesToInstall;

	public PolicyChecker() {
	}

	//public boolean policyCheckerHandler(List<FLowRule> flowRulesToInstall) {
		//code here
	//	boolean isConflict = false;
	//	return isconflict;
	//	return false;
	//}
}
