(ns com.klarna.donkej.talks
  (:require [com.klarna.donkej.dynamodb :as dynamo]
            [com.klarna.donkej.events :as events]
            [re-frame.core :as rf]))

(def talks-table "hsi-tools-donkej-talks")
(defn load-talks! []
  (-> (dynamo/make-client)
      (dynamo/scan talks-table
                   (fn [{items :Items}]
                     (rf/dispatch [::events/add-talks items])))))
