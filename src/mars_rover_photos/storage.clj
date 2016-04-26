(ns mars-rover-photos.storage
  (:require [clojure.java.io :as io]
            [clojure.string :as string]))

(defn- img-name
  "Returns image name from url."
  [url]
  (last
   (string/split url  #"/")))

(defn- download-image
  "Downloads image at src to target."
  [src target]
  (let [file (io/file target)]
    (try
      (with-open [from (io/input-stream src)]
        (io/copy from file))
      target
      (catch java.io.IOException _))))

(defn- add-slash
  [^String path]
  (str path (when-not (.endsWith path "/") "/")))

(defn- prepend-target
  [target img-name]
  (str (add-slash target) img-name))

(defn download-images
  "Downloads images to target.

  imgs-src - [collection] a collection of img srcs
  target - [string] path to a folder where images will be downloaded to
  "
  [imgs-src target]
  (map download-image
       imgs-src
       (map #(prepend-target target (img-name %))
            imgs-src)))
