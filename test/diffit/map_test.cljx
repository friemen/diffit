(ns diffit.map-test
  (:require #+clj [clojure.test :refer :all]
            #+cljs [cemerick.cljs.test :as t]
            [diffit.map :refer [diff patch]])
  #+cljs (:require-macros [cemerick.cljs.test :refer [is are deftest testing]])
  #+clj (:import [java.util HashMap]))


(deftest diff-tests
  (are [am          bm           d   es] (= [d es] (diff am bm))
       {}           {}           0   {:+ []       :- []   :r []}
       {:a 1}       {}           1   {:+ []       :- [:a] :r []}
       {:a 1}       {:a 2}       1   {:+ []       :- []   :r [[:a 2]]}
       {}           {:b 2}       1   {:+ [[:b 2]] :- []   :r []}
       {:a 1 :b 2}  {:a 2 :c 4}  3   {:+ [[:c 4]] :- [:b] :r [[:a 2]]}))


(deftest diffpatch-tests
  (are [am          bm] (= bm (patch am (diff am bm)))
       {}           {}
       {:a 1}       {}
       {:a 1}       {:a 2}
       {}           {:b 2}
       {:a 1 :b 2}  {:a 2 :b 3 :c 4}))

#+clj
(deftest patch-javamap-test
  (let [am  {:a 1 :b 2 :c 3 :d 4}
        bm  {:a 2 :c 3 :d 5 :e 6}
        dr  (diff am bm)
        mm  (let [mm (HashMap.)]
              (doseq [[k v] am] (.put mm k v))
              mm)]
    (patch (fn [m k v]
              (.put m k v)
              m)
           (fn [m k]
             (.remove m k)
             m)
           (fn [m k v]
              (.put m k v)
              m)
           mm
           dr)
    (is (= mm bm))))


#_(def am (->> (repeatedly #(rand-nth [:foo :bar :baz]))
               (map vector (range 1e5))
               (into {})))
#_(def bm (->> am
             (filter (fn [_] (rand-nth [true true false])))
             (into {})))
