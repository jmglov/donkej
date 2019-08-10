(ns ^:figwheel-hooks com.klarna.donkej2
  (:require [com.klarna.donkej2.config :as config]
            [com.klarna.donkej2.events :as events]
            [com.klarna.donkej2.views :as views]
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

(defn ^:after-load on-reload []
  (rf/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
