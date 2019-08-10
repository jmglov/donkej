(ns com.klarna.donkej.events
  (:require [com.klarna.donkej.db :as db]
            [re-frame.core :as rf]))

(rf/reg-event-db
 ::initialize-db
 (constantly db/default-db))

(rf/reg-event-db
 ::add-talk
 (fn [db [_ title url]]
   (update db :talks conj {:title title, :url url})))
