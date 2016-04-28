(ns mars-rover-photos.gif
  (:require [clojure.java.io :as io]
            [gifclj.core :as gif]))

(defn generate
  "Generates an animated gif image from images and saves it under name."
  [gif-name images & {:keys [delay loops lastdelay]
                      :or {delay 100 loops 0 lastdelay 100}}]
  (gif/write-gif gif-name
                 (gif/imgs-from-files images)
                 :delay delay
                 :loops loops
                 :lastdelay lastdelay))
