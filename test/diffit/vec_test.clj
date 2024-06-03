(ns diffit.vec-test
  (:require [clojure.test :refer :all]
            [diffit.vec :refer [diff patch]])
  (:import [java.util ArrayList]))



(deftest diffpatch-tests
  (are [as         bs] (= bs (apply str (patch as (diff as bs))))
       ""          ""
       ""          "abc"
       "ab"        ""
       "ab"        "ABCab"
       "ab"        "abXYZ"
       "ab"        "ABCabXYZ"
       "ABC"       "abc"
       "ABCDEF"    "ADEF"
       "ABCDEF"    "ABCdefXYZEFABCDEF"))


(deftest patch-javalist-test
  (let [as [:a :b :c :d :e :a :b :d]
        bs [:a :c :d :a :b :d]
        dr (diff as bs)
        ml (let [ml (ArrayList.)]
             (doseq [a as]
               (.add ml a))
             ml)]
    (patch (fn [l index item]
             (.addAll l index item)
             l)
           (fn [l index n]
             (dotimes [_ n] (.remove l index))
             l)
           ml
           dr)
    (= ml bs)))


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
