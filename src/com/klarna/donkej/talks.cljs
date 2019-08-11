(ns com.klarna.donkej.talks
  (:require [com.klarna.donkej.dynamodb :as dynamo]
            [com.klarna.donkej.events :as events]
            [re-frame.core :as rf]))

(def talks-table "hsi-tools-donkej-talks")

(defn add-talk! [talk]
  (println "Persisting talk to Dynamo:" talk)
  (-> (dynamo/make-client)
      (dynamo/put! talks-table talk println)))

(defn load-talks! []
  (-> (dynamo/make-client)
      (dynamo/scan talks-table
                   (fn [{items :Items}]
                     (println "Loaded talks:" items)
                     (rf/dispatch [::events/set-talks! items])))))
