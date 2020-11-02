#!/usr/bin/python

import sys
from random import randrange
import requests
import json
import codecs
from pymongo import MongoClient 

def main(argv):
	print 'Format: python intentscript.py <number of intents> <host range begining> <host range end>'
	print 'Number of arguments:', len(sys.argv), 'arguments.'
	print 'Number of intents:', str(sys.argv[1])
	print 'Host range beginning :', str(sys.argv[2])
	print 'Host range ending: :', str(sys.argv[3])
	num = sys.argv[1]
	range1 = sys.argv[2]
	range2 = sys.argv[3]
	conn = MongoClient()  
	db = conn.database  
	IntentStore = db.IntentStore

	generateIntents(num, range1, range2, IntentStore)

def generateIntents(num, range1, range2, IntentStore):
	for i in range(int(num)):
		src = randrange(int(range1),int(range2))
		dst = randrange(int(range1),int(range2))

		if (src < 10 and dst < 10):
			src_full = '00:00:00:00:00:0{}/None'.format(src)
			dst_full = '00:00:00:00:00:0{}/None'.format(dst)
		elif (src < 10 and dst >= 10):
			src_full = '00:00:00:00:00:0{}/None'.format(src)
			dst_full = '00:00:00:00:00:{}/None'.format(dst)
		elif (dst < 10 and src >= 10):
			dst_full = '00:00:00:00:00:0{}/None'.format(dst)
			src_full = '00:00:00:00:00:{}/None'.format(src)
		else : 
			src_full = '00:00:00:00:00:{}/None'.format(src)
			dst_full = '00:00:00:00:00:{}/None'.format(dst)
        
		intent = 'add-host-intent {} {}'.format(src_full,dst_full)
		IntentStore.insert_one({'type':'host-intent', 'src':src_full, 'dst':dst_full})

		#REST API
		URL = "http://localhost:8181/onos/v1/intents"
		AUTH = ('onos','rocks')
		HEADERS = {'content-type': 'application/json', 'Access':'application/json'}
		r = requests.get(url = URL, auth = AUTH)
		data_out = r.json()
		intents = data_out["intents"]
		print "------------------"
		print intents
		print "------------------"
		print r.encoding
		print "------------------"
		print r.content
		print "------------------"
		print r.text
		print "------------------"
		with codecs.open('../../example4.json', "r", encoding="utf-8") as newrule:
			jsonfile = json.load(newrule)
			jsonfile['one'] = src_full
			jsonfile['two'] = dst_full
		print newrule.encoding
		with open("../../example4.json", "w") as newrule:
			json.dump(jsonfile, newrule)
		cursor = IntentStore.find()
		for record in cursor:
			print(record)
		r = requests.post(url = URL, auth = AUTH, data = json.dumps(jsonfile), headers = HEADERS) 
		print r.content

if __name__ == "__main__":
	 main(sys.argv[1:])
