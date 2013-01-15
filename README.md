# WOKManager (Ultra Alpha stage)

WOKManager is intended to be a simple worker manager. That is, you have lots of workers running in several machines/languages but you are blind because you don't know which one is running.

This is extremely early stages of a server that will allows you to track the execution of workers usually listening to queues.

## How data are stored

Now the data are store into simple clojure data structures (maps):

    (def workers (ref {"Importer01" {"worker" "Importer1d" "group" "product.importer"}}))

    (def message-stream (ref [{"worker"  "Importer1d"
	                           "group"   "product.importer"
	                           "event"   "started"
	                           "content" ""}
	                          {"worker"  "Importer1d"
	                           "group"   "product.importer"
	                           "event"   "processing"
	                           "content" "Processing SKU 2522"}
	                          {"worker"  "Importer1d"
	                           "group"   "product.importer"
	                           "event"   "failure"
	                           "content" "Failed while processing SKU 2522"}]))

At some point in time it will use a persistent storage.

## Usage

It is simple to register a worker with (so far) two events: `starte` and `stopped` using the REST API:

    curl -H "Content-Type: application/json" -v -X POST -d '{"worker" : "ImporterXX",
	                       "group" : "product.importer",
	                       "event" :  "started",
	                       "content" : ""}' http://localhost:5000/api/message

    curl -H "Content-Type: application/json" -v -X POST -d '{"worker" : "ImporterXX",
                          "group" : "product.importer",
						  "event" :  "stopped",
						  "content" : ""}' http://localhost:5000/api/message

You could put some code during the start steps of your worker to register itself agains WOKManager. But emitting a `stopped` event will remove the worker from the WOKManager workers list. This is very primitive yet.

There is a simple python script to allows you query which worker is running:

    ./wokmanager.py --worker=* #to retrieve all the workers running
    ./wokmanager.py --worker=Importer1d #to retrieve something like
    Requiring Workers that match {'worker': 'Importer1d'} condition
	1 Workers running with such a criteria:
	Id: Importer1d, group: product.importer
	

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
