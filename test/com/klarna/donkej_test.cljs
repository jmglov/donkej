(ns com.klarna.donkej-test
    (:require [cljs.test :refer-macros [deftest is testing]]))

(deftest multiply-test
  (is (= 2 (* 1 2))))
