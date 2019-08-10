(ns com.klarna.donkej2.views
  (:require [re-frame.core :as rf]
            [com.klarna.donkej2.events :as events]
            [com.klarna.donkej2.subs :as subs]))

(defn render-row [columns]
  [:tr (map (fn [column] [:td column]) columns)])

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
      (render-row ["Title" "URL"])
      (map #(render-row (map % [:title :url])) @talks)]
     [:div
      [:h2 "Submit your own talk!"]
      [:input {:type "text" :placeholder "Title" :id "title-input"}]
      [:input {:type "text" :placeholder "URL" :id "url-input"}]
      [:button {:on-click add-talk} "Submit"]]]))
