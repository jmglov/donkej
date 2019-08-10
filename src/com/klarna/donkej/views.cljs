(ns com.klarna.donkej.views
  (:require [re-frame.core :as rf]
            [com.klarna.donkej.events :as events]
            [com.klarna.donkej.subs :as subs]))

(def gen-key (partial gensym "key-"))

(defn render-row [columns]
  [:tr {:key (gen-key)} (map (fn [column] [:td {:key column} column]) columns)])

(defn grab-value! [element-id]
  (let [element (.getElementById js/document element-id)
        value (.-value element)]
    (set! (.-value element) "")
    value))

(defn add-talk [& _]
  (let [vals (map grab-value! ["title-input" "url-input"])]
    (rf/dispatch (vec (concat [::events/add-talk] vals)))))

(defn main-panel []
  (let [talks (rf/subscribe [::subs/talks])]
    [:div
     [:h1 "Donkej"]
     [:h2 "Submitted talks"]
     [:table
      [:thead
       (render-row ["Title" "URL"])]
      [:tbody
       (map #(render-row (map % [:title :url])) @talks)]]
     [:div
      [:h2 "Submit your own talk!"]
      [:input {:type "text" :placeholder "Title" :id "title-input"}]
      [:input {:type "text" :placeholder "URL" :id "url-input"}]
      [:button {:on-click add-talk} "Submit"]]]))
