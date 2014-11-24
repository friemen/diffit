# diffit

A diff on two vectors / maps that produces the edit distance and a
minimal edit-script to transform the first into the second collection
by applying insertions and deletions.

[![Build Status](https://travis-ci.org/friemen/diffit.png?branch=master)](https://travis-ci.org/friemen/diffit)

[API docs](https://friemen.github.com/diffit)

[![Clojars Project](http://clojars.org/diffit/latest-version.svg)](http://clojars.org/diffit)

The vector based implementation follows
[An O(NP) Sequence Comparison Algorithm](http://www.itu.dk/stud/speciale/bepjea/xwebtex/litt/an-onp-sequence-comparison-algorithm.pdf)
by Wu, Manber, Myers and Miller.

Supports Clojure and ClojureScript.


## Why

Building data transformations with side-effect free functions is sane,
but can sometimes require to synchronize the final result of a
transformation with state that has an expensive mutation-based API
around it, for example a bound JavaFX ObservableList, or to transmit the
changes to a remote process for further processing. A fast diff is one
way to tackle this.

Unfortunately, the good work in
[clj-diff](https://github.com/brentonashworth/clj-diff) never made it
into a non-snapshot release. My implementation is from scratch, but - of
course - contains insights from existing open source work such as `clj-diff`
and others.


## Usage

There are two namespaces: `diffit.vec` and `diffit.map`. Each contains
a `diff` and a `patch` function.

`diffit.vec/diff` and `diffit.vec/patch` work for sequential things
which will be treated as vectors.

`diffit.map/diff` and `diffit.map/patch` work for associative things.

Let `x`, `y` both be sequential or associative, respectively:
 * `(diff x y)` produces a *diff-result* which is a pair of
   `[edit-distance edit-script]`.
 * `(patch diff-result x)` applies the edit-script in diff-result to
   `x` in order to reproduce `y`.
 * It holds that `(= y (patch x (diff x y))`.


In the REPL:

```clojure
(require '[diffit.vec :as v])
;= nil
(v/diff [1 2 3 4] [1 2 7 8 4])
;= [3 [[:+ 2 [7 8]] [:- 4 1]]]
```

The diff-result is a pair: the first part is the
[edit-distance](http://en.wikipedia.org/wiki/Edit_distance), the
second part is a sequence of edits (called an *edit-script*) that is
needed to produce the second input collection from the first.

The edit-script for vectors allows sequential processing in the
`patch` function with insert and remove operations, where insert takes
a sequence, a position and a sequence to insert, and remove takes a
sequence, a position and a number of items to remove. Positions are
zero-based.

The edit-script in the output above can be read like this:

* First, add the sequence [7 8] at position 2 (this will shift the items 3 4 to the right).
* Then, remove 1 item at position 4.

Once you have the result from `diff` you can apply its edit-script with `patch`:

```clojure
(v/patch [1 2 3 4])
;= [1 2 7 8 4]
```

The `patch` function can be used with two or three (resp.) additional
arguments to hand in functions that actually do the insert, remove,
assoc, dissoc or replace operation. This lets you adapt `patch` to an
API based on mutation.


For a Java `ArrayList` here's a corresponding snippet:

```clojure
(patch (fn [l index item]
		 (.addAll l index item)
		 l)
	   (fn [l index n]
		 (dotimes [_ n] (.remove l index))
		 l)
	   mutable-list	diff-result)
```

For Java `HashMap` this looks like this

```clojure
(patch (fn [m k v]
         (.put m k v)
         m)
       (fn [m k]
	     (.remove m k)
         m)
       (fn [m k v]
         (.put m k v)
         m)
       mutable-map diff-result)
```



## Performance for vector based diff

`diffit.vec/diff` is a slight bit better than the
[clj-diff](https://github.com/brentonashworth/clj-diff) implementation
which uses the same algorithm and was also built with performance as
key requirement. Both clearly outperform the 1.3.0 version of an open source
Java library called
[diffutils](https://code.google.com/p/java-diff-utils/).

(So far, I did no extensive research for other Java alternatives. If
you find a better candidate that handles Java lists, and not only
text, drop me a mail.)


### Setup

I tested it for 2000 items where the sink `bs` was derived from the source
`as` by randomly adding or removing items. Add/remove took place
with 10% probability each, so 80% of the sink is the same as the
source.

This snippet defines source and sink sequences:

```clojure
(do (def as (range 2000))
    (def bs (rand-alter 80 10 10 as))))
```

Here's the piece of code that creates the sink from the source vector:

```clojure
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
```

I used [criterium](https://github.com/hugoduncan/criterium) `bench` to
gather times on a JDK 1.8.0_5 with Clojure 1.6.0 and 4 cores of
`Intel(R) Core(TM) i5 CPU M 560 @ 2.67GHz`. Here are the results:

### diffit

```
diffit.vec-test> (>bench (diff as bs))
Evaluation count : 2340 in 60 samples of 39 calls.
             Execution time mean : 28.204187 ms
    Execution time std-deviation : 1.357085 ms
   Execution time lower quantile : 26.592218 ms ( 2.5%)
   Execution time upper quantile : 30.952525 ms (97.5%)
                   Overhead used : 2.049385 ns

Found 2 outliers in 60 samples (3.3333 %)
	low-severe	 1 (1.6667 %)
	low-mild	 1 (1.6667 %)
 Variance from outliers : 33.6221 % Variance is moderately inflated by outliers
```

### clj-diff

```
diffit.vec-test> (>bench (clj-diff.core/diff as bs))
Evaluation count : 1860 in 60 samples of 31 calls.
             Execution time mean : 33.078036 ms
    Execution time std-deviation : 1.514409 ms
   Execution time lower quantile : 31.606479 ms ( 2.5%)
   Execution time upper quantile : 35.715329 ms (97.5%)
                   Overhead used : 2.049385 ns

Found 3 outliers in 60 samples (5.0000 %)
	low-severe	 2 (3.3333 %)
	low-mild	 1 (1.6667 %)
 Variance from outliers : 31.9529 % Variance is moderately inflated by outliers
```

### java-diff-utils

```
diffit.vec-test> (>bench (DiffUtils/diff as bs))
Evaluation count : 120 in 60 samples of 2 calls.
             Execution time mean : 1.014824 sec
    Execution time std-deviation : 16.119216 ms
   Execution time lower quantile : 990.193018 ms ( 2.5%)
   Execution time upper quantile : 1.054244 sec (97.5%)
                   Overhead used : 2.049385 ns

Found 4 outliers in 60 samples (6.6667 %)
	low-severe	 4 (6.6667 %)
 Variance from outliers : 1.6389 % Variance is slightly inflated by outliers
```


Unsurprisingly, the `diff` for maps is an order of magnitude faster due
to the performance characteristics of the underlying datastructures.


## License

Copyright © 2014 F.Riemenschneider

Distributed under the Eclipse Public License version 1.0.
