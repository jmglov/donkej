(ns com.klarna.donkej.talks
  (:require [com.klarna.donkej.dynamodb :as dynamo]
            [com.klarna.donkej.date :as date]
            [com.klarna.donkej.events :as events]
            [re-frame.core :as rf]))

(def talks-table "hsi-tools-donkej-talks")

(defn ->votes-model [votes]
  (.createSet (dynamo/make-client) (clj->js votes)))

(defn ->model [{:keys [votes] :as talk}]
  (if (empty? votes)
    talk
    (-> talk
        (update :date-submitted date/->iso-datetime)
        (update :date-watched date/->iso-datetime)
        (update :votes ->votes-model))))

(defn ->talk [model]
  (-> model
      (update :date-submitted date/->date)
      (update :date-watched date/->date)
      (update :votes (fn [votes] (if votes (set (.-values votes)) #{})))))

(defn add-talk! [talk]
  (println "Persisting talk to Dynamo:" (pr-str talk))
  (-> (dynamo/make-client)
      (dynamo/put! talks-table (->model talk) println)))

(defn load-talks []
  (-> (dynamo/make-client)
      (dynamo/scan talks-table
                   (fn [{items :Items}]
                     (println "Loaded talks from Dynamo:" (pr-str items))
                     (rf/dispatch [::events/set-talks (map ->talk items)])
                     (rf/dispatch [::events/clear-error-msg])))))

(defn mark-watched! [{:keys [id date-watched]}]
  (-> (dynamo/make-client)
      (dynamo/update! talks-table {:id id} {:expression "SET #date_watched = :date"
                                            :condition "attribute_not_exists(#date_watched)"
                                            :names {"#date_watched" "date-watched"}
                                            :values {":date" date-watched}}
                      (fn [_ &]
                        (println "Marked talk as watched at" date-watched)))))

(defn update-votes! [{:keys [id votes] :as talk} username]
  (println "Updating vote for" username "on talk" id)
  (println "Old votes:" votes)
  (let [params
        (cond
          (empty? votes)
          {:expression "SET votes = :new_votes"
           :condition "attribute_not_exists(votes)"
           :values {":new_votes" (->votes-model #{username})}}

          (contains? votes username)
          {:expression "DELETE votes :new_votes"
           :condition "contains(votes, :username)"
           :values {":username" username
                    ":new_votes" (->votes-model #{username})}}

          :else
          {:expression "ADD votes :new_votes"
           :condition "NOT contains(votes, :username)"
           :values {":username" username
                    ":new_votes" (->votes-model username)}})]
    (println "Dynamo params:" (pr-str params))
    (-> (dynamo/make-client)
        (dynamo/update! talks-table {:id id} params
                        (fn [_ &]
                          (println "Updated votes"))))))
