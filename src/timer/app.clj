(ns timer.app
  (:require [timer.timer :as timer])
  (:import [timer.timer Timer])
  (:use [seesaw core make-widget mig]))

;; a record for containing app state
(defrecord App
    [timers                             ; Vector of Timer records
     frame                              ; main frame
     vp                                 ; vertical-panel of timer widgets
     ])

(extend-type Timer
  MakeWidget
  (make-widget* [timer]
    (label (or (:name timer) "Timer"))))

(defn add-timer
  "Add a timer to app."
  [app timer]
  ;; TODO - add timer to :timers list
  ;; TODO - add timer-widget to :vp
  (swap! app (fn [a] (update-in a [:timers] conj timer)))
  (add! (:vp @app) @timer)
  (println @app)
  app)

(defn remove-timer
  "Remove a timer from app."
  [app timer]
  app)

(defn play-timer
  "Start an existing timer."
  [app timer]
  app)

(defn pause-timer
  "Pause a currently running timer"
  [app timer]
  app)

(defn stop-timer
  "Stop a currently running timer"
  [app timer]
  app)

(defn app-layout
  [app]
  (let [vp (:vp @app)]
    (border-panel
     :border 5
     :hgap 5
     :vgap 5
     :north (button
             :action (action :name "Add Timer"
                             :handler (fn [e]
                                        (do
                                          (add-timer app (timer/init {}))
                                          ))))
     :center (scrollable vp))))

(defn init
  "Return an atom with the initial application state."
  [opts]
  (let [timers  (or (:timers opts) [])
        vp      (vertical-panel :items timers)
        icon    (clojure.java.io/resource "clock.png")
        app     (atom (map->App (merge
                                 {:timers timers
                                  :vp     vp
                                  :title "Timers"})))]
    (do
      (swap! app
             #(assoc % :frame
                     (frame :title (:title @app)
                            :icon icon
                            :size [640 :by 480]
                            :content (app-layout app))))
      app)))

(defn start
  "Start the application and show the UI."
  [app]
  (-> (:frame @app) show!))
