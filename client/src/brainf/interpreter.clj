(ns brainf.interpreter
  (:require [clojure.string :as str]))

(defn change-value [f dp data]
  (concat (take dp data) (list (f (nth data dp))) (drop (inc dp) data)))

(defn interpret
  [code]
  (loop [ip 0 dp 0 data (repeat 32768 0) outstr ""]
    (case (nth code ip nil)
      \+ (if (>= (nth data dp) 127) (recur (inc ip) dp (change-value (constantly 0) dp data) outstr) (recur (inc ip) dp (change-value inc dp data) outstr))
      \- (if (zero? (nth data dp)) (recur (inc ip) dp (change-value (constantly 127) dp data) outstr) (recur (inc ip) dp (change-value dec dp data) outstr))
      \< (if (zero? dp) (recur (inc ip) 32767 data outstr) (recur (inc ip) (dec dp) data outstr))
      \> (if (= dp 32767) (recur (inc ip) 0 data outstr) (recur (inc ip) (inc dp) data outstr))
      \[ (if (zero? (nth data dp)) (recur (str/index-of code \] ip) dp data outstr) (recur (inc ip) dp data outstr))
      \] (if (zero? (nth data dp)) (recur (inc ip) dp data outstr) (recur (str/last-index-of code \[ ip) dp data outstr))
      \. (recur (inc ip) dp data (str outstr (char (nth data dp))))
      \, (do (print "INPUT > ") (flush) (recur (inc ip) dp (change-value (constantly (int (first (read-line)))) dp data) outstr))
      nil outstr
      (recur (inc ip) dp data outstr))))
