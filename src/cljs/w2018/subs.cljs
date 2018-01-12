(ns w2018.subs
  (:require [re-frame.core :as re-frame]
            [cljs-time.core :as time]
            [cljs-time.format :as tf]))

(re-frame/reg-sub
 :initialised?
 (fn [db _]
   (and (not (empty? db))
        (:loaded-observations? db)
        (:loaded-locations? db))))

(re-frame/reg-sub
 :locations
 (fn [db _]
   (:locations db)))

(re-frame/reg-sub
 :observations
 (fn [db _]
   (:observations db)))

(re-frame/reg-sub
 :selected-location
 (fn [db _]
   (:selected-location db)))

(re-frame/reg-sub
 :observation-modal?
 (fn [db _]
   (:observation-modal? db)))

(re-frame/reg-sub
 :invalid-input?
 (fn [db _]
   (:invalid-input? db)))

(defn match-location [selected-location observations]
  (filter #(= (:location %1) selected-location) observations))

(defn minus-day []
  (time/minus (time/now) (time/days 1)))

(defn last-24 [location-observations]
  (filter
   #(time/after? (tf/parse (:obsv_timestamp %1)) (minus-day))
   location-observations))

(re-frame/reg-sub
 :location-observations
 (fn [db _]
   (let [observations (:observations db)
         selected-location (:location (:selected-location db))]
     (match-location selected-location observations))))

(re-frame/reg-sub
 :latest-observation
 (fn [db _]
   (let [observations (:observations db)
         selected-location (:location (:selected-location db))
         location-observations (match-location selected-location observations)]
     (:temp (apply max-key :obsv_timestamp location-observations)))))

(re-frame/reg-sub
 :highest-temp-24h
 (fn [db _]
   (let [observations (:observations db)
         selected-location (:location (:selected-location db))
         location-observations (match-location selected-location observations)
         last-24 (last-24 location-observations)]
     (:temp (apply max-key :temp last-24)))))

(re-frame/reg-sub
 :lowest-temp-24h
 (fn [db _]
   (let [observations (:observations db)
         selected-location (:location (:selected-location db))
         location-observations (match-location selected-location observations)
         last-24 (last-24 location-observations)]
     (:temp (apply min-key :temp last-24)))))
