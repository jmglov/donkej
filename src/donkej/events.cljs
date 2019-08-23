(ns donkej.events
  (:require [donkej.date :as date]
            [donkej.db :as db]
            [re-frame.core :as rf]
            [donkej.aws :as aws]))

;; crappy tool
(rf/reg-event-db
 ::print-db
 (fn [db & _]
   (println "Current db:" (pr-str db))
   db))

(rf/reg-event-db
 ::initialize-db
 (constantly db/default-db))

(rf/reg-event-db
 ::refresh-aws-credentials!
 (fn [db [_ & args]]
   (apply aws/refresh-credentials! args)
   db))

(rf/reg-event-db
 ::set-username
 (fn [db [_ username]]
   (assoc db :username username)))

(rf/reg-event-db
 ::clear-error-msg
 (fn [db [_ msg]]
   (assoc db :error-msg nil)))

(rf/reg-event-db
 ::set-error-msg
 (fn [db [_ msg]]
   (assoc db :error-msg msg)))

(rf/reg-event-db
 ::add-talk
 (fn [{:keys [username] :as db} [_ persist-fn title speakers url]]
   (if username
     (let [talk {:id (str (random-uuid))
                 :title title
                 :speakers speakers
                 :submitted-by username
                 :url url
                 :date-submitted (date/now-iso-datetime)}]
       (println "Adding talk to view:" (pr-str talk))
       (persist-fn talk)
       (update db :talks conj talk))
     (assoc db :error-msg "You have to set your username!"))))

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
 ::get-talks
 (fn [{:keys [talks] :as db} [_ a]]
   (reset! a talks)
   db))

(rf/reg-event-db
 ::set-talks
 (fn [db [_ talks]]
   (println "Set talks to" (pr-str talks))
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
