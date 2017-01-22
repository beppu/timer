(ns timer.core
  (:gen-class)
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.core.async :as a
             :refer [>! <! >!! <!! go go-loop chan close! thread alts! alts!! timeout]]
            [timer.app]
            [timer.ui]
            [seesaw.core :as ss]
            ))

(declare)


(defn -main
  "Initialize the application state and show the UI."
  [& args]
  (let [app (timer.app/init args)
        ui  (timer.ui/init app)]
    (-> ui ss/show!)))
