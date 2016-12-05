(ns brainf.client
  (:import (java.net Socket)
           (java.io PrintWriter InputStreamReader BufferedReader)
           (java.lang NullPointerException))
  (:require [clojure.core.async :as a :refer [<! >! <!! >!! chan go go-loop]]
            [clojure.string :as str]
            [brainf.interpreter :as i]))

(def localhost {:host "localhost" :port 4040})
(def help-message
  "
Nokan - Brainfuck interpreter v0.1.0

command     | description
------------+--------------------------
init        | initialize the code
show        | show entire code
add x       | add x to the code
push x      | change the code into x
amend x y z | change letters x-y into z
replace x y | replace x with y
run         | run the code
quit / exit | quit the interpreter
help        | see these messages

this interpreter is connecting server on port ")

(defn printer-channel [c]
  (go-loop []
    (print (<! c))
    (flush)
    (recur)))

(defn reader-channel [c socket]
  (let [in (BufferedReader. (InputStreamReader. (.getInputStream socket)))]
    (go-loop [line (.readLine in)]
      (>! c line)
      (recur (.readLine in)))))

(defn writer-channel [c socket]
  (let [out (PrintWriter. (.getOutputStream socket) true)]
    (go-loop []
      (.println out (<! c))
      (recur))))

(defn run-code [printc linec socket]
  (let [in (BufferedReader. (InputStreamReader. (.getInputStream socket)))
        line (<!! linec)
        output (try (str "CODE : \n" line "\n\nOUTPUT : \n" (i/interpret line) "\nnokan > ") (catch NullPointerException e (str "ERROR : " e "\nnokan > ")))]
    (go (>! printc output))))

(defn connect [server]
  (with-open [socket (Socket. (:host server) (:port server))]
    (let [printerc (chan) readerc (chan) writerc (chan)]
      (printer-channel printerc)
      (reader-channel readerc socket)
      (writer-channel writerc socket)
      (>!! printerc "server connected.\nnokan > ")
      (loop [cmd (read-line)]
        (let [commands (str/split cmd #"\s")]
          (case (first commands)
            "init" (do (>!! writerc \I) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "show" (do (>!! writerc \S) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "add" (do (>!! writerc (str \A (second commands))) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "push" (do (>!! writerc (str \P (second commands))) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "amend" (do (>!! writerc (str \M (second commands) \| (nth commands 2) \| (nth commands 3))) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "replace" (do (>!! writerc (str \R (second commands) \| (nth commands 2))) (>!! printerc (str (<!! readerc) \newline "nokan > ")) (recur (read-line)))
            "help" (do (>!! printerc (str help-message (:port server) "\n\nnokan > ")) (recur (read-line)))
            "run" (do (>!! writerc \S) (run-code printerc readerc socket) (recur (read-line)))
            "quit" nil
            "exit" nil
            (do (>!! printerc (str (first commands) " : command not found\nIf you want to see help, use \"help\" command.\nnokan > ")) (recur (read-line)))))))))
