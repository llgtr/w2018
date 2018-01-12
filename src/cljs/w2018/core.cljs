(ns w2018.core
  (:require [reagent.core :as reagent]
            [re-frame.core :as re-frame]
            [w2018.events :as events]
            [w2018.views :as views]
            [w2018.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [:initialize-db])
  (re-frame/dispatch [:request-observations])
  (re-frame/dispatch [:request-locations])
  (dev-setup)
  (mount-root))
