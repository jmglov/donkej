(ns com.klarna.donkej.dynamodb)

(defn make-client []
  (new (-> js/AWS (.-DynamoDB) (.-DocumentClient))))

(defn scan [client table f]
  (.scan client
         (clj->js {:TableName table})
         (fn [err data]
           (if err
             (println "Error:" (js->clj err))
             (f (js->clj data :keywordize-keys true))))))
