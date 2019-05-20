;; MACROS

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Macros give you a convenient way to manipulate lists before Clojure evaluates them.

;; - They are executed in between the reader and the evaluator, transforming the data structures before passing them to the evaluator.

		(defmacro ignore-last-operand
		  [function-call]
		  (butlast function-call))

		(ignore-last-operand (+ 1 2 10))
  ; => 3

;; This will not print anything
		(ignore-last-operand (+ 1 2 (println "look at me!!!")))
  ; => 3

;; - When you call a macro, the operands are not evaluated.

;; - The data structure returned by a function is not evaluated, but the data structure returned by a macro is.

;; - The process of determining the return value of a macro is called macro expansion, and you can use the function macroexpand 
;; to see what data structure a macro returns before that data structure is evaluated:

		(macroexpand '(ignore-last-operand (+ 1 2 10)))
  ; => (+ 1 2)

		(macroexpand '(ignore-last-operand (+ 1 2 (println "look at me!!!"))))
  ; => (+ 1 2)

;; - Macro for doing infix notation:

		(defmacro infix
  [infixed]
  (list (second infixed) 
        (first infixed) 
        (last infixed)))

		(infix (1 + 2))
  ; => 3

;; - Macros enable syntactic abstraction: use of the built-in -> macro, which is also known as the threading or stabby macro.

		(defn read-resource
		  "Read a resource into a string"
		  [path]
		  (read-string (slurp (clojure.java.io/resource path))))

		(defn read-resource
  [path]
  (-> path
      clojure.java.io/resource
      slurp
      read-string))

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Macros allow Clojure to derive a lot of its built-in functionality from a tiny core of functions and special forms.

;; - when has this general form:

  (when boolean-expression
	  expression-1
	  expression-2
	  expression-3
	  ...
	  expression-x)

;; - when is actually a macro. In this macro expansion, you can see that when is implemented in terms of if and do:

		(macroexpand '(when boolean-expression
		                expression-1
		                expression-2
		                expression-3))
		; => (if boolean-expression
		;       (do expression-1
		;           expression-2
		;           expression-3))

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Anatomy of a Macro:

;; - Macro definitions have a name, an optional document string, an argument list and a body.

;; - The body will almost always return a list.

;; - Macros are a way of transforming a data structure into a form Clojure can evaluate.

;; - You can also use argument destructuring in macro definitions, just like you can with functions:

		(defmacro infix-2
		  [[operand1 op operand2]]
		  (list op operand1 operand2))

;; - You can also create multiple-arity macros, and in fact the fundamental Boolean operations and and or are defined as macros.

		(defmacro and
		  "Evaluates exprs one at a time, from left to right. If a form
		  returns logical false (nil or false), and returns that value and
		  doesn't evaluate any of the other expressions, otherwise it returns
		  the value of the last expr. (and) returns true."
		  {:added "1.0"}
		  ([] true)
		  ([x] x)
		  ([x & next]
		   `(let [and# ~x]
		      (if and# (and ~@next) and#))))

;; - Macros can be recursive, and they also can use rest args (& next in the n-arity macro body), just like functions.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Distinguishing Symbols and Values:

;; - You want to create a macro that takes an expression and both prints and returns its value:

		(defmacro my-print
		  [expression]
		  (list 'let ['result expression]
		        (list 'println 'result)
		        'result)) ;; this result is inside the let

;; - Here, you’re quoting each symbol you want to use as a symbol by prefixing it with the single quote character, '.

;; - If you unquote let, your macro body tries to get the value that the symbol let refers to, whereas what you actually want to
;; do is return the let symbol itself.

;; - This tells Clojure to turn off evaluation for whatever follows, in this case preventing Clojure from trying to resolve the 
;; symbols and instead just returning the symbols.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Simple Quoting:

;; - If we add quote at the beginning, it returns an unevaluated data structure:

		(quote (+ 1 2))
		; => (+ 1 2)

;; - The single quote character is a reader macro for (quote x):

		'(+ 1 2)
  ; => (+ 1 2)

;; - You can see quoting at work in the when macro. This is when’s actual source code:

		(defmacro when
		  "Evaluates test. If logical true, evaluates body in an implicit do."
		  {:added "1.0"}
		  [test & body]
		  (list 'if test (cons 'do body)))

;; - The macro definition quotes both if and do, because you want them to be in the final list that when returns for evaluation:

		(macroexpand '(when (the-cows-come :home)
		                (call me :pappy)
		                (slap me :silly)))
		; => (if (the-cows-come :home)
		;       (do (call me :pappy)
		;           (slap me :silly)))

;; - Another example of source code for a built-in macro, this time for unless:

		(defmacro unless
		  "Inverted 'if'"
		  [test & branches]
		  (conj (reverse branches) test 'if))  ; conjoining to a list is done at the beginning

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Syntax Quoting

;; - Quoting does not include a namespace if your code doesn’t include a namespace:

		'+
		; => +

;; - Syntax quoting will always include the symbol’s full namespace:

		`+
		; => clojure.core/+

;; - The reason syntax quotes include the namespace is to help you avoid name collisions.

;; - The other difference between quoting and syntax quoting is that the latter allows you to unquote forms using the tilde, ~:

		`(+ 1 ~(inc 1))
		; => (clojure.core/+ 1 2)

;; - Syntax quoting and unquoting allow you to create lists more clearly and concisely.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Using Syntax Quoting in a Macro

;; - Without using syntax quoting:

		(defmacro code-critic
		  "Phrases are courtesy Hermes Conrad from Futurama"
		  [bad good]
		  (list 'do
		        (list 'println
		              "Great squid of Madrid, this is bad code:"
		              (list 'quote bad))
		        (list 'println
		              "Sweet gorilla of Manila, this is good code:"
		              (list 'quote good))))

		(code-critic (1 + 1) (+ 1 1))
		; => Great squid of Madrid, this is bad code: (1 + 1)
		; => Sweet gorilla of Manila, this is good code: (+ 1 1)

;; - Using syntax quoting:

		(defmacro code-critic
		  "Phrases are courtesy Hermes Conrad from Futurama"
		  [bad good]
		  `(do (println "Great squid of Madrid, this is bad code:"
		                (quote ~bad))
		       (println "Sweet gorilla of Manila, this is good code:"
		                (quote ~good))))

;; - With syntax quoting, you can just wrap the entire do expression in a quote and simply unquote the two symbols that you
;; want to evaluate.

;; - Most of the time, your macros will return lists. You can build up the list to be returned by using list functions or 
;; by using syntax quoting.

;; -  If you want your macro to return multiple forms for Clojure to evaluate, make sure to wrap them in a do.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Refactoring a Macro and Unquote Splicing

;; - Improving the code-critic macro by creating a function to generate those println lists:

		(defn criticize-code
		  [criticism code]
		  `(println ~criticism (quote ~code)))

		(defmacro code-critic
		  [bad good]
		  `(do ~(criticize-code "Cursed bacteria of Liberia, this is bad code:" bad)
		       ~(criticize-code "Sweet sacred boa of Western and Eastern Samoa, this is good code:" good)))

;; - The criticize-code function returns a syntax-quoted list. This is how you build up the list that the macro will return.

;; - In a situation like this where you want to apply the same function to a collection of values, it makes sense to use a 
;; seq function like map:

		(defmacro code-critic
		  [bad good]
		  `(do ~(map #(apply criticize-code %)
		             [["Great squid of Madrid, this is bad code:" bad]
		              ["Sweet gorilla of Manila, this is good code:" good]])))
		(code-critic (1 + 1) (+ 1 1))
		; => NullPointerException

;; - The problem is that map returns a list, and in this case, it returned a list of println expressions. this code sticks
;; both results in a list and then tries to evaluate that list.

;; - Unquote splicing was invented precisely to handle this kind of situation. Unquote splicing is performed with ~@.

;; - If you merely unquote a list, this is what you get:

		`(+ ~(list 1 2 3))
		; => (clojure.core/+ (1 2 3))

;; - However, if you use unquote splicing, this is what you get:

		`(+ ~@(list 1 2 3))
		; => (clojure.core/+ 1 2 3)

;; - Unquote splicing unwraps a seqable data structure, placing its contents directly within the enclosing syntax-quoted
;; data structure.

;; - If you use unquote splicing in your code critic, then everything will work great:

		(defmacro code-critic
		  [{:keys [good bad]}]
		  `(do ~@(map #(apply criticize-code %)
		              [["Sweet lion of Zion, this is bad code:" bad]
		               ["Great cow of Moscow, this is good code:" good]])))

		(code-critic (1 + 1) (+ 1 1))
		; => Sweet lion of Zion, this is bad code: (1 + 1)
		; => Great cow of Moscow, this is good code: (+ 1 1)

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Variable Capture

;; - Macros have a couple of sneaky gotchas that you should be aware of.

;; - Variable capture occurs when a macro introduces a binding that, unknown to the macro’s user, eclipses an existing binding:

		(def message "Good job!")
		(defmacro with-mischief
		  [& stuff-to-do]
		  (concat (list 'let ['message "Oh, big deal!"])
		          stuff-to-do))

		(with-mischief
		  (println "Here's how I feel about that thing you did: " message))
		; => Here's how I feel about that thing you did: Oh, big deal!

















