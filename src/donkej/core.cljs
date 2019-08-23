(ns ^:figwheel-hooks donkej.core
  (:require [donkej.aws :as aws]
            [donkej.config :as config]
            [donkej.events :as events]
            [donkej.talks :as talks]
            [donkej.views :as views]
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
  (rf/dispatch-sync [::events/initialize-db])
  (rf/dispatch-sync [::events/set-error-msg "Please set your AWS credentials"])
  (dev-setup)
  (mount-root))

(defn ^:after-load on-reload []
  (println "Reloading")
  (mount-root))
