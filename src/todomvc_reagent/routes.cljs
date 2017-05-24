(ns todomvc-reagent.routes
  (:require [secretary.core :as secretary :refer-macros [defroute]]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [todomvc-reagent.session :as session])
  (:import goog.History))

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(defroute "/" []
  (reset! session/display-type :all))

(defroute "/active" []
  (reset! session/display-type :active))

(defroute "/completed" []
  (reset! session/display-type :completed))

(defn hook-browser-navigation! []
  (doto (History.)
        (events/listen
          HistoryEventType/NAVIGATE
          (fn [event]
              (secretary/dispatch! (.-token event))))
        (.setEnabled true)))

