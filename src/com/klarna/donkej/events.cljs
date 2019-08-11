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
   (assoc db :talks talks)))
