(ns todomvc-reagent.core
  (:require [reagent.core :as r]
            [clojure.string :as s]))

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
(defn save
  ([title]
   (when (not-empty title) (add-todo title)))
  ([id title]
   (swap! todos assoc-in [id :title] title)))
(defn clear-completed []
  (reset! todos (->> (vals @todos)
                     (filter :done)
                     (map :id)
                     (reduce dissoc @todos))))
(defn complete-all [v]
  (reset! todos (reduce #(assoc-in %1 [%2 :done] v) @todos (keys @todos))))

;; -------------------------
;; Views

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val (r/atom title)
        stop #(do (reset! val "")
                  (if on-stop (on-stop)))
        save #(let [v (-> @val str s/trim)]
                (if-not (empty? v) (on-save v))
                (stop))]
    (fn [{:keys [id class placeholder]}]
      [:input
       {:id id :class class
        :value @val
        :autoFocus true
        :placeholder placeholder
        :on-blur save
        :on-change #(reset! val (-> % .-target .-value))
        :on-key-down #(case (.-which %)
                        13 (save)
                        27 (stop)
                        nil)}])))

(defn todo-item []
  (let [editing (r/atom false)]
    (fn [{:keys [id title done]}]
      [:li {:class (str (when done "completed ") (when @editing "editing"))}
       [:div.view
        [:input.toggle {:type "checkbox" :checked done :on-change #(toggle id)}]
        [:label
         {:on-double-click #(reset! editing true)}
         title]
        [:button.destroy {:on-click #(delete id)}]]
       (when @editing
         [todo-input {:class "edit" :title title
                      :on-save #(save id %)
                      :on-stop #(reset! editing false)}])])))

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
                txt])
        active-cnt (- count completed-cnt)]
   [:footer.footer
    [:span.todo-count [:strong (str active-cnt)] (str (if (> active-cnt 1) " items" " item") " left")]
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
        [:header.header
         [:h1 "reagent"]
         [todo-input {:class "new-todo" :placeholder "What needs to be done?"
                      :on-save add-todo}]]

        (when (pos? (count items))
          [:div
           [:section.main
            [:input.toggle-all {:type "checkbox"
                                :checked (zero? active-cnt)
                                :on-click #(complete-all (pos? active-cnt))}]
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
