(ns todomvc-reagent.session
  (:require [reagent.core :as r]
            [alandipert.storage-atom :refer [local-storage]]))

;; -------------------------
;; Db

(def todos
  (local-storage (r/atom (sorted-map)) :todos))

(def counter
  (local-storage (r/atom 0) :counter))

