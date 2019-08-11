(ns com.klarna.donkej.views
  (:require [com.klarna.donkej.events :as events]
            [com.klarna.donkej.subs :as subs]
            [com.klarna.donkej.talks :as talks]
            [goog.dom :as gdom]
            [re-frame.core :as rf]))

(def gen-key (partial gensym "key-"))

(defn render-row [columns]
  [:tr {:key (gen-key)} (map (fn [column] [:td {:key column} column]) columns)])

(defn input-row [id label placeholder]
  [:div {:style {:display "flex"
                 :justify-content "space-between"
                 :margin "2px 0px 2px 0px"}}
   [:div {:style {:font-weight "bold"}} label]
   [:input {:id id :type "text" :placeholder placeholder :size 64}]])

(defn grab-value! [element-id]
  (let [element (gdom/getElement element-id)
        value (.-value element)]
    (if value
      (do
        (set! (.-value element) "")
        value)
      (println "Missing value for element" element-id))))

(defn add-talk [& _]
  (let [vals (map grab-value! ["title-input" "speakers-input" "url-input"])]
    (if (every? identity vals)
      (rf/dispatch (vec (concat [::events/add-talk talks/add-talk!] vals)))
      (println "Will highlight missing field"))))

(defn main-panel []
  (let [talks (rf/subscribe [::subs/talks])]
    [:div
     [:h1 "Donkej"]
     [:h2 "Submitted talks"]
     [:table
      [:tbody
       (->> @talks
            (map (fn [{:keys [id title speakers url]}]
                   [:tr {:key id}
                    [:td [:a {:href url :target "_blank"} title]]
                    [:td speakers]])))]]
     [:div
      [:h2 "Submit your own talk!"]
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       (input-row "title-input" "Title" "Title")
       (input-row "speakers-input" "Speakers" "Speaker 1, Speaker 2")
       (input-row "url-input" "URL" "https://example.com/awesome-talk.html")
       [:div {:style {:align-self "center"
                      :margin-top "2px"}}
        [:button {:on-click add-talk} "Submit"]]]]]))
