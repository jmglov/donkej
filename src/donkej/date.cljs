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

(defn units->ms [num units]
  (case units
    :ms num
    :millis num
    :millisecond num
    :milliseconds num
    :s (* num 1000)
    :sec (units->ms num :s)
    :second (units->ms num :s)
    :seconds (units->ms num :s)
    :m (* (units->ms num :s) 60)
    :min (units->ms num :m)
    :minute (units->ms num :m)
    :minutes (units->ms num :m)
    :h (* (units->ms num :m) 60)
    :hour (units->ms num :h)
    :hours (units->ms num :h)
    :d (* (units->ms num :h) 24)
    :day (units->ms num :d)
    :days (units->ms num :d)))
