(ns donkej.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub ::username :username)

(rf/reg-sub ::error-msg :error-msg)

(rf/reg-sub ::editing-talk :editing-talk)
(rf/reg-sub ::talks :talks)
