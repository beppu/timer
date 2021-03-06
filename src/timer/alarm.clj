(ns timer.alarm
  (:require [clj-audio.core :as audio]
            [clojure.core.async :as a
             :refer
             [<!
              <!!
              >!
              >!!
              alts!
              alts!!
              buffer
              chan
              close!
              go
              go-loop
              thread
              timeout]]
            [taoensso.timbre :as timbre :refer [debug]]))

(defrecord Alarm
    [name                               ; String
     status                             ; Symbol :stopped :running
     control                            ; Channel
     cb                                 ; Function
     ])

(defn default-start
  "Run an alarm that repeatedly prints a string while waiting for a stop message."
  [aa]
  (go-loop []
    (let [[c channel] (alts! [(:control @aa) (timeout 1000)])]
      (if c
        (debug "Stop Alarm" c)
        (do
          (debug "Alarm [" (:name @aa) "]")
          (recur))))))

(defn play-with-future
  "Play audio only if another audio is not already playing."
  ([file]
   (future (audio/play (audio/->stream file))))

  ([file ft]
   (if (future? ft)
     (if (future-done? ft)
       (play-with-future file)
       ft)
     (play-with-future file))
   ))

(defn wav-start-fn
  "Return an alarm start function that repeatedly plays a .wav file"
  [wav]
  (fn [aa]
    (go-loop [f nil]
      (let [[c channel] (alts! [(:control @aa) (timeout 1000)])]
        (if c
          (debug "Stop Alarm" c)
          (do
            (recur (play-with-future wav f))))))))

(defn start!
  "Start alarm."
  [aa]
  (when (= :stopped (:status @aa))
    (swap! aa #(assoc %1 :status :running))
    ((:cb @aa) aa))
  aa)

(defn stop!
  "Stop alarm."
  [aa]
  (when (= :running (:status @aa))
    (swap! aa #(assoc %1 :status :stopped))
    (>!! (:control @aa) :stop))
  aa)

(defn init
  "Instantiate an alarm and wrap it in an atom."
  [opts]
  (atom (map->Alarm (merge
                     {:name    "Default"
                      :status  :stopped
                      :control (chan)
                      :cb      (wav-start-fn (clojure.java.io/resource "tada.wav"))
                      ;:cb      default-start
                      } opts))))
