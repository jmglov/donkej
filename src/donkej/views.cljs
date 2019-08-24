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

(defn input-row [field label placeholder]
  (let [talk @(rf/subscribe [::subs/editing-talk])]
    [:div {:style {:display "flex"
                   :justify-content "space-between"
                   :margin "2px 0px 2px 0px"}}
     [:div {:style {:font-weight "bold"}} label]
     [:input {:type "text"
              :size 64
              :value (get talk field)
              :placeholder placeholder
              :on-change #(rf/dispatch [::events/edit-field field (-> % .-target .-value)])}]]))

(defn talk-editor []
  (let [{:keys [id title speakers url]} @(rf/subscribe [::subs/editing-talk])]
    [:div
      (if id
        [:h2 "Editing talk"]
        [:h2 "Submit your own talk!"])
      [:div {:style {:display "flex"
                     :flex-direction "column"}}
       [input-row :title "Title" "Title"]
       [input-row :speakers "Speakers" "Speaker 1, Speaker 2"]
       [input-row :url "URL" "https://example.com/awesome-talk.html"]
       [:div {:style {:align-self "center"
                      :margin-top "2px"}}
        (if id
          [:div
           [:button {:on-click #(rf/dispatch [::events/update-talk talks/update-talk!])} "Update"]
           [:button {:on-click #(rf/dispatch [::events/cancel-editing])} "Cancel"]]
          [:button {:on-click #(rf/dispatch [::events/add-talk talks/add-talk!])} "Submit"])]]]))

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
                          :style {:cursor "pointer"}}
                     [:div {:style {:display "flex"
                                    :justify-content "flex-end"}}
                      (when (= @username submitted-by)
                        [:span {:style {:cursor "pointer"
                                        :margin-right "10px"}
                                :on-click (fn [& _]
                                            (rf/dispatch [::events/edit-talk id]))}
                         emoji/pencil])
                      [:span {:style {:cursor "pointer"}
                              :on-click (fn [& _]
                                          (rf/dispatch [::events/mark-watched talks/mark-watched! id]))}
                       emoji/eyes]]]
                    [:td {:width "5%"}
                     [:div {:style {:display "flex"
                                    :justify-content "flex-end"}}
                      [:span {:style {:cursor "pointer"}
                              :on-click (fn [& _]
                                          (rf/dispatch [::events/vote-for-talk talks/update-votes! id @username]))}
                       emoji/thumbs-up]]]
                    [:td {:width "5%"}
                     [:div {:style {:display "flex"
                                    :justify-content "flex-end"}}
                      [:span (count votes)]]]]))
            doall)]]]))

(defn display-watched-talks []
  (let [watched-talks (filter :date-watched @(rf/subscribe [::subs/talks]))]
    [:div
     [:h2 "Watched talks"]
     [:table {:width "100%"}
      (table-header ["Title" "Speakers" "Date Watched"])
      [:tbody
       (->> watched-talks
            (sort-by :date-watched)
            reverse
            (map (fn [{:keys [id title speakers url date-watched]}]
                   [:tr {:key id}
                    [:td {:width "55%"} [:a {:href url :target "_blank"} title]]
                    [:td {:width "30%"} speakers]
                    [:td {:width "15%"} (str (date/->iso-date date-watched) " " emoji/check-mark)]])))]]]))

(defn main-panel []
  (let [error-msg @(rf/subscribe [::subs/error-msg])
        username @(rf/subscribe [::subs/username])]
    [:div
     [:h1 "Donkej"]
     [:p (str "Welcome, " (if username username "SET THIS") "!")]
     (when error-msg
       [:div
        [:p {:style {:font-weight "bold"
                     :outline-color "red"
                     :outline-style "auto"
                     :padding "2px"}}
         error-msg]])
     [display-current-talks]
     [talk-editor]
     [display-watched-talks]]))
