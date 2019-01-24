(ns timer.alarm-test
  (:require [timer.alarm :as alarm]
            [clojure.test :refer :all]))

(deftest create-record
  (testing "An alarm should be created"
    (let [a (alarm/init {})]
      (is (= (:status @a) :stopped)))))
