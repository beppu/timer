(ns timer.app-test
  (:require [timer.app :as app]
            [clojure.test :refer :all]))

(deftest create-record
  (testing "An app should be created."
    (let [a (app/init [])]
      (is (= (:timers @a) [])))))
