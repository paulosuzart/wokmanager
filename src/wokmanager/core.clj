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


(def worker-domains (ref {"product" 
                        {"importer" ["Importer01"]}
                     "price" {"promotion" ["Promo01" "Promo02"]}}))

(def workers (ref {"Importer01" {"id" "Importer01" "group" "product.importer"}}))

(def messages (ref {"Importer01" 
					    [{"id"    "Importer1d" 
	                      "group" "product.importer" 
	                      "event" "started"
	                      "at"    "now"}]}))

(def all-messages (ref [{"id"    "Importer1d" 
	                     "group" "product.importer" 
	                     "event" "started"
	                     "at"    "now"}]))

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
        (if (get @workers (get msg "id"))
            (do 
                (alter workers dissoc (get msg "id"))
                (u/json-> 200 {:registered (get msg "event")
                               :at "now"}))
            (u/json-> 404 {"error" "no such worker registered"}))))
            
(defn register-message [request msg]
	(let [id (get msg "id")]
      (dosync 
        (or 
          (and (get @messages id)
               (alter messages update-in [id] (fn [ms] (cons msg ms))))
          (alter messages merge {id [msg]}))
        (alter all-messages #(cons msg %)))))

(defn process-state [request msg]
  (do 
	(register-message request msg)
	(register-state request msg)))


(defn query-state [request query]
    (letfn [(by-id [q] 
                (if (= "*" (get q "id"))
                    identity
                    (fn [w] (= (get q "id") (get w "id")))))]
        (u/json-> 200 (filter (by-id query) (vals @workers)))))

(defn dashboard-index [request]
	{:status 200 
	 :body
		(v/layout [:table 
                  [:thead 
                    [:tr
                      [:th "Worker ID"] 
                      [:th "State" "At"]
                      [:th "Group"]]
					   (map (fn [e] 
					  	      [:tr 
					 	        [:td (key e)] 
						        [:td (get (first (val e)) "event")]
						        [:td (get-in @workers [(key e) "group"])]])  @messages)]]
				  [:table 
				    [:thead 
				      [:tr [:th "Message tail"]]
				        (map (fn [e]
					           [:tr 
					             [:td (json/write-str e)]]) @all-messages)]])})

(defroutes app
	(GET "/" [] dashboard-index)
    (context "/worker" []
      (POST "/state" [] (u/accepting-json process-state))
      (GET "/state" [] (u/accepting-json query-state))))

(defn -main [port]
  (run-jetty (-> app
                 (handler/api)
                 (wrap-file "public")
                 (stack/wrap-stacktrace)
                 (reload/wrap-reload)) 
             {:port (Integer. port)}))