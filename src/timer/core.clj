(ns timer.core
  (:gen-class)
  (:use [seesaw.core])
  (:require [timer.app]
            [clojure.tools.cli :as cli]
            [taoensso.timbre :as timbre
             :refer [log trace debug info warn error fatal report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]))

(def cli-options [["-c" "--on-close ACTION" "Action to take when closing window"
                   :default :exit
                   :parse-fn (fn [s] (keyword s))
                   :validate [(fn [s] (#{:exit :hide :dispose :nothing} s)) "Valid values are exit, hide, dispose or nothing."]
                  ]])

(defn -main
  "Initialize the application state and show the UI."
  [& args]
  ;; TODO - load saved timers from disk
  (native!)
  (let [results (cli/parse-opts args cli-options)
        opts    (:options results)
        app     (timer.app/init opts)]
    (info "started")
    (timer.app/start app)
    app))
