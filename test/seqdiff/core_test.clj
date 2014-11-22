(ns seqdiff.core-test
  (:require [clojure.test :refer :all]
            [seqdiff.core :refer :all]))



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
  (are [n] (let [as      (range n)
                 bs      (rand-alter 90 5 5 as)
                 _       (println n "items")
                 diffres (time (diff as bs))
                 patched (patch as diffres)]
             (= bs patched))
       10 100 1000 2000))



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

#_(do (def n 2000)
      (def as (rand-alter 90 5 5 (range n)))
      (def bs (range n)))
#_(do (reset! diff.core/t-snake 0)
      (diff as bs)
      (println (float (/ @diff.core/t-snake 1e6))))
