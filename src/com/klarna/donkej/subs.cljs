(ns com.klarna.donkej.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub ::talks :talks)
