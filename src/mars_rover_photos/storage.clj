(ns mars-rover-photos.storage
  (:require
   [mars-rover-photos.conf :refer [config]]
   [clojure.java.io :as io]
   [clojure.string :as string]))

(defn- src->name
  [url]
  (last (string/split url  #"/")))

(defn- name->location
  [filename]
  (string/join "/" (butlast (string/split filename #"_"))))

(defn- name->path
  [filename]
  (str (name->location filename)  "/" filename))

(defn src->path
  ([src]
   (-> src src->name name->path))
  ([parent src]
   (str parent (-> src src->name name->path))))

(defn realise-path
  [& path]
  (let [file (apply io/file path)]
    (when-not (.exists file)
      (.mkdirs file))
    (apply str path)))

(defn- add-slash
  [^String path]
  (str path (when-not (.endsWith path "/") "/")))

(defn- download-image
  [src target]
  (let [filename (src->name src)
        path (realise-path target (name->location filename))
        to (io/file path filename)]
    (try
      (with-open [from (io/input-stream src)]
        (io/copy from to))
      to
      (catch java.io.IOException _))))

(defn download-images
  [imgs-src]
  (let [target (add-slash (get-in config [:resources :raw]))
        [existing to-download] ((juxt filter remove) #(->> % (src->path target) io/file .exists) imgs-src)
        downloaded (map #(download-image % target)
                        to-download)]
    (into downloaded (map #(src->path target %) existing))))
