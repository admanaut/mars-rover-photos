(ns mars-rover-photos.api
  (:require
   [mars-rover-photos.conf :refer [config]]
   [mars-rover-photos.util :as util]
   [clojure.java.io :as io]
   [clojure.data.json :as json])
  (:import
   [java.net URL]))

(defn- photos-api-url
  "Builds the API endpoint for retrieving rover photos."
  [base-url rover camera {:keys [sol date]} api-key]
  (URL. (str base-url (name rover) "/photos?"
             (when-not (= camera :all) (str "camera=" (name camera)) )
             (or (and sol (str "&sol=" sol))
                 (and date (str "&earth_date=" date)))
             "&api_key=" api-key)))

(defn rovers-api-url
  "Builds the API endpoint for retrieving rover info."
  [base-url rover api-key]
  (URL. (str base-url (name rover) "?api_key=" api-key)))

(def json-io (util/io #(json/read (io/reader %) :key-fn keyword)))

(defn- get-sol-images
  [^String url]
  (loop [pages (iterate inc 1)
         r []]
    (let [srcs (->> (str url "&page=" (first pages))
                    (util/perform-io json-io)
                    util/try*
                    :photos
                     (map :img_src))]
      (if (seq srcs)
        (recur (rest pages) (into r srcs))
        r))))

(defn get-sols-images
  [rover camera sols]
  (let [base-url (get-in config [:api :base-url])
        api-key (get-in config [:api :key])]
    (loop [s sols
           r []]
      (if (seq s)
        (recur (rest s)
               (into r (get-sol-images (photos-api-url base-url rover camera {:sol (first s)} api-key))))
        r))))

(defn get-info
  [rover]
  (->> (rovers-api-url (get-in config [:api :base-url]) rover (get-in config [:api :key]))
       (util/perform-io json-io)
       util/try*
       :rover))
