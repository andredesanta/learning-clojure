;; READING AND EVALUATING

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Consider this trivial macro:

		(defmacro backwards
		  [form]
		  (reverse form))

		(backwards (" backwards" " am" "I" str))
		; => "I am backwards"

;; - Clojure has an evaluation model that differs from most other languages.

;; - It has a two-phase system where it reads textual source code, producing Clojure data structures.

;; - These data structures are then evaluated: Clojure traverses the data structures and performs actions based on the type of the data
;; structure.

;; - Languages that have this relationship between source code, data, and evaluation are called homoiconic.

;; - Homoiconic languages empower you to reason about your code as a set of data structures that you can manipulate programmatically.

;; - s-expressions refers to both the actual data object that gets evaluated and the source code that represents that data.

;; - Your text represents native data structures, and Lisps evaluate native data structures.

;; - You can send your program’s data structures directly to the Clojure evaluator with eval:

		(def addition-list (list + 1 2))
		(eval addition-list)
		; => 3

		(eval (concat addition-list [10]))
		; => 13

		(eval (list 'def 'lucky-number (concat addition-list [10])))
		; => #'user/lucky-number

		lucky-number
		; => 13

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The Reader

;; - The reader converts the textual source code you save in a file or enter in the REPL into Clojure data structures.

;; - The textual representation of data structures (sequence of Unicode characters) is called a reader form.

;; - read-string takes a string as an argument and processes it using Clojure’s reader, returning a data structure:

		(read-string "(+ 1 2)")
		; => (+ 1 2)

		(list? (read-string "(+ 1 2)"))
		; => true

		(conj (read-string "(+ 1 2)") :zagglewag)
		; => (:zagglewag + 1 2)

;; - You can read text without evaluating it, and you can pass the result to other functions.

;; - Reader macros are sets of rules for transforming text into data structures. They’re designated by macro characters,
;; like ' (the single quote), #, and @.

;; - They often allow you to represent data structures in more compact ways because they take an abbreviated reader form
;; and expand it into a full form.

		(read-string "'(a b c)")
		; => (quote (a b c))

;; - When the reader encounters the single quote, it expands it to a list whose first member is the symbol quote and whose
;; second member is the data structure that followed the single quote.

;; - The deref reader macro works similarly for the @ character:

		(read-string "@var")
		; => (clojure.core/deref var)

;; - The semicolon designates the single-line comment reader macro:

		(read-string "; ignore!\n(+ 1 2)")
		; => (+ 1 2)

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The Evaluator

;; - To evaluate a symbol, Clojure looks up what the symbol refers to.

;; - To evaluate a list, Clojure looks at the first element of the list and calls a function, macro, or special form.

;; - Any other values (including strings, numbers, and keywords) simply evaluate to themselves. Empty lists evaluate to themselves too.

;; - Clojure uses symbols to name functions, macros, data, and anything else you can use, and evaluates them by resolving them.

;; - To resolve a symbol, Clojure traverses any bindings you’ve created and then looks up the symbol’s entry in a namespace mapping.

;; - If you try to refer to a special form outside of this context, you’ll get an exception.

;; - A local binding is any association between a symbol and a value that wasn’t created by def.

;; - Special forms are special because they implement core behavior that can’t be implemented with functions.

;; - The quote special form tells the evaluator, “Instead of evaluating my next data structure like normal, just return the data
;; structure itself.”

