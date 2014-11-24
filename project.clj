(defproject diffit "0.1.0-SNAPSHOT"
  :description "Diff and patch implementations for vector and map"
  :url "https://github.com/friemen/diffit"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 ;; make sure these are commented
                 #_ [clj-diff "1.0.0-SNAPSHOT" :scope "test"]
                 #_ [com.googlecode.java-diff-utils/diffutils "1.3.0"]])
