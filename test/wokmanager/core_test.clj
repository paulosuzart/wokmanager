(ns wokmanager.core-test
  (:use clojure.test
        wokmanager.core
        ring.mock.request))

(deftest test-process-message
  (testing "POST a msg"
	(let [msg {"worker" "ImporterXX",
		       "group" "product.importer",
		       "event"  "started",
		        "content" ""}
		 req (request :post "/api/message")
		 message-count (count @message-stream)]
		(is (= 200 (:status (process-message req msg))))
		(is (= (inc message-count) (count @message-stream)))
		(println "####" @message-stream)
        (is (get @workers "ImporterXX") "The wokers ref should contain the posted worker."))))
