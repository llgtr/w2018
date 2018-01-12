(ns w2018.views
  (:require [re-frame.core :as re-frame]
            [reagent.core :as reagent]
            [w2018.events :as events]
            [w2018.subs :as subs]
            [cljs-time.format :as tf]))

;; https://github.com/Day8/re-frame/blob/master/docs/Using-Stateful-JS-Components.md
(defn home-render []
  (let [osmmap (atom nil)
        options (clj->js {:attribution "Map data &copy; [<a href='http://openstreetmap.org'>OpenStreetMap</a>]"
                          :minZoom 3
                          :maxZoom 9})
        update (fn [comp]
                 (let [{:keys [lat lng]} (-> (reagent/props comp)
                                             first
                                             first)]
                   (.panTo @osmmap #js [lat lng] 5)))]
    
    (reagent/create-class {:reagent-render (fn []
                                             [:div#map {:style {:flex "6"}}])
                           :component-did-mount
                           (fn [comp]
                             (let [curr (-> (reagent/props comp)
                                            first
                                            first)
                                   locs (-> (reagent/props comp)
                                            first
                                            second)
                                   map (.setView (.map js/L "map") #js [(:lat curr) (:lng curr)] 5)]
                               (do
                                 (.addTo (.tileLayer js/L "http://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" options) map)
                                 (doseq [loc locs]
                                   (let [lat (:lat loc)
                                         lng (:lng loc)]
                                     (.addTo (.marker js/L #js [lat lng]) map)))
                                 (reset! osmmap map))))
                           :component-did-update update
                           :display-name "home-render"})))

(defn map-outer []
  (let [selected-location (re-frame/subscribe [:selected-location])
        locations (re-frame/subscribe [:locations])]
    (fn []
      [home-render {@selected-location @locations}])))

;; -- Locations and buttons for adding observations/(locations) --

(defn location-button [location]
  [:li.btn.btn-location.disable-select {:on-click
                                        #(re-frame/dispatch [:change-current-location location])}
   (:location location)
   [:div.coordinates (:lat location) ", " (:lng location)]])

(defn modal-buttons []
  [:div.flex-row
   [:div.btn-borderless.padded.disable-select
    {:on-click #(re-frame/dispatch [:toggle-modal :observation-modal?])}
    "Add new observation"]])

(defn locations-list []
  (let [locations (re-frame/subscribe [:locations])]
    [:div.flex-col.card.locations-main-container
     [:div
      [:h2.locations-title "Locations"]
      [:ul
       (for [loc @locations]
         ^{:key (:location loc)}
         [location-button loc])]
      [modal-buttons]]]))

;; -- Observation statistics --

(defn temperature-stat [title temp]
  [:div.readout (str title)
   (if (not temp)
     [:span.temp "N/A"]
     [:span.temp (str temp "°C")])])

(defn temperatures-container []
  (let [latest (re-frame/subscribe [:latest-observation])
        low (re-frame/subscribe [:lowest-temp-24h])
        high (re-frame/subscribe [:highest-temp-24h])]
    [:div.ticker.card.flex-row
     [temperature-stat "Latest observation: " @latest]
     [temperature-stat "24h low: " @low]
     [temperature-stat "24h high: " @high]]))

;; -- Observations --

(defn observation [obsv]
  [:li.observation-li
   [:span.timestamp
    (tf/unparse (tf/formatter "dd.MM.yyyy HH:mm") (tf/parse (:obsv_timestamp obsv)))]
   [:span (str "  " (:temp obsv) "°C")]])

(defn observations-container []
  (let [observations (re-frame/subscribe [:location-observations])]
    (if (seq @observations)
      [:div.observations-main.card.flex-col
       [:ul
        (for [obsv @observations]
          ^{:key (:id obsv)}
          [observation obsv])]])))

;; -- Modals --

(defn observation-modal []
  (let [handler (fn [e]
                  (let [form (-> e .-target .-children)
                        temp (-> form .-temp .-value)
                        date (-> form .-datetime .-value)
                        location (-> form .-location .-value)]
                    (do
                      (.preventDefault e)
                      (re-frame/dispatch [:add-observation temp date location]))))
        invalid? (re-frame/subscribe [:invalid-input?])]
    [:div.modal.card
     [:form.flex-col {:on-submit handler}
      (if @invalid?
        [:div.invalid "Invalid input"])
      [:input {:type "text" :name "temp" :placeholder "Temperature"}]
      [:input {:type "text" :name "datetime" :placeholder "yyyy-MM-ddTHH:mm (GMT)"}]
      [:input {:type "text" :name "location" :placeholder "Location name"}]
      [:input.btn-borderless {:type "submit" :value "Submit"}]]]))

(defn dimmer []
  [:div.dimmer
   {:on-click #(re-frame/dispatch [:toggle-modal :observation-modal?])}])

;; -- Main --

(defn main-panel []
  (let [ready? (re-frame/subscribe [:initialised?])
        observation-modal? (re-frame/subscribe [:observation-modal?])]
    (if-not @ready?
      [:h1 "Initialising"]
      [:div.main.flex-row
       (if @observation-modal?
         [observation-modal])
       (if @observation-modal?
         [dimmer])
       [:div.flex-col {:style {:flex "1" :position "relative"}}
        [locations-list]
        [temperatures-container]
        [observations-container]
        [map-outer]]])))
