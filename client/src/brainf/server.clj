(ns brainf.server
  (:import [java.net Socket ServerSocket]
           [java.io PrintWriter BufferedReader InputStreamReader])
  (:require [clojure.core.async :as a :refer [<! >! >!! chan go-loop]]))

(def port 4040)

(defn sender-channel [c socket]
  (let [out (PrintWriter. (.getOutputStream socket) true)]
    (go-loop []
      (.println out (<! c))
      (recur))))

(defn receiver-channel [c socket]
  (let [in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    (go-loop [line (.readLine in)]
      (println line)
      (>! c line)
      (recur (.readLine in)))))

(defn create-server [port]
  (with-open [server-socket (ServerSocket. port) socket (.accept server-socket)]
    (let [ch (chan)]
      (sender-channel ch socket)
      (receiver-channel ch socket)
      (println "Accepted connections on port" port)
      (loop [] (recur)))))
