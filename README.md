# seqdiff

A diff on two sequences that produces a minimal edit-script to
transform the first into the second by applying insertions and
deletions.  It follows
[An O(NP) Sequence Comparison Algorithm](http://www.itu.dk/stud/speciale/bepjea/xwebtex/litt/an-onp-sequence-comparison-algorithm.pdf)
by Wu, Manber, Myers and Miller.

The API consists of a `(diff xs ys)` and a `(patch diff-result xs)` function, where
`(= ys (patch xs (diff xs ys))`.

## Usage

Include a dependency in your project.clj.

TODO

In the REPL

```clojure
(require '[seqdiff.core :as d])
;= nil
(d/diff [1 2 3 4] [1 2 7 8 4])
;= [3 [[:+ 2 [7 8]] [:- 4 1]]]
```

The result is a pair: the first part is the
[edit distance](http://en.wikipedia.org/wiki/Edit_distance), the
second part is a sequence of edits (called an *edit-script*) that is
needed to create the second input sequence from the first.

It's produced in a form that allows sequential processing in a `patch`
function with insert and remove operations, where insert takes a
sequence, a position and a sequence to insert, and remove takes a
sequence, a position and a number of items to remove. Positions are
zero-based.

The edit-script in the output above can be read like this:

* First, add the sequence [7 8] at position 2 (this will shift the items 3 4 to the right).
* Then, remove 1 item at position 4.

TODO describe patch

## License

Copyright © 2014 F.Riemenschneider

Distributed under the Eclipse Public License either version 1.0.
