(ns donkej.views
  (:require [donkej.date :as date]
            [donkej.emoji :as emoji]
            [donkej.events :as events]
            [donkej.icons :as icons]
            [donkej.subs :as subs]
            [donkej.talks :as talks]
            [goog.dom :as gdom]
            [re-frame.core :as rf]))

(defn gen-key [prefix]
  (gensym (str "key-" prefix)))

(defn table-header [labels]
  [:thead
   [:tr (map (fn [label] [:th {:key (gen-key label)} label]) labels)]])

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
    [:div
     [:h2 "This week"]
     [:div {:style {:display "flex"
                    :justify-content "flex-end"
                    :width "100%"}}
      [:div {:style {:cursor "pointer"
                     :padding "5px"}
             :on-click (fn [_ &]
                         (println "Refreshing")
                         (talks/load-talks))}
       (icons/refresh 15 15)]]
     [:table {:width "100%"}
      (table-header ["Title" "Speakers" "" "" "" "Votes"])
      [:tbody
       (->> @talks
            (remove :date-watched)
            (sort-by (fn [{:keys [votes]}] (count votes)))
            reverse
            (map (fn [{:keys [id title speakers url votes submitted-by]}]
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
                    [:td {:width "5%"}
                     (when (= @username submitted-by)
                       [:button {:on-click (fn [& _] (rf/dispatch [::events/edit-talk id]))} "Edit"])]
                    [:td {:width "5%"} (count votes)]])))]]]))

(defn display-watched-talks []
  (let [talks (rf/subscribe [::subs/talks])]
    [:div
     [:h2 "Watched talks"]
     [:table {:width "100%"}
      (table-header ["Title" "Speakers" "Date Watched"])
      [:tbody
       (->> @talks
            (filter :date-watched)
            (sort-by :date-watched)
            reverse
            (map (fn [{:keys [id title speakers url date-watched]}]
                   [:tr {:key id}
                    [:td {:width "55%"} [:a {:href url :target "_blank"} title]]
                    [:td {:width "30%"} speakers]
                    [:td {:width "15%"} (str (date/->iso-date date-watched) " " emoji/check-mark)]])))]]]))

(defn display-backlog []
  (let [talks (rf/subscribe [::subs/talks])
        backlog (->> @talks
                     (filter :in-backlog?)
                     (sort-by :date-submitted)
                     reverse
                     (map (fn [{:keys [id title speakers url date-submitted]}]
                            [:tr {:key id}
                             [:td {:width "55%"} [:a {:href url :target "_blank"} title]]
                             [:td {:width "30%"} speakers]
                             [:td {:width "15%"} (str (date/->iso-date date-submitted))]])))]
    (when-not (empty? backlog)
      [:div
       [:h2 "Backlog"]
       [:table {:width "100%"}
        (table-header ["Title" "Speakers" "Date Submitted"])
        [:tbody
         backlog]]])))

(defn main-panel []
  (let [editing (rf/subscribe [::subs/editing])
        error-msg (rf/subscribe [::subs/error-msg])
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
     (display-current-talks)
     [:div
      (if (map? @editing)
        [:h2 "Edit your own talk!"]
        [:h2 "Submit your own talk!"])
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       (input-row "title-input" "Title" "Title")
       (input-row "speakers-input" "Speakers" "Speaker 1, Speaker 2")
       (input-row "url-input" "URL" "https://example.com/awesome-talk.html")
       [:div {:style {:align-self "center"
                      :margin-top "2px"}}
        [:button {:on-click add-talk} "Submit"]]]]
     (display-watched-talks)
     (display-backlog)]))
