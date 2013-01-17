(ns wokmanager.view
  (:require [hiccup
	         [element :refer [javascript-tag]]
             [page :refer [include-js]]
             [page :refer [html5]]]))


(defn layout [& content]
  (html5
    [:head
      [:title "WOKManager Dashboard"]
      [:style {:type "text/css"}
	"
	/*
	 You are allowed to do whatever you want with this layout. 
	 Though I would be pleased if you placed a link on your site to csseasy.com or to profit42.com 
	 (best \"blog about hacking\" ever). Donations are also welcome: paypal@profit42.com 
	 (or follow the donation button on csseasy.com)
	*/

	body{ 
	background-color:#dc8;
	font-size:16px; 
	margin:0; 
	padding:0;
	font-family: verdana;
	}

	tr:nth-child(2n+1) {
	  background-color: #99ff99;
	}

	#header{ 
	background-color:#333;
	height:150px;
	}

	#top{ 
	width:100%;
	background-color:#dc8; 
	height:50px;
	}

	#center { 
	background-color:#eec; 
	min-height:600px; /* for modern browsers */
	height:auto !important; /* for modern browsers */
	height:600px; /* for IE5.x and IE6 */
	}

	#footer { 
	clear:both;
	background-color:#333;
	height:100px;
	}
	
	#failure {
	background-color:red;
	}
    "]]
	[:body
	  [:div {:id "header"}
        "WOKManager Dashboard"]
        [:div {:id "center"}
          content]
        [:div {:id "footer"}]]
      (list (javascript-tag "var CLOSURE_NO_DEPS = true;")
	        (include-js "js/main.js"))))
	

(defn dashboard-index [request workers]
	{:status 200 
	 :body
		(layout [:table 
                  [:thead 
                      [:th "Worker worker"] 
                      [:th "Group"]]
					   (map (fn [e] 
					  	      [:tr 
					 	        [:td (key e)]
					            [:td (get-in @workers [(key e) "group"])]]) @workers)]
				 [:table#tail
				  [:thead [:th "Messages"]] 
				  [:tbody]])})

