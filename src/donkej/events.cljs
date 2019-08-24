(ns donkej.events
  (:require [clojure.string :as string]
            [donkej.aws :as aws]
            [donkej.date :as date]
            [donkej.db :as db]
            [re-frame.core :as rf]))

;; crappy tool
(rf/reg-event-db
 ::print-db
 (fn [db [_ key]]
   (if key
     (println "Current" key ":" (pr-str (get db key)))
     (println "Current db:" (pr-str db)))
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
 (fn [{:keys [editing-talk username] :as db} [_ persist-fn]]
   (if username
     (let [talk (merge editing-talk
                       {:id (str (random-uuid))
                        :submitted-by username
                        :date-submitted (date/now-iso-datetime)})
           missing-fields (filter #(empty? (editing-talk %)) [:title :speakers :url])]
       (if (empty? missing-fields)
         (do
           (println "Adding talk to view:" (pr-str talk))
           (persist-fn talk)
           (-> db
               (update :talks conj talk)
               (dissoc :editing-talk)
               (dissoc :error-msg)))
         (assoc db :error-msg (str "Talk is incomplete; missing "
                                   (string/join ", " (map name missing-fields))))))
     (assoc db :error-msg "You have to set your username!"))))

(rf/reg-event-db
 ::cancel-editing
 (fn [db [_]]
   (dissoc db :editing-talk)))

(rf/reg-event-db
 ::edit-talk
 (fn [db [_ id]]
   (let [[_ talk] (db/find-talk db id)]
     (assoc db :editing-talk talk))))

(rf/reg-event-db
 ::edit-field
 (fn [db [_ field value]]
   (assoc-in db [:editing-talk field] value)))

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
 ::update-talk
 (fn [{:keys [editing-talk] :as db} [_ persist-fn]]
   (let [missing-fields (filter #(empty? (editing-talk %)) [:title :speakers :url])
         [i _] (db/find-talk db (:id editing-talk))]
     (if (empty? missing-fields)
       (do
         (println "Updating talk:" (pr-str editing-talk))
         (persist-fn editing-talk)
         (-> db
             (assoc-in [:talks i] editing-talk)
             (dissoc :editing-talk)
             (dissoc :error-msg)))
       (assoc db :error-msg (str "Talk is incomplete; missing "
                                 (string/join ", " (map name missing-fields))))))))

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
