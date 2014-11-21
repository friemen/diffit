(ns diff.core
  "Implementation of O(NP) sequence comparison algorithm.")


;; Concepts
;;
;; fp is map from diagonal k to a pair [d edits],
;;   where d is the furthest distance and
;;   edits is a vector of edit operations.
;; 
;; as, bs are sequences of arbitrary items that support =
;; 
;; 


(defn- dump
  [fp]
  (doseq [[k [d edits]] (sort-by first fp)]
    (println (format "%4d" k) (format "%4d" d) " -> " edits))
  (println (apply str (repeat 40 "-"))))



(defn- equals   [[a b]] (= a b))
(defn- distance [fp k]  (first (get fp k [-1])))
(defn- edits    [fp k]  (second (get fp k [nil []])))


(defn- snake
  "Advances x on the diagonal k as long as corresponding items in as
  and bs match."
  [as bs fp k]
  (let [k+1   (inc k)
        k-1   (dec k)
        i     (inc (distance fp k-1))
        j     (distance fp k+1)
        x     (max i j)
        d     (->> (map vector (drop x as) (drop (- x k) bs))
                   (take-while equals)
                   (count))]
    [(+ x d)
     (into (if (> i j)
             (conj (edits fp k-1) :-)
             (conj (edits fp k+1) :+))
           (repeat d :=))]))


(defn- step
  "Returns the next pair of [fp p] of furthest distances."
  [as bs delta [fp p]]
  (let [p         (inc p)
        diagonals (concat (range (* -1 p) delta)
                          (range (+ delta p) delta -1)
                          [delta])
        fp        (reduce (fn [fp k]
                            (assoc fp k (snake as bs fp k)))
                          fp
                          diagonals)]
    #_(dump fp)
    [fp p]))


(defn diff*
  "Assumes that (count as) >= (count bs)."
  [as n bs m]
  (let [delta (- n m)
        [fp p] (->> [{} -1]
                    (iterate (partial step as bs delta))
                    (drop-while (fn [[fp _]]
                                  (not= (distance fp delta) n)))
                    (first))]
    [(+ delta (* 2 p)) (->> (get fp delta) second (drop 1))]))


(defn swap-insdels
  "Swaps edit operation symbols :+ <-> :-"
  [[d edits]]
  [d (map {:+ :- :- :+ := :=} edits)])


(defn editscript
  "Produces an edit script from the edits issued by diff*."
  [as bs edits]
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
                    (conj script [:+ y (subvec (vec bs) y (+ y n))]))
          (recur (rest groups)
                 (+ x n) (+ y n)
                 script)))
      script)))


(defn diff
  "Returns a pair [edit-distance edit-script] after comparision of
  sequences as and bs. 

  Edit-distance is an integer. 

  The edit-script is a sequence of vectors starting with an insert or
  delete operation.

  An insert is [:+ position items].
  A delete is  [:- position number-of-items].

  The edit-script is made for sequential processing with operations
  like insert-at: [xs pos items -> xs'] and  remove-at: [xs pos n -> xs']."
  [as bs]
  (let [n (count as)
        m (count bs)
        [d edits] (if (< n m)
                    (swap-insdels (diff* bs m as n))
                    (diff* as n bs m))]
    [d (editscript as bs edits)]))


;; inefficient insert and remove implementation

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
  [as [d es]]
  (vec (reduce (fn [bs [op & params]]
                 (case op
                   :+ (insert-at bs (first params) (second params))
                   :- (remove-at bs (first params) (second params))))
               (vec as)
               es)))

