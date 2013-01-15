(defproject wokmanager "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :min-lein-version "2.0.0"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [ring/ring-core "1.1.6"]
				 [ring/ring-jetty-adapter "1.1.6"]
				 [ring/ring-devel "1.1.6"]
                 [compojure "1.1.4"]
                 [org.clojure/data.json "0.2.0"]
				 [lamina "0.4.1"]
  				 [hiccup "1.0.2"]
				 [clj-time "0.4.4"]]
  :profiles {:dev {:dependencies  [[ring-mock "0.1.3"]]}}
  :plugins [[lein-ring "0.7.5"]])

