(ns mars-rover-photos.rover
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json])
  (:import [java.net URL]))

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
  "Builds the API endpoint for retrieving rover photos.

  rover   - [keyword] one of defined rovers
  camera  - [keyword] either one of defined cameras or :all
  day     - [map] with either :date or :sol
  api-key - [string] NASA's rover API key
  "
  [rover camera {:keys [sol date]} api-key]
  (URL. (str api-base-url (name rover) "/photos?"
             (when-not (= camera :all) (str "camera=" (name camera)) )
             (or (and sol (str "&sol=" sol))
                 (and date (str "&earth_date=" date)))
             "&api_key=" api-key)))

(defn rovers-api-url
  "Builds the API endpoint for retrieving rover info.

  rover   - [keyword] one of defined rovers
  api-key - [string] NASA's rover API key
  "
  [rover api-key]
  (URL. (str api-base-url (name rover) "?api_key=DEMO_KEY")))

(defn photos
  "Extracts 'photos' key from m.

  m - [map]
  "
  [m]
  (get m "photos"))

(defn img-src
  "Extracts 'img_src' key from m.

  m - [map]
  "
  [m]
  (get m "img_src"))

(defn rover
  "Extracts 'rover' key from m.

  m - [map]
  "
  [m]
  (get m "rover"))

(defn- get-json
  [url]
  (try
    (-> url
        io/reader
        json/read)
    (catch java.io.IOException _ nil)))

(defn- photos-src
  [photos]
  (when-not (nil? photos)
    (map img-src photos)))

(defn get-images
  "Returns a list of photo URIs taken by rover with camera on day.

  rover   - [keyword] one of defined rovers
  camera  - [keyword] either one of defined cameras or :all
  day     - [map] with either :date or :sol
  api-key - [string &optional] NASA's rover API key, default to DEMO_KEY
  "
  ([rover camera day] (get-images rover camera day API_DEMO_KEY))
  ([rover camera day api-key]
   (->> (photos-api-url rover camera day api-key)
        get-json
        photos
        photos-src
       )))

(defn get-info
  "Returns a map of information about rover, like landing_date max_sol etc.

  rover   - [keyword] one of defined rovers
  api-key - [string &optional] NASA's rover API key, default to DEMO_KEY
  "
  ([r] (get-info r API_DEMO_KEY))
  ([r api-key]
   (-> (rovers-api-url r api-key)
       io/reader
       json/read
       rover)))

(comment
  (defn get-info
    []
    [{"id" 5, "name" "Curiosity", "landing_date" "2012-08-06", "max_sol" 1315, "max_date" "2016-04-18", "total_photos" 250165, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "MAST", "full_name" "Mast Camera"} {"name" "CHEMCAM", "full_name" "Chemistry and Camera Complex"} {"name" "MAHLI", "full_name" "Mars Hand Lens Imager"} {"name" "MARDI", "full_name" "Mars Descent Imager"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]} {"id" 6, "name" "Opportunity", "landing_date" "2004-01-25", "max_sol" 4348, "max_date" "2016-04-18", "total_photos" 179636, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "PANCAM", "full_name" "Panoramic Camera"} {"name" "MINITES", "full_name" "Miniature Thermal Emission Spectrometer (Mini-TES)"} {"name" "ENTRY", "full_name" "Entry, Descent, and Landing Camera"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]} {"id" 7, "name" "Spirit", "landing_date" "2004-01-04", "max_sol" 2208, "max_date" "2010-03-21", "total_photos" 124550, "cameras" [{"name" "FHAZ", "full_name" "Front Hazard Avoidance Camera"} {"name" "NAVCAM", "full_name" "Navigation Camera"} {"name" "PANCAM", "full_name" "Panoramic Camera"} {"name" "MINITES", "full_name" "Miniature Thermal Emission Spectrometer (Mini-TES)"} {"name" "ENTRY", "full_name" "Entry, Descent, and Landing Camera"} {"name" "RHAZ", "full_name" "Rear Hazard Avoidance Camera"}]}]
    ))
