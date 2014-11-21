# onpdiff

A diff implementation that produces an edit-script.
It follows
[An O(NP) Sequence Comparison Algorithm](http://www.itu.dk/stud/speciale/bepjea/xwebtex/litt/an-onp-sequence-comparison-algorithm.pdf)
by Wu, Manber, Myers and Miller.

The API consists of a `diff` and a `patch` function.

## Usage

Include a dependency in your project.clj.

In the REPL

```clojure
(require '[diff.core :as d])
;= nil
(diff [1 2 3 4] [1 2 7 8 4])
;= [3 [[:+ 2 [7 8]] [:- 4 1]]]
```

The result is a pair: the first part is the edit distance, the second
part is a sequence of edits (called an *edit-script*) that is needed
to create the second input sequence from the first.

It's produced in a form that allows sequential processing in a `patch`
function with insert and remove operations, where insert takes a
sequence, a position and a sequence to insert, and remove takes a
sequence, a position and a number of items to remove. Positions are
zero-based.

The edit-script in the output above can be read like this:

* First, add the sequence [7 8] at position 2 (this will shift the items 3 4 to the right).
* Then, remove 1 item at position 4.

There's also a `patch` operation included, but its current
implementation is a bit slow.


## License

Copyright © 2014 F.Riemenschneider

Distributed under the Eclipse Public License either version 1.0.
