(ns timer.timer
  (:require [clj-time.core :as t]
            [clj-time.coerce :as c]
            [clojure.core.async :as a
             :refer [>! <! >!! <!! go go-loop chan close! thread alts! alts!! timeout]]
            [timer.alarm]
            ))

;; A record for holding timer metadata
(defrecord Timer
    [id                                 ; UUID
     name                               ; String
     status                             ; :stopped :running :paused
     duration                           ; Integer milliseconds
     elapsed                            ; Integer milliseconds
     started                            ; Integer milliseconds
     previous                           ; Integer milliseconds
     control                            ; Channel
     finished                           ; Boolean
     alarm                              ; Alarm
     ])

(defn init
  "Returns a new timer wrapped in an atom."
  [opts]
  (let [t (map->Timer (merge
                       {:id            (java.util.UUID/randomUUID)
                        :status        :stopped
                        :duration      0
                        :elapsed       0
                        :previous      0
                        :control       (chan)
                        :finished      false
                        :alarm         (timer.alarm/init {})}
                       opts))]
    (atom t)))

(defn duration!
  "Set the duration for the timer."
  [at duration]
  (swap! at #(assoc %1 :duration duration)))

(defn elapse!
  "Increase the amount of time elapsed."
  [at]
  (when (= :running (:status @at))
    (let [now      (c/to-long (t/now))
          previous (:previous @at)
          started  (:started @at)
          elapsed  (+ previous (- now started))]
      (if (and (= false (:finished @at)) (>= elapsed (:duration @at)))
        (do
          (swap! at (fn [m] (assoc m :elapsed elapsed :finished true)))
          (timer.alarm/start! (:alarm @at)))
        (swap! at (fn [m] (assoc m :elapsed elapsed))))))
  at)

(defn play!
  "Start or resume the timer."
  [at]
  (when (or (= :stopped (:status @at)) (= :paused (:status @at)))
    (swap! at (fn [m]
                (assoc m
                       :status   :running
                       :previous (:elapsed m)
                       :started  (c/to-long (t/now)))))
    (go-loop []
      (let [[c channel] (alts! [(:control @at) (timeout 100)])]
        (if c
          (case c
            :pause (do (println "Pause Timer" c))
            :stop (do (println "Stop Timer" c)
                      (timer.alarm/stop! (:alarm @at))))
          (do
            (elapse! at)
            (recur))))))
  at)

(defn pause!
  "Pause the timer."
  [at]
  (if (= :running (:status @at))
    (do
      (>!! (:control @at) :pause)
      (swap! at (fn [m]
                  (assoc m :status :paused))))
    at))

(defn stop!
  "Stop the timer and reset its elapsed time to 0."
  [at]
  (timer.alarm/stop! (:alarm @at))
  (if (or (= :paused (:status @at)) (= :running (:status @at)))
    (do
      (and (= :running (:status @at))
           (>!! (:control @at) :stop))
      (swap! at (fn [m]
                  (assoc m
                         :status   :stopped
                         :elapsed  0
                         :finished false))))
    at))
