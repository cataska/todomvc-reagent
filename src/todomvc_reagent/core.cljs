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

(defn init []
  (add-todo "Taste JavaScript" true)
  (add-todo "Buy a unicorn"))

(init)

;; -------------------------
;; Helper

(defn toggle [id]
  (swap! todos update-in [id :done] not))
(defn delete [id]
  (swap! todos dissoc id))
(defn save [title]
  (when (not-empty title) (add-todo title)))
(defn clear-completed []
  (reset! todos (->> (vals @todos)
                     (filter :done)
                     (map :id)
                     (reduce dissoc @todos))))
(defn complete-all [v]
  (reset! todos (reduce #(assoc-in %1 [%2 :done] v) @todos (keys @todos))))

;; -------------------------
;; Views

(defn todo-header [title placeholder]
  (let [val (r/atom "")
        stop #(reset! val "")]
    (fn []
      [:header.header
       [:h1 title]
       [:input.new-todo
        {:value @val
         :autoFocus true
         :placeholder placeholder
         :on-change #(reset! val (-> % .-target .-value))
         :on-key-down #(case (.-which %)
                         13 (do (save @val) (stop))
                         27 (stop)
                         nil)}]])))

(defn todo-item []
  (fn [{:keys [id title done]}]
    [:li {:class (if done "completed" "")}
     [:div.view
      [:input.toggle {:type "checkbox" :checked done :on-change #(toggle id)}]
      [:label title]
      [:button.destroy {:on-click #(delete id)}]
      [:input.edit {:value ""}]]]))

(defn todo-list [showing todos]
  [:ul.todo-list
   (for [todo (filter (case @showing
                        :all identity
                        :active (complement :done)
                        :completed :done)
                      todos)]
     ^{:key (:id todo)} [todo-item todo])])

(defn todo-footer [showing completed-cnt count]
  (let [a-fn (fn [kw url txt]
               [:a {:class (when (= kw @showing) "selected")
                    :href url
                    :on-click #(reset! showing kw)}
                txt])]
   [:footer.footer
    [:span.todo-count [:strong (str count)] " item left"]
    [:ul.filters
     [:li (a-fn :all "#/" "All")]
     [:li (a-fn :active "#/active" "Active")]
     [:li (a-fn :completed "#/completed" "Completed")]]
    (when (pos? completed-cnt)
      [:button.clear-completed {:on-click #(clear-completed)} "Clear completed"])]))

(defn home-page []
  (let [showing (r/atom :all)]
   (fn []
     (let [items (vals @todos)
           completed-cnt (->> items (filter :done) count)
           active-cnt (- (count items) completed-cnt)]
      [:div
       [:section.todoapp
        [todo-header "reagent" "What needs to be done?"]

        (when (pos? (count items))
          [:div
           [:section.main
            [:input.toggle-all {:type "checkbox" :checked (zero? active-cnt) :on-click #(complete-all (pos? active-cnt))}]
            [:label {:for "toggle-all"} "Mark all as complte"]

            [todo-list showing items]]

           [todo-footer showing completed-cnt (count items)]])
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
         "TodoMVC"]]]))))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (mount-root))
