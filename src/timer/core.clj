(ns timer.core
  (:gen-class)
  (:require [timer.app]
            [seesaw.core :as ss]
            ))

(defn -main
  "Initialize the application state and show the UI."
  [& args]
  ;; TODO - load saved timers from disk
  (let [app (timer.app/init {})]
    (timer.app/start app)))
