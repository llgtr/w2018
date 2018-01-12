(ns w2018.handler
  (:require [w2018.core :as core]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.util.response :refer [resource-response redirect]]
            [ring.middleware.reload :refer [wrap-reload]]
            [ring.middleware.params :refer [wrap-params]]
            [cheshire.core :as cheshire]))

(defn json-response [data & [status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json; charset=utf-8"}
   :body (cheshire/generate-string data)})

(defroutes api-routes
  (context "/api" []
           (GET "/observations" []
                (json-response (core/get-observations)))
           (GET "/observations/:id{[0-9]+}" [id]
                (json-response (core/get-observation id)))
           (GET "/locations" []
                (json-response (core/get-locations)))
           (POST "/observations" [temp datetime location]
                 (if (and (core/valid-temp? temp)
                          (core/valid-datetime? datetime))
                   (core/create-observation temp datetime location)
                   {:status 403 :headers {"Content-Type" "text/html"} :body "invalid request"}))
           (POST "/locations" [name lat lng]
                 (do
                   (println name)
                   (if (and (core/valid-input? name)
                            (core/valid-lat? lat)
                            (core/valid-lng? lng))
                     (core/create-location name lat lng)
                     {:status 403 :headers {"Content-Type" "text/html"} :body "invalid request"})))))

(defroutes site-routes
  (GET "/" [] (resource-response "index.html" {:root "public"}))
  (route/resources "/")
  (route/not-found "<h1>404: Not found</h1>"))

(def handler (-> (routes api-routes site-routes) wrap-params))
