(ns donkej.date
  (:require [clojure.string :as string]))

(defn ->date [date]
  (if (string? date)
    (js/Date. date)
    date))

(defn pad [digits width]
  (let [digits (str digits)]
    (->> digits
         (concat (repeat (- width (count digits)) \0))
         string/join)))

(defn now []
  (js/Date.))

(defn ->iso-date [date]
  (let [date (->date date)]
    (let [year (.getUTCFullYear date)
          month (pad (inc (.getUTCMonth date)) 2)
          day (pad (.getUTCDate date) 2)]
      (string/join "-" [year month day]))))

(defn ->iso-datetime [date]
  (when date
    (.toISOString date)))

(defn now-iso-datetime []
  (->iso-datetime (now)))
