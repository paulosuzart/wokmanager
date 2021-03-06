(ns wokmanager.utils
	(:require [clojure.data.json :as json]
			  [clojure.java.io :as io]))
			
(def handled-events ["started" "stopped" "processing" "failure"])

(defn accepting-json [handler] 
	(fn [request]
		(handler request 
				 (json/read (io/reader (:body request))))))
				
(defn json-> [status m]
	{:status status
     :body (json/write-str m)})				