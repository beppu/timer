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

(defn timer-widget
  "Create an interactive widget that can control a timer."
  [app timer]
  (mig-panel
   :id (:id timer)
   :items [[(label (or (:name timer) "Timer"))]
           [(button :action
                    (action :name "X"
                            :handler (fn [e] (remove-timer app timer))))]

           ]))


(defn add-timer
  "Add a timer to app."
  [app timer]
  (let [tw (timer-widget app timer)]
    (swap! app (fn [a] (update-in a [:timers] conj timer)))
    (add! (:vp @app) tw)
    (swap! app (fn [a] (update-in a [:widgets-by-id] assoc (:id @timer) tw)))
    (println (mapv #(:id (deref %)) (:timers @app))))
  app)

(defn remove-timer
  "Remove a timer from app."
  [app timer]
  (swap! app (fn [a] (update-in a [:timers] (fn [timers]
                                             (filterv #(not (= (:id @timer) (:id (deref %)))) timers)))))
  ;;(println :id (:id @timer))
  ;;(println :timer ((-> @app :widgets-by-id) (-> @timer :id)))
  (remove! (:vp @app) ((:widgets-by-id @app) (:id @timer)))
  (swap! app (fn [a] (update-in a [:widgets-by-id] dissoc (:id @timer))))
  (println (mapv #(:id (deref %)) (:timers @app)))
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
