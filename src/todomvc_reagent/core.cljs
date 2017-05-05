(ns todomvc-reagent.core
    (:require [reagent.core :as r]))

;; -------------------------
;; Db

(def todos
  (r/atom (sorted-map)))

(def counter
  (r/atom 0))

(defn add-todo
  ([title]
   (add-todo title false))
  ([title done]
   (let [id (swap! counter inc)]
     (swap! todos assoc id {:id id :title title :done done}))))

(add-todo "Taste JavaScript" true)
(add-todo "Buy a unicorn")

;; -------------------------
;; Helper
(defn toggle [id]
  (swap! todos update-in [id :done] not))
(defn delete [id]
  (swap! todos dissoc id))

;; -------------------------
;; Views

(defn todo-header [title placeholder]
  [:header.header
   [:h1 title]
   [:input.new-todo
    {:autofocus true, :placeholder placeholder}]])

(defn todo-item [todo]
    (fn [{:keys [id title done]}]
      [:li {:class (if done "completed" "")}
       [:div.view
        [:input.toggle {:type "checkbox" :checked done :on-change #(toggle id)}]
        [:label title]
        [:button.destroy {:on-click #(delete id)}]
        [:input.edit {:value ""}]]]))

(defn todo-list [todos]
  [:ul.todo-list
   (for [todo todos]
     ^{:key (:id todo)} [todo-item todo])])

(defn todo-footer [count]
  (when (> count 0)
    [:footer.footer
     [:span.todo-count [:strong (str count)] " item left"]
     [:ul.filters
      [:li [:a.selected {:href "#/"} "All"]]
      [:li [:a {:href "#/active"} "Active"]]
      [:li [:a {:href "#/completed"} "Completed"]]]
     [:button.clear-completed "Clear completed"]]))

(defn home-page []
  (fn []
    [:div
     [:section.todoapp
      [todo-header "reagent" "What needs to be done?"]

      [:section.main
       [:input.toggle-all {:type "checkbox"}]
       [:label {:for "toggle-all"} "Mark all as complte"]

       [todo-list (vals @todos)]]
      
      [todo-footer (count @todos)]
      ]
     
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
