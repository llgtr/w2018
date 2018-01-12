(ns w2018.server
  (:require [w2018.handler :refer [handler]]
            [w2018.core :as core]
            [config.core :refer [env]]
            [ring.adapter.jetty :refer [run-jetty]])
  (:gen-class))

(defn -main [& args]
  (let [port (Integer/parseInt (or (env :port) "3000"))]
    (do
      (if (not (core/migrated?))
        (core/migrate))
      (run-jetty handler {:port port :join? false}))))
