(ns mars-rover-photos.core
  (:require [clojure.java.io :as io]
            [clojure.data.json :as json]
            [clojure.string :as string]
            [gifclj.core :as gif])
  (:import [java.net URL])
  (:gen-class))

(def API_DEMO_KEY "DEMO_KEY")
(def storage "images/")

(def cameras
  [{:abbrev "FHAZ" :name "Front Hazard Avoidance Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev "RHAZ" :name "Rear Hazard Avoidance Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev "MAST" :name "Mast Camera" :rovers [:curiousity]}
   {:abbrev "CHEMCAM" :name "Chemistry and Camera Complex" :rovers [:curiousity]}
   {:abbrev "MAHLI" :name "Mars Hand Lens Imager" :rovers [:curiousity]}
   {:abbrev "MARDI"	:name "Mars Descent Image" :rovers [:curiousity]}
   {:abbrev "NAVCAM" :name "Navigation Camera" :rovers [:curiousity :opportunity :spirit]}
   {:abbrev "PANCAM" :name"Panoramic Camera" :rovers [:opportunity :spirit]}
   {:abbrev "MINITES" :name "Miniature Thermal Emission Spectrometer (Mini-TES)" :rovers [:opportunity :spirit]}])

(defn api-url
  "Returns the images endpoint of NASA's rover API."
  [rover camera {:keys [sol date]} api-key]
  (URL. (str "https://api.nasa.gov/mars-photos/api/v1/rovers/" (name rover) "/photos?"
             "camera=" (name camera)
             (or (and sol (str "&sol=" sol))
                 (and date (str "&earth_date=" date)))
             "&api_key=" api-key)))

(defn photos
  "Extracts 'photos' key from resp."
  [resp]
  (get resp "photos"))

(defn img-src
  "Extracts 'img_src' key from photo."
  [photo]
  (get photo "img_src"))

(defn get-photos
  "Returns a list of photo URIs taken by rover with camera on day."
  [rover camera day]
  (->
   (api-url rover camera day API_DEMO_KEY)
   io/reader
   json/read
   photos))

(defn img-name
  "Returns image name from url."
  [url]
  (last (string/split url  #"/")))

(defn download-image
  "Downloads image at src to to."
  [to src]
  (let [file (io/file (str to (img-name src)))]
    (with-open [from (io/input-stream src)]
      (io/copy from file))))


(defn download-rover-images
  "Downloads rover's images for day and camera."
  [rover camera day to]
  (try
    (->>
     (get-photos rover camera day)
     (map img-src)
     (map (partial download-image to)))
     (catch java.io.IOException _)))

(defn jpg?
  "Returns true if image ends in wither .JPG or .jpg, false otherwise."
  [image]
  (or (.endsWith image ".JPG")
      (.endsWith image ".jpg")))

(defn make-gif
  "Generates an animated gif image from images and saves it under name."
  [name images & {:keys [delay loops lastdelay]
                  :or {delay 50 loops 0 lastdelay 50}}]
  (gif/write-gif name
                 (gif/imgs-from-files images)
                 :delay delay
                 :loops loops
                 :lastdelay lastdelay))

(defn list-images
  "Returns a list of images (jpg default) found at source."
  ([source] (list-images jpg? source))
  ([f source]
   (filter f (seq (.list (clojure.java.io/file source))))))

(defn prefix-storage
  "Prefixes each item in strings with storage."
  [strings]
  (map (partial str storage) strings))



(comment

  ;; test
  (doall
   (pmap #(download-rover-images :curiosity :rhaz {:sol %} storage)
         (range 1200 1309)))


  (make-gif "mars.gif" (prefix-storage (list-images storage)))

  "run gif-name rover camera 100-150"

  ;;(api/get-photos :curiosity :mast :date "2015-6-3")
)
