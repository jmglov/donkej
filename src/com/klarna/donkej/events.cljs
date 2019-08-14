(ns com.klarna.donkej.events
  (:require [com.klarna.donkej.date :as date]
            [com.klarna.donkej.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::initialize-db
 (constantly db/default-db))

(rf/reg-event-db
 ::add-talk
 (fn [db [_ persist-fn title speakers url]]
   (let [talk {:id (str (random-uuid))
               :title title
               :speakers speakers
               :url url
               :date-submitted (date/now-iso-datetime)}]
     (println "Adding talk to view:" (pr-str talk))
     (persist-fn talk)
     (update db :talks conj talk))))

(rf/reg-event-db
 ::mark-watched
 (fn [db [_ persist-fn id]]
   (let [[i talk] (db/find-talk db id)]
     (if i
       (let [talk* (assoc talk :date-watched (date/now-iso-datetime))]
         (persist-fn talk*)
         (assoc-in db [:talks i] talk*))
       db))))

(rf/reg-event-db
 ::set-talks!
 (fn [db [_ talks]]
   (assoc db :talks (vec talks))))

(rf/reg-event-db
 ::vote-for-talk
 (fn [db [_ persist-fn id username]]
   (let [[i talk] (db/find-talk db id)]
     (if i
       (let [update-fn (if (contains? (:votes talk) username) disj conj)
             talk* (update talk :votes update-fn username)]
         (persist-fn talk username)
         (assoc-in db [:talks i] talk*))
       db))))
