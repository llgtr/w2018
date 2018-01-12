(ns w2018.core
  (:require [clojure.java.jdbc :as sql]
            [clj-time.format :as f]
            [clj-time.core :as t]
            [clj-time.coerce :as tc]))

;; -- validation --

(defn valid-input? [input]
  (and (some? input)
       (seq input)))

(defn parse-number [input]
  (try
    (bigdec input)
    (catch Exception e))) ; Return nil if can't coerce

(defn valid-num? [num lo hi]
  (and (valid-input? num)
       (let [n (parse-number num)]
         (and (some? n)
              (> n lo)
              (< n hi)))))

(defn valid-temp? [temp]
  (valid-num? temp -100 100))

;; ISO 8601
(defn valid-datetime? [datetime]
  (and (valid-input? datetime)
       (let [dt (f/parse datetime)]
         (if (some? dt)
           (t/before? dt (t/now))))))

(defn valid-lat? [lat]
  (valid-num? lat -90 90))

(defn valid-lng? [lng]
  (valid-num? lng -180 180))

;; -- db --

(def dbspec (or (System/getenv "DATABASE_URL")
                "postgresql://postgres:postgres@localhost:5432/postgres"))

(defn migrated? []
  (-> (sql/query dbspec
                 [(str "SELECT count(*) FROM information_schema.tables" " "
                       "WHERE table_name='observations' or table_name='locations'")])
      first :count pos?))

(defn migrate []
  (when (not (migrated?))
    (println "Migrating...")
    (do
      (sql/db-do-commands dbspec
                          [(sql/create-table-ddl
                            :locations
                            [[:location :varchar "PRIMARY KEY"]
                             [:lat "numeric" "NOT NULL"]
                             [:lng "numeric" "NOT NULL"]])
                           (sql/create-table-ddl
                            :observations
                            [[:id :serial "PRIMARY KEY"]
                             [:temp "numeric" "NOT NULL"]
                             [:obsv_timestamp :timestamp "NOT NULL"]
                             [:location :varchar "NOT NULL REFERENCES locations(location)"]])])
      (sql/insert-multi! dbspec :locations
                         [:location :lat :lng]
                         [["Tokio" 35.6584421 139.7328635]
                          ["Helsinki" 60.1697530 24.9490830]
                          ["New York" 40.7406905 -73.9938438]
                          ["Amsterdam" 52.3650691 4.9040238]
                          ["Dubai" 25.092535 55.1562243]]))
    (println "Ready!")))

(defn get-observations []
  (sql/query dbspec "SELECT * FROM observations ORDER BY obsv_timestamp desc"))

(defn get-observation [id]
  (sql/query dbspec (str "SELECT * FROM observations WHERE id=" id)))

(defn get-locations []
  (sql/query dbspec "SELECT * FROM locations"))

(defn create-observation [temp datetime location]
  (sql/insert! dbspec :observations {:temp (bigdec temp)
                                     :obsv_timestamp (tc/to-sql-time (f/parse datetime))
                                     :location location}))

(defn create-location [name lat lng]
  (sql/insert! dbspec :locations {:location name
                                  :lat (bigdec lat)
                                  :lng (bigdec lng)}))
