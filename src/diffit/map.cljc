(ns diffit.map
  "Diff and patch on maps.")


;; ---------------------------------------------------------------------------
;; diff

(defn diff
  "Returns a pair [edit-distance edit-script] as result of comparision
  of two maps am and bm.
  
  The edit-distance is the number of assocs+dissocs necessary to
  reproduce bm from am.

  The edit-script is a map with three entries: 
    :+ sequence of k-v pairs that are only present in bm
    :- sequence of keys removed in bm
    :r sequence of k-v pairs that have changed in bm.

  The edit-script can be used with patch to create bm from am."
  [am bm]
  (let [[adds dels reps]
        (->> (keys am)
             (concat (keys bm))
             (into #{})
             (reduce (fn [[adds dels reps] k]
                       (let [a  (get am k ::none)
                             b  (get bm k ::none)
                             a? (not= a ::none)
                             b? (not= b ::none)]
                         (cond
                          (and a? b? (not= a b)) [adds dels (conj! reps [k b])]
                          (and a? b?)            [adds dels reps]
                          a?                     [adds (conj! dels k) reps] 
                          b?                     [(conj! adds [k b]) dels reps])))
                     [(transient []) (transient []) (transient [])]))]
    [(+ (count adds) (count dels) (count reps))
     {:+ (persistent! adds)
      :- (persistent! dels)
      :r (persistent! reps)}]))



;; ---------------------------------------------------------------------------
;; patch

(defn patch
  "Takes a map am and the result as produced by diff and returns a map
  with all deletions, replacements and additions applied to the input
  map."
  ([assoc-f dissoc-f replace-f am [d {adds :+ dels :- reps :r}]]
     (let [m' (reduce (fn [m k]
                        (dissoc-f m k)) am dels)
           m' (reduce (fn [m [k v]]
                        (replace-f m k v)) m' reps)]
       (reduce (fn [m [k v]]
                 (assoc-f m k v)) m' adds)))
  ([am [d {adds :+ dels :- reps :r}]]
     (into (reduce dissoc am dels) (concat reps adds))))


