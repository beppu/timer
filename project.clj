(defproject timer "1.0.0-SNAPSHOT"
  :description "An application for managing countdown timers"
  :url "https://github.com/DimensionSoftware/timer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]
                 [seesaw "1.4.5"]
                 [clj-time "0.13.0"]
                 [failjure "0.1.4"]
                 [xdg-basedir "1.0.0"]
                 [com.taoensso/timbre "4.8.0"]
                 [org.clojars.beppu/clj-audio "0.3.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main ^:skip-aot timer.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
