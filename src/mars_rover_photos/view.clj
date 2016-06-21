(ns mars-rover-photos.view
  (:require [clojure.string :as string]
            [hiccup.page :as h]))

(defn rover-cameras
  [cameras]
  [:table {:class "table table-condensed"}
   [:caption "Cameras installed on board"]
   [:tr
    [:th "Name"]
    [:th "Full name"]
    [:th "Use"]]
   (for [c cameras]
     [:tr
      [:td (:name c)]
      [:td (:full_name c)]
      [:td
       [:input {:type "radio" :name "camera" :value (string/lower-case (:name c))}]]])])

(defn prop-val
  [rover]
  (for [[n v] (seq rover)
        :when (not= n :cameras)]
    [:dd v]))

(defn prop-name
  [rover]
  (for [[n _] (seq rover)
        :when (not= n :cameras)]
    [:dt n]))

(defn rover-panel
  [r]
  [:div {:class "panel panel-default"}
   [:div {:class "panel-heading text-center"} (get r :name)]
   [:div {:class "panel-body"}
    [:dl {:class "dl-horizontal"}
     (interleave (prop-name r) (prop-val r))]
    (if-let [cams (:cameras r)]
      [:form {:action "gif" :method "GET" :target "gif-iframe"}
       [:input {:type "hidden" :name "rover" :value (string/lower-case (get r :name))}]
       (rover-cameras cams)
       [:div {:class "input-group"}
        [:input {:type "text" :class "form-control" :placeholder "Enter sol i.e. 100 or 100-200" :name "sol"}]
        [:span {:class "input-group-btn"}
         [:button {:type "submit" :class"btn btn-primary" } "GO!"]]]])]])

(defn rover-select
  [r]
  [:div {:class "col-sm-6 col-md-4"}
   [:div {:class "thumbnail"}
    [:img {:src "" :alt ""}]
    [:div {:class "caption"}
     [:h3 (get r "name")]
     [:p
      [:dl
       [:dt (str "Landing date: " (get r "landing_date")) ]
       [:dt (str "Total photos: " (get r "total_photos")) ]]]
     [:p
      [:a {:href "#" :class "btn btn-primary" :role "button" } "Select"]]]]])

(defn index
  [rovers]
  (h/html5
   [:head
    [:title "NASA Mars Rover Photos"]
    (h/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")]
   [:body
    [:div {:class "row"}
     (for [r rovers]
       [:div {:class "col-md-4"}
        (rover-panel r)])]
    [:div {:class "embed-responsive embed-responsive-16by9"}
     [:iframe {:name "gif-iframe" :class "embed-responsive-item"}]]]))

(defn rover-gif-page
  [content]
  (h/html5
   [:head
    (h/include-css "https://maxcdn.bootstrapcdn.com/bootstrap/3.3.6/css/bootstrap.min.css")]
   [:body
    [:div {:class "row"}
     [:div {:class "col-md-2"}]
     [:div {:class "col-md-8"}
      content]
     [:div {:class "col-md-2"}]]]))

(defn rover-gif
  [src rover camera sol]
  [:div
   [:div {:class "well well-sm"}
    [:span {:style "display: block"} (str "Images taken by " rover " with " camera " on sol " sol)]]
   [:img {:src src }]
   [:div {:class "col-md-2"}]])


(defn error
  [message]
  [:div {:class "alert alert-danger" :role "alert"} message])


(comment
     [:div {:class "row"}
     (for [r (seq rovers)]
       (rover-select r))]
 )
