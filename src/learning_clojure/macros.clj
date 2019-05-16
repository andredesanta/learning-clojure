;; MACROS

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Macros give you a convenient way to manipulate lists before Clojure evaluates them.

;; - They are executed in between the reader and the evaluator, transforming the data structures before passing them to the evaluator.

		(defmacro ignore-last-operand
		  [function-call]
		  (butlast function-call))

		(ignore-last-operand (+ 1 2 10))
;  => 3

;; This will not print anything
		(ignore-last-operand (+ 1 2 (println "look at me!!!")))
;  => 3

;; - When you call a macro, the operands are not evaluated.

;; - The data structure returned by a function is not evaluated, but the data structure returned by a macro is.

;; - The process of determining the return value of a macro is called macro expansion, and you can use the function macroexpand 
;; to see what data structure a macro returns before that data structure is evaluated:

		(macroexpand '(ignore-last-operand (+ 1 2 10)))
;  => (+ 1 2)

		(macroexpand '(ignore-last-operand (+ 1 2 (println "look at me!!!"))))
;  => (+ 1 2)

;; - Macro for doing infix notation:

		(defmacro infix
  [infixed]
  (list (second infixed) 
        (first infixed) 
        (last infixed)))

		(infix (1 + 2))
;  => 3

;; - Macros enable syntactic abstraction: use of the the built-in -> macro, which is also known as the threading or stabby macro.

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


