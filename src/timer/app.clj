(ns timer.app)

;; a record for containing app state
(defrecord App [timers])

(defn add-timer
  "Add a timer to app."
  [app timer]
  app)

(defn remove-timer
  "Remove a timer from app."
  [app i]
  app)

(defn play-timer
  "Start an existing timer."
  [app i]
  app)

(defn pause-timer
  "Pause a currently running timer"
  [app i]
  app)

(defn stop-timer
  "Stop a currently running timer"
  [app i]
  app)


(defn init
  "Return an atom with the initial application state."
  [args]
  (atom (map->App {:timers []
                   :title "Timer"})))
