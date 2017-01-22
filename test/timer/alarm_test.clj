(ns timer.alarm-test
  (:require [timer.alarm :as alarm]
            [clojure.test :refer :all]))

(deftest create-record
  (testing "An alarm should be created"
    (let [a (alarm/atom-alarm {})]
      (is (= (:status @a) :stopped)))))
