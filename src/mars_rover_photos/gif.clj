(ns mars-rover-photos.gif
  (:require [clojure.java.io :as io]
            [gifclj.core :as gif]))

(defn- tmp-gif-name
  []
  (str (quot (System/currentTimeMillis) 1000) ".gif"))

(defn generate
  "Generates an animated gif image from images and saves it under name."
  [path images & {:keys [delay loops lastdelay]
                  :or {delay 50 loops 0 lastdelay 50}}]
  (let [gif-fullname (str path (tmp-gif-name))
        result (gif/write-gif gif-fullname
                              (gif/imgs-from-files images)
                              :delay delay
                              :loops loops
                              :lastdelay lastdelay)]
    (when result
      gif-fullname)))
