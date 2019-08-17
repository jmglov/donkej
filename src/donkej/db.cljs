(ns donkej.db)

(def default-db
  {:username nil

   :error-msg nil
   :talks []})

(defn find-talk
  "Returns index and talk, or nil if no talk with that ID exists"
  [{:keys [talks]} id]
  (println "Looking for talk with id" id)
  (if-let [talk (->> talks
                     (map-indexed (fn [i talk] (and (= id (:id talk)) [i talk])))
                     (some identity))]
    (do
      (println "Found talk:" (pr-str talk))
      talk)
    (println "Could not find talk with id" id)))
