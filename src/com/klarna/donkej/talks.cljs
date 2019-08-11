(ns com.klarna.donkej.talks
  (:require [com.klarna.donkej.dynamodb :as dynamo]
            [com.klarna.donkej.events :as events]
            [re-frame.core :as rf]))

(def talks-table "hsi-tools-donkej-talks")

(defn ->votes-model [votes]
  (.createSet (dynamo/make-client) (clj->js votes)))

(defn ->model [talk]
  (update talk :votes ->votes-model))

(defn ->talk [model]
  (update model :votes #(-> % (.-values) set)))

(defn add-talk! [talk]
  (println "Persisting talk to Dynamo:" (pr-str talk))
  (-> (dynamo/make-client)
      (dynamo/put! talks-table (->model talk) println)))

(defn load-talks! []
  (-> (dynamo/make-client)
      (dynamo/scan talks-table
                   (fn [{items :Items}]
                     (println "Loaded talks:" (pr-str items))
                     (rf/dispatch [::events/set-talks! (map ->talk items)])))))

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
