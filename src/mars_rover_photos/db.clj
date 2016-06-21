(ns mars-rover-photos.db
  (:require
   [mars-rover-photos.conf :refer [config]]
   [mount.core :refer [defstate]]
   [clojure.java.jdbc :as jdbc]
   [java-jdbc.sql :as sql]
   [clj-time.core :as time]
   [clj-time.format :as tformat]
   [clj-time.coerce :as tcoerce]
   [clojure.string :as string])
  (:import
   [java.net URI]
   [java.sql Date]))

(defn- conn-string
  [config]
  (let [{:keys [protocol host port database]} (:database config)]
    (format "%s://%s:%s/%s" protocol host port database)))

(defn- conn-uri
  [config]
  (URI. (conn-string config)))

(defn- drop-schema
  [^java.sql.Connection conn]
  (doseq [tname [:photo :camera :rover]]
    (jdbc/db-do-commands
     {:connection conn}
     (jdbc/drop-table-ddl tname))))

(defn create-schema
  [^java.sql.Connection conn]
  (jdbc/db-do-commands
   {:connection conn}
   (jdbc/create-table-ddl :rover [[:id "BIGSERIAL PRIMARY KEY"]
                                  [:name "VARCHAR(20)"]
                                  [:landing_date :date]
                                  [:max_sol :int]
                                  [:max_date :date]
                                  [:total_photos :int]]))

  (jdbc/db-do-commands
   {:connection conn}
   (jdbc/create-table-ddl :camera [[:id "BIGSERIAL PRIMARY KEY"]
                                  [:rover :int "REFERENCES rover (id)"]
                                  [:name "VARCHAR(32)"]
                                  [:full_name :text]]))

  (jdbc/db-do-commands
   {:connection conn}
   (jdbc/create-table-ddl :photo [[:id "BIGSERIAL PRIMARY KEY"]
                                 [:rover :int "REFERENCES rover (id)"]
                                 [:camera :int "REFERENCES camera (id)"]
                                 [:sol :int]
                                 [:earth_date :date]
                                 [:img_src :json]]))

  (jdbc/db-do-commands
   {:connection conn}
   "CREATE INDEX ON camera ((lower(name)));")

  (jdbc/db-do-commands
   {:connection conn}
   "CREATE UNIQUE INDEX ON rover ((lower(name)));"))

(defn- new-connection
  ^java.sql.Connection
  [config]
  (jdbc/get-connection (conn-uri config)))

(defn- disconnect
  [^java.sql.Connection conn]
  (when-not (.isClosed conn)
    (.close conn)))

(defstate conn
  :start (new-connection config)
  :stop (disconnect conn))


(defn ->sql-date
  [^String date]
  (Date. (tcoerce/to-long (tformat/parse (tformat/formatters :date) date))))

(defn- substitute-fields
  [pred sub-fn entity & fields]
  (letfn [(sub-fields [ent fields]
            (if (seq fields)
              (let [field (first fields)
                    fval (get ent field)]
                (if (pred fval)
                  (recur (assoc ent field (sub-fn fval)) (rest fields))
                  (recur ent (rest fields))))
              ent))]
    (if (seq fields)
      (sub-fields entity fields)
      entity)))

(defn- add-dates
  [rover & fields]
  (apply substitute-fields string? ->sql-date rover fields))

(defn insert-rover
  [rover]
  (jdbc/insert! {:connection conn}
               :rover (add-dates (dissoc rover :id) :landing_date :max_date)))

(defn insert-camera
  [camera]
  (jdbc/insert! {:connection conn}
               :camera (dissoc camera :id)))

(defn insert-photo
  [photo]
  (jdbc/insert! {:connection conn}
               :photo (add-dates (dissoc photo :id) :earth_date)))

(defn select-rover
  ([name]
   (select-rover :name (string/capitalize name)))
  ([field value]
   (jdbc/query
    {:connection conn}
    (sql/select * :rover (sql/where {field value})))))

(defn- rover->id
  [^String name]
  (let [rvs (select-rover name)]
    (when (seq rvs)
      (:id (first rvs)))))

(defn select-cameras
  [rover]
  (if (string? rover)
    (select-cameras (rover->id rover))
    (jdbc/query
     {:connection conn}
     (sql/select * :camera (sql/where {:rover rover})))))


(comment

  "For interactive dev"

  (drop-schema conn)
  (create-schema conn)
  )
