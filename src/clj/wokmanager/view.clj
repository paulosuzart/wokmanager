(ns wokmanager.view
  (:require [hiccup
	         [element :refer [javascript-tag ]]
             [page :refer [include-js include-css]]
             [page :refer [html5]]]
            [wokmanager.utils :as u]))

(def elem-filter-by-event
	(map (fn [e]
		     [:div [:lable.checkbox [:input {:type "checkbox" :value e} e]]])
		  u/handled-events))


(defn layout [& content]
  (html5
    [:head
      [:title "WOKManager Dashboard"]
      [:style {:type "text/css"}]
      (include-css "css/bootstrap-responsive.min.css" "css/bootstrap.min.css")]
      (list (javascript-tag "var CLOSURE_NO_DEPS = true;")
	        (include-js "js/main.js"))
	[:body
	  [:div.container-fluid
        [:div.row-fluid
          [:div.span2
		  [:form
		    [:label.control-label {:for "filter-group"} "Filter by Worker Group"
		    [:select#filter-group {:multiple "multiple" :style "width: 150px;"}
		      [:option 1]
		      [:option 2]
		      [:option 3]
		      [:option 4]
		      [:option 5]]]
		    elem-filter-by-event]]
          [:div.span10 content]]]]))
	
(defn dashboard-index [request workers]
	{:status 200 
	 :body
		(layout [:h3 "List of Workers"]
			    [:table.table.table-striped
                  [:thead 
                      [:th "Worker worker"] 
                      [:th "Group"]]
					   (map (fn [e] 
					  	      [:tr 
					 	        [:td (key e)]
					            [:td (get-in @workers [(key e) "group"])]]) @workers)]
				[:h3 "Message stream"]
				[:table#tail.table.table-striped
				   [:thead [:th "Messages"]] 
				   [:tbody]])})

