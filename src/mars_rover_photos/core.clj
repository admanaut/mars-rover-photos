(ns mars-rover-photos.core
  (:require
   [mars-rover-photos.rover :as rv]
   [mars-rover-photos.gif :as gif]
   [mars-rover-photos.storage :as st]
   [mars-rover-photos.view :as view]
   [mars-rover-photos.util :as ut]
   [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
   [compojure.handler :refer [site]]
   [compojure.route :as route]
   [clojure.java.io :as io]
   [ring.adapter.jetty :as jetty])
  (:gen-class))

(def pub-res "public/")
(def imgs-path "images/")

(defn- respond
  [body status]
  {:status status
   :headers {"Content-Type" "text/html"}
   :body body})

(defn- res-exists?
  [name]
  (.exists (io/as-file name)))

(defn ->int
  [^String string]
  (ut/try* (Integer. string)))

(defn- valid-params?
  [rover camera sol]
  (and (not (empty? (filter #(= (keyword rover) %) rv/rovers)))
       (not (empty? (filter #(= (:abbrev %) (keyword camera)) rv/cameras)))
       (not (nil? (re-matches #"[0-9]+-?[0-9]+?" sol)))))

(defn- generate-gif
  [rover camera sol]
  (if (valid-params? rover camera sol)
    (let [[_ sol-from sol-to] (re-find #"([0-9]*)-?([0-9]*)?" sol)
          images-src (rv/get-images (keyword rover) (keyword camera) {:sol [(->int sol-from) (->int sol-to)]})
          images (st/download-images images-src imgs-path)]
      (println sol-from sol-to)
      (if (seq images)
        (let [gif-src (str pub-res (hash (sort images)) ".gif")]
          (if (or (res-exists? gif-src) (gif/generate gif-src images))
            (respond (view/rover-gif gif-src rover camera sol) 200)
            (respond "There was a problem generating the gif.." 404)))
        (respond "No images found for these parameters" 404)))
    (respond "bad request" 400)))

(defn- rovers-info []
  (reduce (fn [rs r] (conj rs (rv/get-info r)) )
          []
          rv/rovers))


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
  (.start server)
  (.stop server)

  "and open http://localhost:8080"
)
