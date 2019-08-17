(ns donkej.views
  (:require [donkej.date :as date]
            [donkej.emoji :as emoji]
            [donkej.events :as events]
            [donkej.icons :as icons]
            [donkej.subs :as subs]
            [donkej.talks :as talks]
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

(defn display-current-talks []
  (let [talks (rf/subscribe [::subs/talks])
        username (rf/subscribe [::subs/username])]
    [:table {:width "100%"}
     [:tbody
      (->> @talks
           (remove :date-watched)
           (map (fn [{:keys [id title speakers url votes]}]
                  [:tr {:key id}
                   [:td {:width "55%"} [:a {:href url :target "_blank"} title]]
                   [:td {:width "30%"} speakers]
                   [:td {:width "5%"
                         :style {:cursor "pointer"}
                         :on-click (fn [& _]
                                     (rf/dispatch [::events/mark-watched talks/mark-watched! id]))}
                    emoji/eyes]
                   [:td {:width "5%"
                         :style {:cursor "pointer"}
                         :on-click (fn [& _]
                                     (rf/dispatch [::events/vote-for-talk talks/update-votes! id @username]))}
                    emoji/thumbs-up]
                   [:td {:width "5%"} (count votes)]])))]]))

(defn display-watched-talks []
  (let [talks (rf/subscribe [::subs/talks])]
    [:table {:width "100%"}
     [:tbody
      (->> @talks
           (filter :date-watched)
           (map (fn [{:keys [id title speakers url date-watched]}]
                  [:tr {:key id}
                   [:td {:width "55%"} [:a {:href url :target "_blank"} title]]
                   [:td {:width "30%"} speakers]
                   [:td {:width "15%"} (str (date/->iso-date date-watched) " " emoji/check-mark)]])))]]))

(defn main-panel []
  (let [error-msg (rf/subscribe [::subs/error-msg])
        username (rf/subscribe [::subs/username])]
    [:div
     [:h1 "Donkej"]
     [:p (str "Welcome, " (if @username @username "SET THIS") "!")]
     (when @error-msg
       [:div
        [:p {:style {:font-weight "bold"
                     :outline-color "red"
                     :outline-style "auto"
                     :padding "2px"}}
         @error-msg]])
     [:div {:style {:display "flex"
                    :justify-content "space-between"}}
      [:h2 "Submitted talks"]
      [:div {:style {:cursor "pointer"
                     :padding "5px"}
             :on-click (fn [_ &]
                         (println "Refreshing")
                         (talks/load-talks))}
       (icons/refresh 15 15)]]
     (display-current-talks)
     [:div
      [:h2 "Submit your own talk!"]
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       (input-row "title-input" "Title" "Title")
       (input-row "speakers-input" "Speakers" "Speaker 1, Speaker 2")
       (input-row "url-input" "URL" "https://example.com/awesome-talk.html")
       [:div {:style {:align-self "center"
                      :margin-top "2px"}}
        [:button {:on-click add-talk} "Submit"]]]]
     (display-watched-talks)]))
