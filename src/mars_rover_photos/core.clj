(ns mars-rover-photos.core
  (:require
   [mars-rover-photos.conf :refer [config]]
   [mars-rover-photos.rover :as rv]
   [mars-rover-photos.gif :as gif]
   [mars-rover-photos.view :as view]
   [mount.core :as mount :refer [defstate]]
   [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
   [compojure.handler :refer [site]]
   [compojure.route :as route]
   [ring.adapter.jetty :as jetty])
  (:gen-class))

(defn- parse-rover
  [^String rover]
  (when (some #(= (keyword rover) %) rv/rovers)
    (keyword rover)))

(defn- parse-camera
  [rover ^String camera]
  (let [cam (get rv/cameras (keyword camera))]
    (when (some #(= rover %) (:rovers cam))
      (keyword camera))))

(defn- parse-sol
  [^String sol]
  (try
    (let [sol-int (Integer/parseInt sol)]
      (when (pos? sol-int)
        sol-int))
    (catch NumberFormatException _
      (if-let [[_ sol-from sol-to] (re-find #"([0-9]*)-?([0-9]*)?" sol)]
        (try
          (let [sol-from-int (Integer/parseInt sol-from)
                sol-to-int (Integer/parseInt sol-to)]
               (when (and (pos? sol-from-int)
                          (pos? sol-to-int)
                          (> sol-to-int sol-from-int))
                 [sol-from-int sol-to-int]))
          (catch NumberFormatException _))))))

(defn- generate-gif
  [rover camera sol]
  (let [rv-parsed (parse-rover rover)
        cam-parsed (parse-camera rv-parsed camera)
        sol-parsed (parse-sol sol)]
    (if (and rv-parsed cam-parsed sol-parsed)
      (let [images (rv/get-images rv-parsed cam-parsed sol-parsed)]
        (if (seq images)
          (view/rover-gif (gif/generate images) rv-parsed cam-parsed nil)
          (view/error "There was an error somewhere. You better try again!")))
      (view/error "Invalid parameters"))))

(defn- rovers-info []
  (reduce (fn [rs r] (conj rs (rv/get-info r)) )
          []
          rv/rovers))

(defroutes app
  (GET "/" [] (view/index (rovers-info)))

  (GET "/gif" { {:keys [rover camera sol]} :params}
       (view/rover-gif-page (generate-gif rover camera sol)))

  (route/files "/public/")
  (route/not-found "Page not found"))


(defn start-server
  [config]
  (let [port (get (System/getenv) "PORT" (get-in config [:www :port]))]
    (-> #'app
        site
        (jetty/run-jetty {:port port
                          :join? false}))))

(defstate www-app
  :start (start-server config)
  :stop (.stop www-app))

(defn -main []
  (mount/start))
