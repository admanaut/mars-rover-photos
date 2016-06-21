(defproject mars-rover-photos "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.6.1"]
                 [java-jdbc/dsl "0.1.3"]
                 [postgresql "9.3-1102.jdbc41"]
                 [mount "0.1.10"]
                 [org.clojure/data.json "0.2.6"]
                 [gif-clj "1.0.3"]
                 [compojure "1.5.0"]
                 [ring/ring-core "1.4.0"]
                 [ring/ring-jetty-adapter "1.4.0"]
                 [hiccup "1.0.5"]
                 [clj-time "0.11.0"]]
  :main ^:skip-aot mars-rover-photos.core
  :uberjar-name "mars-rover-photos.jar"
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler mars-rover-photos.core/application}
  :profiles {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]]}
             :uberjar {:aot :all}})
