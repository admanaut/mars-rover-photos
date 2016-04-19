(ns mars-rover-photos.storage
  (:require [clojure.java.io :as io]
            [gifclj.core :as gif]))

(def path "images/")

(defn jpg?
  "Returns true if image ends in wither .JPG or .jpg, false otherwise."
  [image]
  (or (.endsWith image ".JPG")
      (.endsWith image ".jpg")))

(defn list-images
  "Returns a list of images (jpg default) found at source."
  ([] (list-images jpg? path))
  ([f path]
   (map (partial str path)
        (filter f (seq (.list (io/file path)))))))
