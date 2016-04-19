(ns mars-rover-photos.gif
  (:require [clojure.java.io :as io]
            [gifclj.core :as gif]))

(defn generate
  "Generates an animated gif image from images and saves it under name."
  [name images & {:keys [delay loops lastdelay]
                  :or {delay 50 loops 0 lastdelay 50}}]
  (gif/write-gif name
                 (gif/imgs-from-files images)
                 :delay delay
                 :loops loops
                 :lastdelay lastdelay))
