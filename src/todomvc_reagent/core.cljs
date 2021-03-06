(ns todomvc-reagent.core
  (:require [reagent.core :as r]
            [clojure.string :as s]
            [todomvc-reagent.session :as session]
            [todomvc-reagent.routes :as routes]))

;; -------------------------
;; Constants

(def KEY_ENTER 13)
(def KEY_ESCAPE 27)

;; -------------------------
;; Helper

(defn add-todo
  ([title]
   (add-todo title false))
  ([title done]
   (let [id (swap! session/counter inc)]
     (swap! session/todos assoc id {:id id :title title :done done}))))

(defn toggle [id]
  (swap! session/todos update-in [id :done] not))
(defn delete [id]
  (swap! session/todos dissoc id))
(defn save
  ([title]
   (when (not-empty title) (add-todo title)))
  ([id title]
   (swap! session/todos assoc-in [id :title] title)))
(defn clear-completed []
  (reset! session/todos (->> (vals @session/todos)
                             (filter :done)
                             (map :id)
                             (reduce dissoc @session/todos))))
(defn complete-all [v]
  (reset! session/todos (reduce #(assoc-in %1 [%2 :done] v) @session/todos (keys @session/todos))))

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
        :on-key-down #(cond
                        (= (.-which %) KEY_ENTER) (save)
                        (= (.-which %) KEY_ESCAPE) (stop)
                        :else nil)}])))

(defn todo-header [title placeholder]
  [:header.header
   [:h1 title]
   [todo-input {:class "new-todo" :placeholder placeholder
                :on-save add-todo}]])

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

(defn todo-list [todos]
  [:ul.todo-list
   (for [todo (filter (case @session/display-type
                        :all identity
                        :active (complement :done)
                        :completed :done)
                      todos)]
     ^{:key (:id todo)} [todo-item todo])])

(defn todo-footer [completed-cnt count]
  (let [a-fn (fn [kw url txt]
               [:a {:class (when (= kw @session/display-type) "selected")
                    :href url}
                txt])
        active-cnt (- count completed-cnt)]
   [:footer.footer
    [:span.todo-count [:strong (str active-cnt)] (str (if (= active-cnt 1) " item" " items") " left")]
    [:ul.filters
     [:li (a-fn :all "#/" "All")]
     [:li (a-fn :active "#/active" "Active")]
     [:li (a-fn :completed "#/completed" "Completed")]]
    (when (pos? completed-cnt)
      [:button.clear-completed {:on-click #(clear-completed)} "Clear completed"])]))

(defn home-page []
   (fn []
     (let [items (vals @session/todos)
           completed-cnt (->> items (filter :done) count)
           active-cnt (- (count items) completed-cnt)]
      [:div
       [:section.todoapp
        [todo-header "reagent" "What needs to be done?"]

        (when (pos? (count items))
          [:div
           [:section.main
            [:input.toggle-all {:type "checkbox"
                                :checked (zero? active-cnt)
                                :on-click #(complete-all (pos? active-cnt))}]
            [:label {:for "toggle-all"} "Mark all as complte"]

            [todo-list items]]

           [todo-footer completed-cnt (count items)]])]
     
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
         "TodoMVC"]]])))

;; -------------------------
;; Initialize app

(defn mount-root []
  (r/render [home-page] (.getElementById js/document "app")))

(defn init! []
  (routes/hook-browser-navigation!)
  (mount-root))
