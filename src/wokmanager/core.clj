(ns wokmanager.core
  (:use [ring.adapter.jetty :only [run-jetty]]
	    [compojure.core])
  (:require [compojure.route :as route]
	        [compojure.handler :as handler]
			[ring.middleware.stacktrace :as stack]
			[ring.middleware.reload :as reload]
			[clojure.data.json :as json]
			[clojure.java.io :as io]
			[lamina.core :as l]
			[wokmanager.utils :as u]))


(def worker-domains (ref {"product" 
						{"importer" ["Importer01"]}
					 "price" {"promotion" ["Promo01" "Promo02"]}}))

(def workers (ref {"Importer01" {"id" "Importer01" "group" "product.importer"}}))

(def messages (ref [{"Importer01" ""}]))

(def errors (ref [{"Importer01" {"message" "Error processing Procut21" "action" "logged"}}]))

(defmulti register-state (fn [_ msg] (get msg "event")))

(defmethod register-state "started"
	[request msg]
	(dosync 
		(if-let [worker (get @workers (get msg "id"))]
			(alter workers update-in 
								[(get msg "id")]
								(fn [w]
									{"id" (get msg "id") 
				                     "group" (get msg "group")}))
			(alter workers merge
								{(get msg "id")
								{"id" (get msg "id")
				                 "group" (get msg "group")}})))
	(println @workers)
	(u/json-> 200 {:registered (get msg "event")
			         :at "now"}))

(defmethod register-state "stopped"
	[request msg]
	(dosync 
		(if-let [worker (get @workers (get msg "id"))]
			(do 
				(alter workers dissoc (get msg "id"))
				(u/json-> 200 {:registered (get msg "event")
					           :at "now"}))
			(u/json-> 404 {"error" "no such worker registered"}))))
			
(defn query-state [request query]
	(letfn [(by-id [q] 
				(if (= "*" (get q "id"))
					identity
					(fn [w] (= (get q "id") (get w "id")))))]
		(u/json-> 200 (filter (by-id query) (vals @workers)))))

(defroutes app
  (GET "/worker/:worker/status" [worker] (str "The worker " worker " is running fine"))
  (POST "/worker/state" [] (u/accepting-json register-state))
  (GET "/worker/state" [] (u/accepting-json query-state)))

(defn -main [port]
  (run-jetty (-> app
                 (handler/api)
                 (stack/wrap-stacktrace)
                 (reload/wrap-reload)) 
             {:port (Integer. port)}))