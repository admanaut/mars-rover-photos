(ns mars-rover-photos.util)

(defmacro try*
  "Macro for wrapping body in try catch, returns nil on Exception."
  [& body]
  `(try
     (do ~@body)
     (catch Exception e#
       nil)))

(defn io
  "Delays the execution of the impure function by wrapping it in a function."
  [impure-fn]
  (fn [] impure-fn))

(defn perform-io
  "Executes an io action by applying it to the args."
  [io & args]
  (apply (io) args))
