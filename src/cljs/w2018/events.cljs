(ns w2018.events
  (:require [re-frame.core :as re-frame]
            [w2018.db :as db]
            [day8.re-frame.http-fx]
            [ajax.core :as ajax]
            [clojure.string :as str]))

;; -- db event handlers --

(re-frame/reg-event-db
 :initialize-db
 (fn [_ _]
   db/app-db))

(re-frame/reg-event-db
 :observations-response
 (fn [db [_ response]]
   (-> db
       (assoc :observations (js->clj response))
       (assoc :loaded-observations? true))))

(re-frame/reg-event-db
 :locations-response
 (fn [db [_ response]]
   (-> db
       (assoc :locations (js->clj response))
       (assoc :selected-location (rand-nth (js->clj response))) ; More interesting this way
       (assoc :loaded-locations? true))))

(re-frame/reg-event-db
 :bad-response
 (fn [db [_ response]]
   (assoc db :loading? true)))

(re-frame/reg-event-db
 :invalid-input
 (fn [db _]
   (assoc db :invalid-input? true)))

(re-frame/reg-event-db
 :change-current-location
 (fn [db [_ new]]
   (assoc db :selected-location new)))

(re-frame/reg-event-db
 :toggle-modal
 (fn [db [_ modal]]
   (update-in db [modal] not)))

(re-frame/reg-event-db
 :toggle-invalid
 (fn [db [_ bool]]
   (assoc db :invalid-input? bool)))

; -- fx event handlers --

(def api-url (-> js/window .-location .-href (str "api")))

(defn endpoint [& params]
  (str/join "/" (concat [api-url] params)))

(re-frame/reg-event-fx
 :request-observations
 (fn [state _]
   {:http-xhrio {:method :get
                 :uri (endpoint "observations")
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:observations-response]
                 :on-failure [:bad-response]}}))


(re-frame/reg-event-fx
 :request-locations
 (fn [state _]
   {:http-xhrio {:method :get
                 :uri (endpoint "locations")
                 :format (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success [:locations-response]
                 :on-failure [:bad-response]}}))


(re-frame/reg-event-fx
 :add-location
 (fn [state [_ location lat lng]]
   {:http-xhrio {:method :post
                 :uri (endpoint "locations")
                 :params {:name location :lat lat :lng lng}
                 :format (ajax/url-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:location-added]
                 :on-failure [:invalid-input]}}))

(re-frame/reg-event-fx
 :add-observation
 (fn [state [_ temp date location]]
   {:http-xhrio {:method :post
                 :uri (endpoint "observations")
                 :params {:temp temp :datetime date :location location}
                 :format (ajax/url-request-format)
                 :response-format (ajax/text-response-format)
                 :on-success [:observation-added]
                 :on-failure [:invalid-input]}}))

(re-frame/reg-event-fx
 :observation-added
 (fn [state _]
   {:db (assoc (:db state) :observation-modal? false)
    :dispatch [:request-observations]}))
