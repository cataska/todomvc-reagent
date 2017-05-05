(ns todomvc-reagent.core
    (:require [reagent.core :as r]))

(def todos
  (r/atom [{:value "Taste JavaScript"} {:value "Buy a unicorn"}]))

;; -------------------------
;; Views

(defn todo-header [title placeholder]
  [:header.header
   [:h1 title]
   [:input.new-todo
    {:autofocus true, :placeholder placeholder}]])

(defn todo-list [todos]
  (for [todo todos]
    [:li
     [:div.view
      [:input.toggle {:type "checkbox"}]
      [:label (:value todo)]
      [:button.destroy]
      [:input.edit {:value ""}]]]))

(defn home-page []
  (fn []
    [:div
     [:section.todoapp
      [todo-header "reagent" "What needs to be done?"]

      [:section.main
       [:input.toggle-all {:type "checkbox"}]
       [:label {:for "toggle-all"} "Mark all as complte"]
       
       [:ul.todo-list
        (todo-list @todos)
        ]]
      
      [:footer.footer
       [:span.todo-count [:strong "0"] " item left"]
       [:ul.filters
        [:li [:a.selected {:href "#/"} "All"]]
        [:li [:a {:href "#/active"} "Active"]]
        [:li [:a {:href "#/completed"} "Completed"]]]
       [:button.clear-completed "Clear completed"]]]
     
     [:footer.info
      [:p "Double-click to edit a todo"]
      [:p "Template by "
       [:a {:href "http://sindresorhus.com"}]
       "http://sindresorhus.com"]
      [:p "Created by "
       [:a {:href "http://todomvc.com"}]
       "you"]
      [:p "Part of  "
       [:a {:href "http://todomvc.com"}]
       "TodoMVC"]]
     ])
  )

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
