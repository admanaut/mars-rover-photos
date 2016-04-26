(ns mars-rover-photos.core
  (:require [mars-rover-photos.rover :as rover]
            [mars-rover-photos.gif :as gif]
            [mars-rover-photos.storage :as storage]
            [mars-rover-photos.view :as view]
            [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [ring.adapter.jetty :as jetty]
            [hiccup.page :as h])
  (:gen-class))


(def pub-res "public/")
(def imgs-path "images/")

(defn- gif-response
  [body status]
  {:status status
   :headers {"Content-Type" "text/html"}
   :body body})

(defn- generate-gif
  [rover camera sol]
  (let [images-src (rover/get-images (keyword rover) (keyword camera) {:sol (Integer. sol)})
        files (storage/download-images images-src imgs-path)
        gif-src (gif/generate pub-res files)]
    (if (and images-src files gif-src)
      (gif-response (view/rover-gif gif-src rover camera sol) 200)
      (gif-response "No images found for these parameter" 404))))

(defn- rovers-info []
  (reduce (fn [rs r] (conj rs (rover/get-info r)) )
          []
          rover/rovers))


(defroutes app
  (GET "/" [] (view/index (rovers-info)))

  (GET "/gif" { {:keys [rover camera sol]} :params}
       (generate-gif rover camera sol))

  (route/files "/public/")
  (route/not-found "Page not found"))


(def application (site #'app))

(defn start
  [port]
  (jetty/run-jetty application {:port port
                                :join? false}))


(defn -main []
  (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
    (start port)))

(comment
  "For interactive development:"

  (def server (-main))
  (.stop server)
  (.start server)
)
