#! /usr/bin/python
import httplib2 as http
import json
from optparse import OptionParser


server = 'http://localhost:5000'
headers = {'Content-type' : 'application/json'}

client = http.Http()

def query(q={'id' : '*'}):
	print ('Requiring Workers that match %s condition' % str(q))
	response, content = client.request(server + '/api/worker', method='GET', body=json.dumps(q))
	return json.loads(content)


if __name__ == "__main__":
	parser = OptionParser()
	parser.add_option("-w", "--worker", dest="worker", help="The worker id. * for all.")
	(options, args) = parser.parse_args()
	res = (query({'worker' : options.worker}))
	print "%d Workers running with such a criteria:" % len(res)
	for w in res:
		print "Id: %s, group: %s" % (w['worker'], w['group'])
	

		
	




