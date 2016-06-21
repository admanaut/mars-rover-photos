(ns mars-rover-photos.rover
  (:require
   [mars-rover-photos.db :as db]
   [mars-rover-photos.api :as api]
   [mars-rover-photos.util :as util]
   [mars-rover-photos.storage :as storage]))

(def rovers [:curiosity :opportunity :spirit])

(def cameras
  {:fhaz {:name "Front Hazard Avoidance Camera" :rovers [:curiosity :opportunity :spirit]}
   :rhaz {:name "Rear Hazard Avoidance Camera" :rovers [:curiosity :opportunity :spirit]}
   :mast {:name "Mast Camera" :rovers [:curiosity]}
   :chemcam {:name "Chemistry and Camera Complex" :rovers [:curiosity]}
   :mahli {:name "Mars Hand Lens Imager" :rovers [:curiosity]}
   :mardi {:name "Mars Descent Image" :rovers [:curiosity]}
   :navcam {:name "Navigation Camera" :rovers [:curiosity :opportunity :spirit]}
   :pancam {:name "Panoramic Camera" :rovers [:opportunity :spirit]}
   :minites {:name "Miniature Thermal Emission Spectrometer (Mini-TES)" :rovers [:opportunity :spirit]}})

(defn get-images
  [rover camera sol]
  (if (integer? sol)
    (get-images rover camera [sol (inc sol)])
    (let [[sol-from sol-to] sol
          images (api/get-sols-images rover camera (range sol-from sol-to))]
      (when (seq images)
        (storage/download-images images)))))

(defn get-info
  [rover]
  (if (keyword? rover)
    (get-info (name rover))
    ;; 1. check in the database
    (let [rv-db (db/select-rover rover)]
      (if (seq rv-db)
        (assoc (first rv-db) :cameras (db/select-cameras rover))
        ;; 2. get from api
        (when-let [rv-api (api/get-info rover)]
          ;; 3. store in db
          (when-let [rv-ins (util/try* (db/insert-rover (dissoc rv-api :cameras)))]
            (doseq [cam (:cameras rv-api)]
              (db/insert-camera (assoc cam :rover (:id (first rv-ins)))))
            (assoc (first rv-ins) :cameras (:cameras rv-api)))
          rv-api)))))
