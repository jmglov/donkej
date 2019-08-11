(ns com.klarna.donkej.aws)

(defn refresh-credentials!
  ([region access-key secret-key]
   (refresh-credentials! {:region region
                          :accessKeyId access-key
                          :secretAccessKey secret-key}))
  ([creds]
   (-> (.-config js/AWS)
       (.update (clj->js creds)))))
