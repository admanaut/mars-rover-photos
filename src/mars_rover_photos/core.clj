(ns mars-rover-photos.core
  (:require [mars-rover-photos.rover :as rover]
            [mars-rover-photos.gif :as gif]
            [mars-rover-photos.storage :as storage]
            [mars-rover-photos.view :as view]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn fetch-gif
  [rover camera sol]
  (let [gif-filename (str (quot (System/currentTimeMillis) 1000) ".gif")
        gif-path (str "public/" gif-filename)]

    (rover/download-images (keyword rover) (keyword camera) {:sol (Integer. sol)} storage/path)
    (gif/generate gif-path (storage/list-images))

    {:status 200
     :headers {"Content-Type" "text/html"}
     :body (str "<img src='/" gif-path "' />") }
    ))

(defroutes app
  (GET "/" []
       (view/index (rover/get-info))
       ;(view/index (reduce (fn [rs r] (conj rs (rover/get-info r)) ) [] rover/rovers))
       )

  (GET "/gif" { {:keys [rover camera sol]} :params} (str rover camera sol)
       (fetch-gif rover camera sol)
       )

  (route/files "/public/")
  (route/not-found "Page not found")
  )


(def application (site #'app))

(defn start
  [port]
  (jetty/run-jetty application {:port port
                                :join? false}))

(defn -main [& [port]]
  (let [port (Integer. (or port 5000))]
    (start port)))

;; For interactive development:
(comment
  (def server (-main))
  (.stop server)
  (.start server)

  (rover/download-images :curiosity :any {:sol 1000} "images/")

  (fetch-gif "curiosity" "any" 1000)
  )

(comment

  (rover/download-images :curiosity :any {:sol 1000} img-storage)
  (gif/generate "test.gif" (storage/list-images))


  ;; test
  (doall
   (pmap #(download-rover-images :curiosity :rhaz {:sol %} storage)
         (range 1200 1309)))


  (make-gif "mars.gif" (prefix-storage (list-images storage)))

  "run gif-name rover camera 100-150"

  ;;(api/get-photos :curiosity :mast :date "2015-6-3")
  )
