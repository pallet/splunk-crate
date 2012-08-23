(ns pallet.crate.splunk-test
  (:use pallet.crate.splunk
        clojure.test)
  (:require
   [pallet.build-actions :as build-actions]
   [pallet.test-utils :as test-utils]))

(deftest invoke-test
  (is (build-actions/build-actions
       {:server {:node (test-utils/make-node "tag" :id "id")}}
       (splunk)
       (configure))))

(deftest rpmfile-test
	(is (= "splunk-4.3.3-128297-linux-2.6-x86_64.rpm" 
		(rpmfile {:server {:node (test-utils/make-node "tag" :id "id")}}
			 "4.3.3" false))))

(deftest rpmfile-forwarder-test
	(is (= "splunkforwarder-4.3.3-128297-linux-2.6-x86_64.rpm" 
		(rpmfile {:server {:node (test-utils/make-node "tag" :id "id")}}
			 "4.3.3" true))))

(deftest url-segment-test
	(is (= "universalforwarder"
		(url-segment true)))
	(is (= "splunk"
		(url-segment false))))

(deftest get-splunk-path-test
	(is (= "/opt/splunk/bin/splunk"
		(get-splunk-path false))
	(is (= "/opt/splunkforwarder/bin/splunk"
		(get-splunk-path true)))))