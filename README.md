# wokmanager

WOKManager is intended to be a simple worker manager. That is, you have lots of workers running in several machines/languages but you are blind because you don't know which one is running.

This is extremely early stages of a server that will allows you to track the execution of workers usually listening to queues.

## How data is stored

Now the data are store into simple clojure data structures (maps):

    (def worker-domains (ref {"product" 
	 					{"importer" ["Importer01"]}
	  				     "price" {"promotion" ["Promo01" "Promo02"]}}))
	
    (def workers (ref {"Importer01" {"id" "Importer01" "group" "product.importer"}}))

    (def messages (ref [{"Importer01" ""}]))

    (def errors (ref [{"Importer01" {"message" "Error processing Procut21" "action" "logged"}}]))

At some point in time it will use a persistent storage.

## Usage

It is simple to register a worker with (so far) two events: `starte` and `stopped` using the REST API:

    curl -H "Content-Type: application/json" -v -X POST -d '{"id" : "Importer1d", "group" : "product.importer", "event" : "started", "at" : "now"}' http://localhost:5000/worker/state?api-key=231cooai1d

    curl -H "Content-Type: application/json" -v -X POST -d '{"id" : "Importer1d", "group" : "product.importer", "event" : "stopped", "at" : "now"}' http://localhost:5000/worker/state?api-key=231cooai1d

You could put some code during the start steps of your worker to register itself agains WOKManager. But emitting a `stopped` event will remove the worker from the WOKManager workers list. This is very primitive yet.

There is a simple python script to allows you query which worker is running:

    ./wokmanager.py --id * #to retrieve all the workers running
    ./wokmanager.py --id Importer01 #to retrieve something like
    Requiring Workers that match {'id': 'Importer01'} condition
	1 Workers running with such a criteria:
	Id: Importer01, group: product.importer
	

## License

Copyright © 2013 FIXME

Distributed under the Eclipse Public License, the same as Clojure.
