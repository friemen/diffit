(ns diffit.debug
  (:require [diffit.vec :as v]
            [diffit.vec-test :refer [rand-alter]]))


#_ (do (def as (range 2000))
    (def bs (rand-alter 80 10 10 as)))

#_ (>bench (v/diff as bs))

#_ ( require '[clj-diff.core :as d])
#_ (>bench (d/diff as bs))

#_ (import '[difflib DiffUtils])
#_ (>bench (DiffUtils/diff as bs))



;; ---------------------------------------------------------------------------
;; stuff to debug and tune performance

#_ (defn- dump
  [fp]
  (doseq [[k [d edits]] (sort-by first fp)]
    (println (format "%4d" k) (format "%4d" d) " -> " edits))
  (println (apply str (repeat 40 "-"))))


#_ (defmacro ^:private with-time
  [time-atom & exprs]
  `(let [start# (System/nanoTime)
         result# ~@exprs
         stop# (System/nanoTime)]
     (swap! ~time-atom + (- stop# start#))
     result#))

#_ (def t (atom 0))


#_ (defn diffpatch
  [as bs]
  (println as)
  (println bs)
  (let [diffres (v/diff as bs)
        patched (v/patch as diffres)]
    (println (second diffres))
    (println "expected" (vec bs))
    (println "actual  " patched)
    (assert (= (vec bs) patched))))

#_ (do (reset! diffit.vec/t 0)
      (v/diff as bs)
      (println (float (/ @diffit.vec/t 1e6))))

