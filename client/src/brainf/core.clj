(ns brainf.core
  (:require [brainf.client :refer :all]
            [brainf.server :as s])
  (:gen-class))

(defn -main
  [& args]
  (case (first args)
    "server" (s/create-server s/port)
    (connect localhost)))
