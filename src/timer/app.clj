(ns timer.app
  (:require [timer.timer :as t])
  (:import [timer.timer Timer])
  (:use [seesaw core make-widget mig]))

;; a record for containing app state
(defrecord App
    [timers                             ; Vector of Timer records
     frame                              ; main frame
     vp                                 ; vertical-panel of timer widgets
     ])

(defn h:m:s
  "Turn a duration into a vector of hours, minutes, and seconds."
  [duration]
  (let [hour   (* 1000 60 60)
        minute (* 1000 60)
        second 1000
        h      (quot duration hour)
        mh     (mod  duration hour)
        m      (quot mh minute)
        mm     (mod  mh minute)
        s      (quot mm second)]
    (mapv #(.intValue %) [h m s])))

(defn or-zero
  "Return number if postive or zero otherwise."
  [n]
  (if (> n 0) n 0))

(defn refresh-timer!
  "Refresh the elapsed time of a timer"
  [tw state]
  (let [[h m s] (h:m:s (or-zero (- (:duration state) (:elapsed state))))]
    (selection! (-> (select tw [:JSpinner]) (nth 0)) h)
    (selection! (-> (select tw [:JSpinner]) (nth 1)) m)
    (selection! (-> (select tw [:JSpinner]) (nth 2)) s)))

(defn add-timer
  "Add a timer to app."
  [app timer]
  (let [tw (timer-widget app timer)]
    (swap! app (fn [a] (update-in a [:timers] conj timer)))
    (add! (:vp @app) tw)
    (swap! app (fn [a] (update-in a [:widgets-by-id] assoc (:id @timer) tw)))
    (add-watch timer :refresh (fn [k r o n] (refresh-timer! tw n)))
    (println (mapv #(:id (deref %)) (:timers @app))))
  app)

(defn remove-timer
  "Remove a timer from app."
  [app timer]
  (swap! app (fn [a] (update-in a [:timers] (fn [timers]
                                             (filterv #(not (= (:id @timer) (:id (deref %)))) timers)))))
  (remove! (:vp @app) ((:widgets-by-id @app) (:id @timer)))
  (swap! app (fn [a] (update-in a [:widgets-by-id] dissoc (:id @timer))))
  (t/stop! timer)
  (println (mapv #(:id (deref %)) (:timers @app)))
  app)

(defn play-timer!
  "Start an existing timer."
  [app timer]
  (t/play! timer)
  app)

(defn pause-timer!
  "Pause a currently running timer"
  [app timer]
  (t/pause! timer)
  app)

(defn stop-timer!
  "Stop a currently running timer"
  [app timer]
  (t/stop! timer)
  app)

(defn timer-widget
  "Create an interactive widget that can control a timer."
  [app timer]
  (let [[h m s] (h:m:s (:duration @timer))]
    (mig-panel
     :id
     (:id timer)

     :constraints
     []

     :items
     [[(label (or (:name timer) "Timer"))]
      [(button :action
               (action :name "X"
                       :handler (fn [e] (remove-timer app timer)))) "wrap"]
      [(spinner :id :h
                :model (spinner-model h :from 0 :to 99 :by 1))]
      [(spinner :id :m
                :model (spinner-model m :from 0 :to 59 :by 1))]
      [(spinner :id :s
                :model (spinner-model s :from 0 :to 59 :by 1)) "wrap"]
      [(button :action
               (action :name "Play"
                       :handler (fn [e] (play-timer! app timer))))]
      [(button :action
               (action :name "Stop"
                       :handler (fn [e] (stop-timer! app timer))))]
      [(button :action
               (action :name "Pause"
                       :handler (fn [e] (pause-timer! app timer))))]
      ])))

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

(defn t
  "Return timer n."
  [app n]
  ((-> @app :timers) n))


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
