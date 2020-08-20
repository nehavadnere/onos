/*
 * Copyright 2014 Open Networking Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onosproject.policychecker;

import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.onlab.packet.Ethernet;
import org.onosproject.core.ApplicationId;
import org.onosproject.core.CoreService;
import org.onosproject.net.Host;
import org.onosproject.net.HostId;
import org.onosproject.net.PortNumber;
import org.onosproject.net.flow.DefaultTrafficSelector;
import org.onosproject.net.flow.DefaultTrafficTreatment;
import org.onosproject.net.flow.FlowRuleService;
import org.onosproject.net.flow.TrafficSelector;
import org.onosproject.net.flow.TrafficTreatment;
import org.onosproject.net.host.HostService;
import org.onosproject.net.intent.HostToHostIntent;
import org.onosproject.net.intent.IntentService;
import org.onosproject.net.intent.IntentState;
import org.onosproject.net.intent.Key;
import org.onosproject.net.packet.DefaultOutboundPacket;
import org.onosproject.net.packet.InboundPacket;
import org.onosproject.net.packet.OutboundPacket;
import org.onosproject.net.packet.PacketContext;
import org.onosproject.net.packet.PacketPriority;
import org.onosproject.net.packet.PacketProcessor;
import org.onosproject.net.packet.PacketService;
import org.onosproject.net.topology.TopologyService;
import org.slf4j.Logger;

import java.util.*;
import java.util.EnumSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * WORK-IN-PROGRESS: Policy Checker Handler for verifying flowrules.
 */
@Component(immediate = true)
public class PolicyCheckerHandler {

    private final Logger log = getLogger(getClass());
    //private static final Logger log = getLogger(PolicyCheckerHandler.class);

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected CoreService coreService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected TopologyService topologyService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected PacketService packetService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected IntentService intentService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected HostService hostService;

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    protected FlowRuleService flowRuleService;


    private ApplicationId appId;
    List<String> rules = new ArrayList<>();
    private static final String FILE_NAME = "/src/main/resource/org/onosproject/policychecker/resource/rules.txt";
    
	public PolicyCheckerHandler() {
    }

    public PolicyCheckerHandler(ApplicationId appId) {
      this.appId = appId;
    }

    private void loadFile() {
        log.info("load file module ");
        String relativelyPath = System.getProperty("user.dir");
        File ruleFile = new File(relativelyPath + FILE_NAME);
        BufferedReader br = null;
        try {
            FileReader in = new FileReader(ruleFile);
            br = new BufferedReader(in);
            int i = 0;
            String icmd = "";
            while ((icmd = br.readLine()) != null) {
                rules.add(icmd);
            }
        } catch (IOException e) {
            log.info("file does not exist.");
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.info("nothing");
                }
            }
        }
        log.info("rules = " + rules);
		intentMapper(rules);
    }

	private void intentMapper(List<String> rules) {
		Iterator<String> iterator = rules.iterator();
        while(iterator.hasNext()) {
            String icmd = iterator.next();
            String[] str = icmd.split(" ");
			if (str[0].equals("add-host-intent"))
				createHostIntent(str[1], str[2]);
			else if (str[0].equals("add-point-intent"))
				log.info("point intent");
			else
				log.error("intent type unknown.");
            }
        } //end of intentMapper

	private void createHostIntent(String one, String two) {
		//IntentService service = get(IntentService.class);
		
		String host1 = one.split("/")[0];
		String host2 = two.split("/")[0];
		
        HostId srcId = HostId.hostId(host1);
        HostId dstId = HostId.hostId(host2);

		//TrafficSelector selector = buildTrafficSelector();
        //TrafficTreatment treatment = buildTrafficTreatment();
        //List<Constraint> constraints = buildConstraints();

		TrafficSelector selector = DefaultTrafficSelector.emptySelector();
	    TrafficTreatment treatment = DefaultTrafficTreatment.emptyTreatment();


        Key key;
        if (srcId.toString().compareTo(dstId.toString()) < 0) {
            key = Key.of(srcId.toString() + dstId.toString(), this.appId);
        } else {
            key = Key.of(dstId.toString() + srcId.toString(), this.appId);
        }

        HostToHostIntent intent = (HostToHostIntent) intentService.getIntent(key);
		HostToHostIntent hostIntent = HostToHostIntent.builder()
			.appId(appId)
			.key(key)
			.one(srcId)
			.two(dstId)
			.selector(selector)
			.treatment(treatment)
			.build();

        intentService.submit(hostIntent);


        log.info("Host to Host intent submitted:\n%s", intent.toString());

	}// end of create_intent


    @Activate
    public void activate() {
        appId = coreService.registerApplication("org.onosproject.policychecker");
        PolicyCheckerHandler policycheckerhandler = new PolicyCheckerHandler(appId);
		log.info("NEHA: activated");
		loadFile();

        //packetService.addProcessor(processor, PacketProcessor.director(2));

        //TrafficSelector.Builder selector = DefaultTrafficSelector.builder();
        //selector.matchEthType(Ethernet.TYPE_IPV4);
        //packetService.requestPackets(selector.build(), PacketPriority.REACTIVE, appId);

        log.info("Started");
    }

    @Deactivate
    public void deactivate() {
        //packetService.removeProcessor(processor);
        //processor = null;
        log.info("Stopped");
    
}
}
