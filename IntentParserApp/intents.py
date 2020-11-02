#!/usr/bin/python

'''
 Intent Generator
 Generate intents from mission requirements and required network topology
'''

import sys
from random import randrange
import requests
import json
import codecs

import time

def main(argv):
	print 'Format: python intents.py <path to network.json> <path to mission.json>'
	print 'Number of arguments:', len(sys.argv), 'arguments.'
	network_file = sys.argv[1]
	mission_file = sys.argv[2]
	intent_list = []
	populateData(network_file, mission_file, intent_list)

'''
Get the network topology and mission data from the ws
'''

def populateData(network_file, mission_file, intent_list):
	dataset = []
	#Get data from network and mission
	with codecs.open(network_file, "r", encoding="utf-8") as network:
		nw = json.load(network)
		n_loki = nw["$loki"]
		for hosts in nw["hosts"]:
			host = hosts.get("display_name")
			id_nw = hosts.get("id")
	#network.close()
		
	with codecs.open(mission_file, "r", encoding="utf-8") as mission:
		mi = json.load(mission)
		m_loki = mi["$loki"]
		for req in mi["missionRequirements"]:
			mi_src = req.get("src")
			mi_id = req.get("id")
			mi_dst = req.get("dst")		

			# Get the network info of the specifed hosts in mission requirements	
			mi_src_short = mi_src.rpartition('.')[2]
			mi_dst_short = mi_dst.rpartition('.')[2]
			for hosts in nw["hosts"]:
				host = hosts.get("display_name")
				if (mi_src_short == host):
					src_host_id = hosts.get("id")
				if (mi_dst_short == host):
					dst_host_id = hosts.get("id")
						
			print(mi_src_short,src_host_id,mi_dst_short,dst_host_id,mi_id)
			data = {
			'src_host_id' : src_host_id,
			'dst_host_id' : dst_host_id,
			'mi_id' : mi_id,
			'nw_loki_id' : n_loki,
			'mi_loki_id' : m_loki}
			dataset.append(data)
			intent_list = generateIntent(src_host_id,dst_host_id,mi_id,n_loki,intent_list)

	intent_json = {
	'intents' : intent_list,
	'network' : n_loki,
	'mission' : m_loki
	} 
	print intent_json
	with codecs.open("resources/out.json", "w") as file1:
		json.dump(intent_json,file1)
	file1.close()
		
	URL_WS="http://localhost:5000/api/intents"
	AUTH = ('onos','rocks')
	HEADERS = {'content-type': 'application/json', 'Access':'application/json'}
	r = requests.post(url = URL_WS, data = json.dumps(intent_json), headers = HEADERS)
	for items in intent_list:
		print items["id"]

'''
Generate the intents from mission file and submit to ONOS. 
Input parameters needed to generate and submit intents are gathered from mission file.
Unique ID 'mi_id' is asigned to each intent respose which is associated with the input mission id from mission.json, network database id i.e. loki from network.json and intent id from ONOS's intent subsystem.

src_host_id : ONOS ID of source host
dst_host_id : ONOS ID of destination host
loki        : Network dataset id
mi_id		: mapping to mission ID to ONOS intent ID
'''
def generateIntent(src_host_id,dst_host_id,mi_id,n_loki,intent_list):
	#REST API
	URL = "http://localhost:8181/onos/v1/intents"
	AUTH = ('onos','rocks')
	HEADERS = {'content-type': 'application/json', 'Access':'application/json'}
	r = requests.get(url = URL, auth = AUTH)
	data_out = r.json()
	intents = data_out["intents"]

	with codecs.open('resources/simple_template.json', "r", encoding="utf-8") as newrule:
		jsonfile = json.load(newrule)
		jsonfile['one'] = src_host_id
		jsonfile['two'] = dst_host_id
	with open("resources/simple_template.json", "w") as newrule:
		json.dump(jsonfile, newrule)
	r = requests.post(url = URL, auth = AUTH, data = json.dumps(jsonfile), headers = HEADERS)
	LOCATION = r.headers["Location"]
	
	id1 = LOCATION.rpartition("/")[2]
	id1_hex = hex(int(id1))
	URL_flows = "http://localhost:8181/onos/v1/intents/relatedflows/org.onosproject.cli/{}".format(id1_hex)
	time.sleep(1)
	r = requests.get(url = URL_flows, auth = AUTH)
	data_out = r.json()
	
        data_out["mi_id"] = "md{}_{}_i{}".format(n_loki,mi_id,id1_hex[2:])
	with open("resources/out.json", "w") as newrule:
		json.dump(data_out, newrule)

        intent_list.append(data_out)
        newrule.close()
        return intent_list
	
if __name__ == "__main__":
	 main(sys.argv[1:])
