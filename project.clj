(defproject diffit "1.0.0"
  :description "Clojure(Script) diff and patch implementations for vector and map."
  :url "https://github.com/friemen/diffit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; make sure these are commented
                 #_ [clj-diff "1.0.0-SNAPSHOT" :scope "test"]
                 #_ [com.googlecode.java-diff-utils/diffutils "1.3.0"]]
  :jar-exclusions [#"\.cljx"]
  :plugins [[codox "0.8.10"]]
  :codox {:defaults {}
          :sources ["src" "target/classes"]
          :exclude []
          :src-dir-uri "https://github.com/friemen/diffit/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :scm {:name "git"
        :url "https://github.com/friemen/examine"}
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :creds :gpg}]]
  :hooks [cljx.hooks]
  :source-paths ["src" "target/classes"]
  :test-paths ["target/test-classes"]
  :profiles {:dev {:dependencies [[org.clojure/clojurescript "0.0-2311"]]
                   :plugins [[com.cemerick/austin "0.1.5"]
                             [org.clojars.frozenlock/cljx "0.4.6"]
                             [lein-cljsbuild "1.0.4-SNAPSHOT"]
                             [com.cemerick/clojurescript.test "0.3.1"]]
                   :injections [(require 'cemerick.austin.repls)
                                (defn browser-repl []
                                  (cemerick.austin.repls/cljs-repl (reset! cemerick.austin.repls/browser-repl-env
                                                                           (cemerick.austin/repl-env))))]
                   :cljsbuild {:test-commands {"phantom" ["phantomjs" :runner "target/testable.js"]}
                               :builds [{:source-paths ["target/classes" "target/test-classes"]
                                         :compiler {:output-to "target/testable.js"
                                                    :libs [""]
                                                    :source-map "target/testable.js.map"
                                                    :optimizations :advanced}}]}
                   :cljx {:builds [{:source-paths ["src"]
                                    :output-path "target/classes"
                                    :rules :clj}
                                   {:source-paths ["src"]
                                    :output-path "target/classes"
                                    :rules :cljs}
                                   {:source-paths ["test"]
                                    :output-path "target/test-classes"
                                    :rules :clj}
                                   {:source-paths ["test"]
                                    :output-path "target/test-classes"
                                    :rules :cljs}]}}
             :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0-alpha4"]]}}
  :aliases {"all" ["with-profile" "+dev:+1.4:+1.5:+1.7"]
            "deploy" ["do" "clean," "cljx" "once," "deploy" "clojars"]
            "test" ["do" "clean," "cljx" "once," "test," "with-profile" "dev" "cljsbuild" "test"]})
