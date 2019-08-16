(ns com.klarna.donkej.aws)

(def default-region "eu-west-1")

(defn refresh-credentials!
  ([creds]
   (-> (.-config js/AWS)
       (.update (clj->js creds))))
  ([access-key secret-key]
   (refresh-credentials! default-region access-key secret-key))
  ([region access-key secret-key]
   (refresh-credentials! {:region region
                          :accessKeyId access-key
                          :secretAccessKey secret-key})))
