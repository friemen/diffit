(ns seqdiff.core
  "Implementation of O(NP) sequence comparison algorithm.")


;; Concepts
;;
;; fp is a map {k -> [d edits]} from diagonal k to a pair where
;;   d is the furthest distance and
;;   edits is a vector of edit operations.
;; 
;; as, bs are sequences of arbitrary items that support equals (=)
;; av, bv are vector versions that have better count and nth performance
;; 
;; 


(defn- dump
  [fp]
  (doseq [[k [d edits]] (sort-by first fp)]
    (println (format "%4d" k) (format "%4d" d) " -> " edits))
  (println (apply str (repeat 40 "-"))))


(defmacro ^:private with-time
  [time-atom & exprs]
  `(let [start# (System/nanoTime)
         result# ~@exprs
         stop# (System/nanoTime)]
     (swap! ~time-atom + (- stop# start#))
     result#))


(defn- distance [fp k]  (first (get fp k [-1])))
(defn- edits    [fp k]  (second (get fp k [nil []])))


(defn- snake
  "Advances x on the diagonal k as long as corresponding items in av
  and bv match."
  [av bv fp k]
  (let [n     (count av)
        m     (count bv)
        k+1   (inc k)
        k-1   (dec k)
        i     (inc (distance fp k-1))
        j     (distance fp k+1)
        x     (max i j)
        y     (- x k)
        ;; search for the maximum x on diagonal
        fx    (loop [^long x x ^long y y]
                (if (and (< x n) (< y m) (= (nth av x) (nth bv y)) )
                  (recur (inc x) (inc y))
                  x))]
    [fx
     ;; add edit operation symbols
     (let [es (if (> i j)
                (conj (edits fp k-1) :-)
                (conj (edits fp k+1) :+))]
       (if (> fx x)
         (conj es (- fx x))
         es))]))


(defn- step
  "Returns the next pair of [fp p] of furthest distances."
  [av bv delta [fp p]]
  (let [p         (inc p)
        diagonals (concat (range (* -1 p) delta)
                          (range (+ delta p) delta -1)
                          [delta])
        fp        (reduce (fn [fp k]
                            (assoc fp k (snake av bv fp k)))
                          fp
                          diagonals)]
    [fp p]))


(defn- diff*
  "Assumes that (count as) >= (count bs)."
  [av bv]
  (let [delta (- (count av) (count bv))
        [fp p] (->> [{} -1]
                    (iterate (partial step av bv delta))
                    (drop-while (fn [[fp _]]
                                  (not= (distance fp delta) (count av))))
                    (first))]
    [(+ delta (* 2 p)) (->> (get fp delta)
                            (second)
                            (drop 1))]))


(defn- swap-insdels
  "Swaps edit operation symbols :+ <-> :-"
  [[d edits]]
  [d (map (fn [op] (case op :+ :- :- :+ op)) edits)])


(defn- editscript
  "Produces an edit script from the edits issued by diff*."
  [av bv edits]
  ;; the groups are seqs of :+'s or :-'s or one number
  (loop [groups (partition-by identity edits)
         x 0
         y 0
         script []]
    (if-let [[op & ops] (first groups)]
      (let [n (inc (count ops))]
        (case op
          :- (recur (rest groups)
                    x y
                    (conj script [:- x n]))
          :+ (recur (rest groups)
                    (+ x n) (+ y n)
                    (conj script [:+ y (subvec bv y (+ y n))]))
          (recur (rest groups)
                 (+ x op) (+ y op) ; op is the number of items to skip
                 script)))
      script)))


(defn diff
  "Returns a pair [edit-distance edit-script] as result of comparision
  of sequences as and bs.

  Edit-distance is an integer. 

  The edit-script is a sequence of vectors starting with an insert or
  delete operation symbol :+ or :-.

  An insert is [:+ position items].
  A delete is  [:- position number-of-items].

  The edit-script is made for sequential processing with operations
  like insert-at: [xs pos items -> xs'] and  remove-at: [xs pos n -> xs']."
  [as bs]
  (let [av (vec as)
        bv (vec bs)
        [d edits] (if (< (count av) (count bv))
                    (swap-insdels (diff* bv av))
                    (diff* av bv))]
    [d (editscript av bv edits)]))


;; naive insert and remove implementation

(defn- insert-at
  [xs i ys]
  (concat (take i xs)
          (if (coll? ys) ys (list ys))
          (drop i xs)))


(defn- remove-at
  ([xs i]
     (remove-at xs i 1))
  ([xs i n]
     (concat (take i xs) (drop (+ i n) xs))))


(defn patch
  "Applies the edit-script (as contained in the result of diff) to
  sequence as, using by default insert-at and remove-at as implemented
  in this namespace. Returns a vector."
  ([as diff-result]
     (patch insert-at remove-at as diff-result))
  ([insert-f remove-f as [d es]]
     (vec (reduce (fn [bs [op & params]]
                    (case op
                      :+ (insert-f bs (first params) (second params))
                      :- (remove-f bs (first params) (second params))))
                  (vec as)
                  es))))

