(ns com.klarna.donkej.dynamodb)

(defn make-client []
  (new (-> js/AWS (.-DynamoDB) (.-DocumentClient))))

(defn error-handler [msg err]
  (println msg (js->clj err)))

(defn put!
  ([client table item on-success]
   (put! client table item on-success
         (partial error-handler (str "Error putting item in table " table))))
  ([client table item on-success on-error]
   (.put client
         (clj->js {:TableName table
                   :Item item})
         (fn [err data]
           (if err
             (on-error err)
             (on-success (js->clj data :keywordize-keys true)))))))

(defn scan
  ([client table on-success]
   (scan client table on-success
         (partial error-handler (str "Error scanning table " table))))
  ([client table on-success on-error]
   (.scan client
          (clj->js {:TableName table})
          (fn [err data]
            (if err
              (on-error err)
              (on-success (js->clj data :keywordize-keys true)))))))
