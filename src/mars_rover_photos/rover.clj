(ns mars-rover-photos.rover
  (:require
   [mars-rover-photos.util :as ut]
   [clojure.java.io :as io]
   [clojure.data.json :as json])
  (:import
   [java.net URL]))

(def API_DEMO_KEY "DEMO_KEY")

(def rovers [:curiosity :opportunity :spirit])

(def cameras
  [{:abbrev :fhaz :name "Front Hazard Avoidance Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev :rhaz :name "Rear Hazard Avoidance Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev :mast :name "Mast Camera" :rovers [:curiousity]}
   {:abbrev :chemcam :name "Chemistry and Camera Complex" :rovers [:curiousity]}
   {:abbrev :mahli :name "Mars Hand Lens Imager" :rovers [:curiousity]}
   {:abbrev :mardi	:name "Mars Descent Image" :rovers [:curiousity]}
   {:abbrev :navcam :name "Navigation Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev :pancam :name"Panoramic Camera" :rovers [:opportunity :spirit]}
   {:abbrev :minites :name "Miniature Thermal Emission Spectrometer (Mini-TES)" :rovers [:opportunity :spirit]}])

(def api-base-url "https://api.nasa.gov/mars-photos/api/v1/rovers/")

(defn photos-api-url
  "Builds the API endpoint for retrieving rover photos."
  [rover camera {:keys [sol date]} api-key]
  (URL. (str api-base-url (name rover) "/photos?"
             (when-not (= camera :all) (str "camera=" (name camera)) )
             (or (and sol (str "&sol=" sol))
                 (and date (str "&earth_date=" date)))
             "&api_key=" api-key)))

(defn rovers-api-url
  "Builds the API endpoint for retrieving rover info."
  [base-url rover api-key]
  (URL. (str base-url (name rover) "?api_key=" api-key)))

(defn mkey
  [k]
  (fn [m] (get m k)))

(def img-src (mkey "img_src"))
(def krover (mkey "rover"))
(def photos (mkey "photos"))
(def imgs-src #(map img-src %))

(def json-io (ut/io (comp json/read io/reader)))

(defn- get-sol-images
  [^String url]
  (->> url
       (ut/perform-io json-io)
       ut/try*
       photos
       imgs-src))

(defn get-images
  "Returns a list of image URIs taken by rover with camera on day."
  ([rover camera day]
   (get-images rover camera day API_DEMO_KEY))
  ([rover camera {:keys [sol date] :as day} api-key]
   (let [[sol-from sol-to] sol]
     (if (and sol-from sol-to (<= sol-from sol-to))
       (loop [s (range sol-from sol-to)
              r []]
         (if (seq s)
           (recur (rest s) (into r (get-sol-images (photos-api-url rover camera {:sol (first s)} api-key))))
           r))

       (get-sol-images (photos-api-url rover camera {:sol sol-from} api-key))))))

(defn get-info
  "Returns a map of rover details, like landing_date, max_sol etc."
  ([rover]
   (get-info rover API_DEMO_KEY))
  ([rover api-key]
   (->> (rovers-api-url api-base-url rover api-key)
        (ut/perform-io json-io)
        ut/try*
        krover)))


(comment)
(defn get-info
  [rover]
  (get
   {:curiosity {"id" 5, "name" "Curiosity", "landing_date" "2012-08-06", "max_sol" 1315, "max_date" "2016-04-18", "total_photos" 250165, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "MAST", "full_name" "Mast Camera"} {"name" "CHEMCAM", "full_name" "Chemistry and Camera Complex"} {"name" "MAHLI", "full_name" "Mars Hand Lens Imager"} {"name" "MARDI", "full_name" "Mars Descent Imager"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]}
    :opportunity {"id" 6, "name" "Opportunity", "landing_date" "2004-01-25", "max_sol" 4348, "max_date" "2016-04-18", "total_photos" 179636, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "PANCAM", "full_name" "Panoramic Camera"} {"name" "MINITES", "full_name" "Miniature Thermal Emission Spectrometer (Mini-TES)"} {"name" "ENTRY", "full_name" "Entry, Descent, and Landing Camera"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]}
    :spirit {"id" 7, "name" "Spirit", "landing_date" "2004-01-04", "max_sol" 2208, "max_date" "2010-03-21", "total_photos" 124550, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "PANCAM", "full_name" "Panoramic Camera"} {"name" "MINITES", "full_name" "Miniature Thermal Emission Spectrometer (Mini-TES)"} {"name" "ENTRY", "full_name" "Entry, Descent, and Landing Camera"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]}}
   rover))
