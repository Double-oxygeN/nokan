(defproject brainf "1.0.0"
  :description "NOKAN: Brainfuck interpreter with TCP connection"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/core.async "0.2.395"]]
  :main ^:skip-aot brainf.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
