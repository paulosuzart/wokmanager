(ns wokmanager.core
  (:use [ring.adapter.jetty :only [run-jetty]]
        [compojure.core]
        [ring.middleware.file])
  (:require [compojure.route :as route]
            [compojure.handler :as handler]
            [ring.middleware.stacktrace :as stack]
            [ring.middleware.reload :as reload]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [lamina.core :as l]
            [wokmanager.utils :as u]
            [wokmanager.view :as v]))

(def stream-channel (l/channel))

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

(defmulti register-state (fn [msg] (get msg "event")))

(defmethod register-state "started"
    "Registers a Worker in the `workers` ref if the
     message event is `started`"
    [msg]
    (println "Registering Started Event")
    (dosync 
        (or 
	      (and 
		    (get @workers (get msg "worker"))
            (alter workers update-in 
                                [(get msg "worker")]
                                (fn [w]
                                    {"worker" (get msg "worker") 
                                     "group" (get msg "group")})))
          (alter workers merge
                             {(get msg "worker")
                                {"worker" (get msg "worker")
                                 "group" (get msg "group")}})))
    (println "ALL WORKERS " @workers))

(defmethod register-state "stopped"
    "Registers a Worker in the `workers` ref if the
     message event is `stopped`"
    [msg]
    (println "Registering Started Event")
    (dosync 
          (and (get @workers (get msg "worker"))
               (alter workers dissoc (get msg "worker"))))
    (println "ALL WORKERS" @workers))
            
(defn register-message 
    "Registers a message in the `message-stream` vector"
    [msg]
	(let [worker (get msg "worker")]
      (dosync 
        (or 
          (and (get @message-stream worker)
               (alter message-stream update-in [worker] (fn [ms] (cons msg ms))))
          (alter message-stream merge {worker [msg]})))
      (println "MESSAGE TAIL " @message-stream)))

(defn process-message [request msg]
  (l/enqueue stream-channel msg)
  {:status 200})
  
(l/receive-all stream-channel #(register-message %))
(l/receive-all stream-channel #(and (#{"started" "stopped"} (get %1 "event")) (register-state %1)))


(defn query-state
    "Base on the `query` tries to filter the `workers` ref and
    returns a json with the filtered workers"
    [request query]
    (letfn [(by-worker [q] 
                (if (= "*" (get q "worker"))
                    identity
                    (fn [w] (= (get q "worker") (get w "worker")))))]
        (u/json-> 200 (filter (by-worker query) (vals @workers)))))

(defn dashboard-index [request]
	{:status 200 
	 :body
		(v/layout [:table 
                  [:thead 
                    [:tr
                      [:th "Worker worker"] 
                      [:th "Group"]]
					   (map (fn [e] 
					  	      [:tr 
					 	        [:td (key e)]
					            [:td (get-in @workers [(key e) "group"])]]) @workers)]])})


(defroutes app
	(GET "/" [] dashboard-index)
    (context "/api" []
      (POST "/message" [] (u/accepting-json process-message))
      (GET "/worker" [] (u/accepting-json query-state))))

(defn -main [port]
  (run-jetty (-> app
                 (handler/api)
                 (wrap-file "public")
                 (stack/wrap-stacktrace)
                 (reload/wrap-reload)) 
             {:port (Integer. port)}))