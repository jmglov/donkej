(ns com.klarna.donkej.db)

(def default-db
  {:talks []})

(defn find-talk
  "Returns index and talk, or nil if no talk with that ID exists"
  [{:keys [talks]} id]
  (->> talks
       (map-indexed (fn [i talk] (and (= id (:id talk)) [i talk])))
       (some identity)))
