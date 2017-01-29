(ns timer.app
  (:require [timer.timer :as t]
            [taoensso.timbre :as timbre :refer [debug]])
  (:import [timer.timer Timer])
  (:use [seesaw core make-widget mig]))

;; a record for containing app state
(defrecord App
    [timers                             ; Vector of Timer records
     frame                              ; main frame
     vp                                 ; vertical-panel of timer widgets
     widgets-by-id                      ; map of widgets by id
     ])

(declare timer-widget)

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

(defn refresh-timer!
  "Refresh the elapsed time of a timer"
  [tw state]
  (let [[h m s] (h:m:s (max 0 (- (:duration state) (:elapsed state))))]
    (config! (select tw [:#hr]) :text h)
    (config! (select tw [:#mr]) :text m)
    (config! (select tw [:#sr]) :text s)))

(defn add-timer!
  "Add a timer to app."
  [app timer]
  (let [tw (timer-widget app timer)]
    (swap! app (fn [a] (update-in a [:timers] conj timer)))
    (add! (:vp @app) tw)
    (swap! app (fn [a] (update-in a [:widgets-by-id] assoc (:id @timer) tw)))
    (add-watch timer :refresh (fn [k r o n] (refresh-timer! tw n)))
    (debug (mapv #(:id (deref %)) (:timers @app))))
  app)

(defn remove-timer!
  "Remove a timer from app."
  [app timer]
  (swap! app (fn [a] (update-in a [:timers] (fn [timers]
                                             (filterv #(not (= (:id @timer) (:id (deref %)))) timers)))))
  (remove! (:vp @app) ((:widgets-by-id @app) (:id @timer)))
  (swap! app (fn [a] (update-in a [:widgets-by-id] dissoc (:id @timer))))
  (t/stop! timer)
  (debug (mapv #(:id (deref %)) (:timers @app)))
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

(defn duration-from-timer
  "Given an app and a timer, read the associated timer-widget's spinner widgets and calculate the duration."
  [app timer]
  (let [tw       ((-> @app :widgets-by-id) (:id @timer))
        [h m s]  (->> (select tw [:JSpinner]) (map selection))
        duration (+ (* 1000 s) (* 1000 60 m) (* 1000 60 60 h))]
    duration))

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
     [[(label (or (:name timer) "Timer")) "span 3"]
      [(button :action
               (action :name "X"
                       :handler (fn [e] (remove-timer! app timer)))) "wrap"]
      [(label :id :hr :v-text-position :bottom :h-text-position :right :text "0")]
      [(label :id :mr :v-text-position :bottom :h-text-position :right :text "0")]
      [(label :id :sr :v-text-position :bottom :h-text-position :right :text "0") "wrap"]
      [(spinner :id :h
                :model (spinner-model h :from 0 :to 99 :by 1)
                :listen
                [:change
                 (fn [e]
                   (let [new-duration (duration-from-timer app timer)]
                     (debug new-duration)
                     (t/duration! timer (duration-from-timer app timer))
                     ))]
                )]
      [(spinner :id :m
                :model (spinner-model m :from 0 :to 59 :by 1)
                :listen
                [:change
                 (fn [e]
                   (let [new-duration (duration-from-timer app timer)]
                     (debug new-duration)
                     (t/duration! timer (duration-from-timer app timer))
                     ))]
                )]
      [(spinner :id :s
                :model (spinner-model s :from 0 :to 59 :by 1)
                :listen
                [:change
                 (fn [e]
                   (let [new-duration (duration-from-timer app timer)]
                     (debug new-duration)
                     (t/duration! timer (duration-from-timer app timer))
                     ))]
                ) "wrap"]
      [(button :action
               (action :name "Play"
                       :handler (fn [e] (play-timer! app timer))))]
      [(button :action
               (action :name "Stop"
                       :handler (fn [e] (stop-timer! app timer))))]
      [(button :action
               (action :name "Pause"
                       :handler (fn [e] (pause-timer! app timer)))) "wrap"]
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
                                          (add-timer! app (t/init {}))
                                          ))))
     :center (scrollable vp))))

(defn t
  "Return timer n."
  [app n]
  ((-> @app :timers) n))

(defn tw
  "Return timer-widget n."
  [app n]
  (let [t (t app n)]
    ((-> @app :widgets-by-id) (:id @t))))

(defn init
  "Return an atom with the initial application state."
  [opts]
  (let [timers  (or (:timers opts) [])
        close   (or (:on-close opts) :exit)
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
                            :on-close close
                            :content (app-layout app))))
      app)))

(defn start
  "Start the application and show the UI."
  [app]
  (-> (:frame @app) show!))
