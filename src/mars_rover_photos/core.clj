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

(defn- res-exists?
  [name]
  (.exists (io/as-file name)))

(defn- generate-gif
  [rover camera sol]
  (let [images-src (rover/get-images (keyword rover) (keyword camera) {:sol (Integer. sol)})
        images (storage/download-images images-src imgs-path)]
    (if-not (empty? images)
      (let [gif-src (str pub-res (hash images) ".gif")]
        (cond (res-exists? gif-src) (gif-response (view/rover-gif gif-src rover camera sol) 200)
              (gif/generate gif-src images)  (gif-response (view/rover-gif gif-src rover camera sol) 200)
              :else (gif-response "There was a problem generating the gif.." 404)))
      (gif-response "No images found for these parameters" 404))))

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
