(ns mars-rover-photos.gif
  (:require
      [mars-rover-photos.conf :refer [config]]
      [gifclj.core :as gif]
      [clojure.java.io :as io]))

(defn generate
  [images & {:keys [delay loops lastdelay]
             :or {delay 100 loops 0 lastdelay 100}}]

  (let [gif-path (str (get-in config [:resources :pub])
                      (hash (sort images))
                      ".gif")]
    (if (.exists (io/as-file gif-path))
      gif-path
      (gif/write-gif gif-path
                     (gif/imgs-from-files images)
                     :delay delay
                     :loops loops
                     :lastdelay lastdelay))))
