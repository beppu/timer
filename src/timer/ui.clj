(ns timer.ui
  (:use [seesaw core border table mig]))

(defn timer-widget
  "Return an interactive widget for controlling a timer."
  [timer]
  (label "Timer"))

(defn timer-list
  [timers]
  (scrollable
   (vertical-panel
    :items (map #(timer-widget %) timers))))

(defn app-layout
  [app]
  (border-panel
   :border 5
   :hgap 5
   :vgap 5
   :north (button
           :action (action :name "Add Timer" :handler (fn [e] (println e))))
   :center (timer-list (:timers @app))))

(defn init
  "Return a frame that contains the App's UI"
  [app]
  (native!)
  (let [icon (clojure.java.io/resource "clock.png")]
    (frame :title (or (:title @app) "Timer")
           :icon icon
           :size [640 :by 480]
           :content (app-layout app))))
