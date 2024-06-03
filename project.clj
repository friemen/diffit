(defproject diffit "1.0.1-SNAPSHOT"
  :description
  "Clojure(Script) diff and patch implementations for vector and map."

  :url
  "https://github.com/friemen/diffit"

  :license
  {:name "Eclipse Public License"
   :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies
  [[org.clojure/clojure "1.10.3"]]

  :plugins
  [[lein-codox "0.10.8"]]

  :codox
  {:defaults {}
   :sources ["src"]
   :exclude []
   :src-dir-uri "https://github.com/friemen/diffit/blob/master/"
   :src-linenum-anchor-prefix "L"}
  :scm

  {:name "git"
   :url "https://github.com/friemen/diffit"}

  :repositories
  [["clojars" {:url "https://clojars.org/repo"
               :creds :gpg}]]

  :aliases
  {"deploy" ["do" "clean," "deploy" "clojars"]})
