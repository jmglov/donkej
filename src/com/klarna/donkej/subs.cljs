(ns com.klarna.donkej.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub ::username :username)

(rf/reg-sub ::error-msg :error-msg)
(rf/reg-sub ::talks :talks)
