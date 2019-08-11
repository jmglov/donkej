(ns ^:figwheel-hooks com.klarna.donkej
  (:require [com.klarna.donkej.aws :as aws]
            [com.klarna.donkej.config :as config]
            [com.klarna.donkej.creds :refer [creds]]  ; temporary hack until we have Cognito
            [com.klarna.donkej.events :as events]
            [com.klarna.donkej.talks :as talks]
            [com.klarna.donkej.views :as views]
            [goog.dom :as gdom]
            [reagent.core :as reagent]
            [re-frame.core :as rf]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (rf/clear-subscription-cache!)
  (reagent/render [views/main-panel] (gdom/getElement "app")))

(defn ^:export init []
  (println "init called")
  (aws/refresh-credentials! creds)
  (rf/dispatch-sync [::events/initialize-db])
  (talks/load-talks!)
  (dev-setup)
  (mount-root))

(defn ^:after-load on-reload []
  (println "Reloading")
  (mount-root))
