(ns mars-rover-photos.conf
  (:require
   [mount.core :refer [defstate]]
   [clojure.edn :as edn]))

(defn load-config [path]
  (-> path
      slurp
      edn/read-string))

(defstate config
  :start (load-config "config/config.edn"))
