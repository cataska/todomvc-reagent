(ns todomvc-reagent.core
    (:require [reagent.core :as r]))

(def todos
  (r/atom [{:value "Create a TodoMVC template"} {:value "Rule the web"}]))

;; -------------------------
;; Views

(defn todo-header [title placeholder]
  [:header.header
   [:h1 title]
   [:input.new-todo
    {:autofocus true, :placeholder placeholder}]])

(defn home-page []
  (fn []
    [:div
     [:section.todoapp
      [todo-header "reagent" "What needs to be done?"]

      [:section.main
       [:input.toggle-all {:type "checkbox"}]
       [:label {:for "toggle-all"} "Mark all as complte"]
       
       [:ul.todo-list
        [:li.completed
         [:div.view
          [:input.toggle {:type "checkbox" :checked "checked"}]
          [:label "Taste JavaScript"]
          [:button.destroy]
          [:input.edit {:value "Create a TodoMVC template"}]]]
        [:li
         [:div.view
          [:input.toggle {:type "checkbox"}]
          [:label "Buy a unicorn"]
          [:button.destroy]
          [:input.edit {:value "Rule the web"}]]]
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
