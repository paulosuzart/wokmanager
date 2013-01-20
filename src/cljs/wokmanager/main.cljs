(ns wokmanager.main
	(:use [domina :only [by-id append! nodes html]]
		  [domina.xpath :only [xpath]]
		  [domina.css :only [sel]]
		  [wokmanager.server :only [load-messages]])
	(:require [hiccups.runtime :as hiccupsrt])
	(:require-macros [hiccups.core :as hiccups]))

(defn row-c [e]
	(get
		{"started"    "success"
		"stopped"    "warning"
		"processing" "info"
		"failure"    "error"}
		e))

(defn message-entry [e]
	(let [wevent (:event e)
		  wworker (:worker e)
		  wat (:at e)
		  message (format "[%s] - %s. %s" wat wevent wworker)]
	  [:tr {:class (row-c wevent)} [:td message]]))

(defn display-messages [data]
  (let [tail (xpath "//table[@id='tail']/tbody")]
	(.log js/console (str data))
 	(append! tail (hiccups/html (map #(message-entry %) data)))))

;; TODO: Store the last msg `at` attribute so the next request can
;; pass it as argument.
(load-messages nil display-messages)

