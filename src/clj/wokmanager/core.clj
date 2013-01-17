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
            [wokmanager.view :as v]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

(def stream-channel (l/channel))

(def workers (ref {"Importer1d" {"worker" "Importer1d" "group" "product.importer"}}))

(def message-stream (ref [{"worker"  "Importer1d"
	                       "group"   "product.importer"
	                       "event"   "started"
	                       "content" ""
	                       "at"      "2013-01-15T23:40:03.452Z"}
	                      {"worker"  "Importer1d"
	                       "group"   "product.importer"
	                       "event"   "processing"
	                       "content" "Processing SKU 2522"
	                       "at"      "2013-01-15T23:41:03.452Z"}
	                      {"worker"  "Importer1d"
	                        "group"   "product.importer"
	                        "event"   "failure"
	                        "content" "Failed while processing SKU 2522"
	                        "at"      "2013-01-15T23:41:03.452Z"}]))

(defmulti register-state (fn [msg] (get msg "event")))

(defmethod register-state "started"
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
    [msg]
    (println "Registering Started Event")
    (dosync 
          (and (get @workers (get msg "worker"))
               (alter workers dissoc (get msg "worker"))))
    (println "ALL WORKERS" @workers))
            
(defn register-message 
    "Registers a message in the `message-stream` vector"
    [msg]
      (dosync 
        (alter message-stream (fn [ms] (cons msg ms))))
      (println "MESSAGE TAIL " @message-stream))

(defn process-message 
  "Adds the `at` attribute to the message and enqueues it."
 [request msg]
   (l/enqueue stream-channel (merge msg {"at" (tc/to-string (t/now))}))
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


(defroutes app
	(GET "/" [] (fn [r] (v/dashboard-index r workers)))
    (context "/api" []
      (POST "/message" [] (u/accepting-json process-message))
      (GET  "/message" [] (u/json-> 200 @message-stream))
      (GET  "/worker" [] (u/accepting-json query-state))))

(defn -main [port]
  (run-jetty (-> app
                 (handler/api)
                 (wrap-file "public")
                 (stack/wrap-stacktrace)
                 (reload/wrap-reload)) 
             {:port (Integer. port)}))