(ns com.klarna.donkej.events
  (:require [com.klarna.donkej.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::initialize-db
 (constantly db/default-db))

(rf/reg-event-db
 ::add-talk
 (fn [db [_ persist-fn title speakers url]]
   (let [talk {:id (str (random-uuid)), :title title, :speakers speakers, :url url}]
     (println "Adding talk:" talk)
     (persist-fn talk)
     (update db :talks conj talk))))

(rf/reg-event-db
 ::set-talks!
 (fn [db [_ talks]]
   (assoc db :talks (vec talks))))

(rf/reg-event-db
 ::vote-for-talk
 (fn [db [_ id username persist-fn]]
   (println "Looking for talk with id" id)
   (let [[i talk] (db/find-talk db id)]
     (if i
       (do
         (println "Found talk" talk)
         (let [update-fn (if (contains? (:votes talk) username) disj conj)
               talk* (update talk :votes update-fn username)]
           (persist-fn talk username)
           (assoc-in db [:talks i] talk*)))
       (do
         (println "Could not find talk with id" id)
         db)))))
