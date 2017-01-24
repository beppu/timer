(ns timer.core
  (:gen-class)
  (:require [timer.app]
            [clojure.tools.cli :as cli]))

(defn -main
  "Initialize the application state and show the UI."
  [& args]
  ;; TODO - load saved timers from disk
  (let [app (timer.app/init {})]
    (timer.app/start app)
    app))
