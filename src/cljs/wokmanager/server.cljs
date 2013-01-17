(ns wokmanager.server
  (:require [goog.net.XhrIo :as xhr]))

(def api "http://localhost:5000/api/message")
	
(defn load-messages [from callback]
  (.send goog.net.XhrIo api
    (fn [data] (callback 
	             (js->clj 
		           (.getResponseJson (.-target data)) :keywordize-keys true)))))

	