(ns diff.core-test
  (:require [clojure.test :refer :all]
            [diff.core :refer :all]))



(deftest diff-tests
  (are [as bs] (= bs (apply str (patch as (diff as bs))))
       ""          ""
       ""          "abc"
       "ab"        ""
       "ab"        "ABCab"
       "ab"        "abXYZ"
       "ab"        "ABCabXYZ"
       "ABC"       "abc"
       "ABCDEF"    "ADEF"
       "ABCDEF"    "ABCdefXYZEF"))


(defn rand-alter
  [pass-prob remove-prob add-prob xs]
  (let [ops (vec (concat (repeat pass-prob :=)
                         (repeat remove-prob :-)
                         (repeat add-prob :+)))]
    (reduce (fn [xs x]
              (case (rand-nth ops)
                :+ (conj xs x "-")
                :- xs
                := (conj xs x)))
            []
            xs)))


(deftest random-tests
  (are [n] (let [as (range n)
                 bs (rand-alter 50 25 25 as)
                 diffres (time (diff as bs))
                 patched (time (patch as diffres))]
             (= bs patched))
       10 100 1000))



;; debugging tool

(defn test-diffpatch
  [as bs]
  (println as)
  (println bs)
  (let [diffres (diff as bs)
        patched (patch as diffres)]
    (println (second diffres))
    (println "expected" (vec bs))
    (println "actual  " patched)
    (assert (= (vec bs) patched))))


