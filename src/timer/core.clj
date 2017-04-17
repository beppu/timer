(ns timer.core
  (:gen-class)
  (:use [seesaw.core])
  (:require [timer.app]
            [clojure.tools.cli :as cli]
            [clojure.string :as string]
            [taoensso.timbre :as timbre
             :refer [log trace debug info warn error fatal report
                     logf tracef debugf infof warnf errorf fatalf reportf
                     spy get-env]]))

(def cli-options [["-c" "--on-close ACTION" "When closing window: exit, hide, dispose or nothing."
                   :default :exit
                   :parse-fn (fn [s] (keyword s))
                   :validate [(fn [s] (#{:exit :hide :dispose :nothing} s)) "Valid values are exit, hide, dispose or nothing."]]
                  ["-h" "--help" "Display help message"]
                  ["-v" "--verbose" "Be verbose"]])

;; credit: https://github.com/clojure/tools.cli/blob/master/README.md
(defn usage
  "Return usage string."
  [options-summary]
  (->>
   ["Usage: timer [OPTION]... "
    ""
    "Options:"
    options-summary
    ""
    ]
   (string/join \newline)))

(defn error-msg [errors]
  "Return error message."
  (str "The following errors occurred while parsing your command:\n\n"
       (string/join \newline errors)))

(defn exit [status msg]
  "Print a message and exit with status."
  (println msg)
  (System/exit status))

;; To start the UI in the REPL for debugging:
;; (-main "--on-close" "hide")
(defn -main
  "Initialize the application state and show the UI."
  [& args]
  ;; TODO - load saved timers from disk
  (native!)
  (let [{:keys [options arguments errors summary]} (cli/parse-opts args cli-options)
        app     (timer.app/init options)]
    (cond
      (:help options) (exit 0 (usage summary))
      errors          (exit 1 (error-msg errors)))
    (info "started")
    (timer.app/start app)
    app))
