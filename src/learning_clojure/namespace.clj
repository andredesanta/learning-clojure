;; NAMESPACES

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Namespaces contain maps between symbols and vars.

;; - Namespaces are objects of type clojure.lang.Namespace.

;; - You can refer to the current namespace with *ns* and get its name with:

		(ns-name *ns*)
  ; => user

;; - In Clojure programs, you are always in a namespace.

;; - If you want to just use the symbol itself, and not the thing it refers to, you have to quote it. 

;; - Quoting any Clojure form tells Clojure not to evaluate it but to treat it as data.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Storing Objects with def

;; - Here’s an example of def in action (interning a var):

		(def great-books ["East of Eden" "The Glass Bead Game"])
  ; => #'user/great-books

;; - This code tells Clojure:

;;     1. Update the current namespace’s map with the association between great-books and the var.
;;     2. Find a free storage shelf.
;;     3. Store ["East of Eden" "The Glass Bead Game"] on the shelf.
;;     4. Write the address of the shelf on the var.
;;     5. Return the var (in this case, #'user/great-books).

;; - Here’s how you’d get a map of interned vars:

		(ns-interns *ns*)
  ; => {great-books #'user/great-books}

;; - #'user/great-books lets you use the var associated with the symbol great-books within the user namespace.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Creating and Switching to Namespaces

;; - Clojure has three tools for creating namespaces: the function create-ns, the function in-ns, and the macro ns.

;; - create-ns takes a symbol, creates a namespace with that name if it doesn’t exist already, and returns the namespace.

;; - Using in-ns is more common because it creates the namespace if it doesn’t exist and switches to it.

;; - To use functions and data from others namespaces, you can use a fully qualified symbol. The general form is namespace/name.

;; - Using refer is as if Clojure:

;;     1. Calls ns-interns on the cheese.taxonomy namespace
;;     2. Merges that with the ns-map of the current namespace
;;     3. Makes the result the new ns-map of the current namespace

;; - When you call refer, you can also pass it the filters :only, :exclude, and :rename.

;; - :only and :exclude restrict which symbol/var mappings get merged into the current namespace’s ns-map.

;; - :rename lets you use different symbols for the vars being merged in.

;; - The REPL automatically refers clojure.core within the user namespace.

;; - You can make your life easier by evaluating (clojure.core/refer-clojure) when you create a new namespace.

;; - You can make a function available only to other functions within the same namespace. You to define private functions using defn-:

		(in-ns 'cheese.analysis)
  ;; Notice the dash after "defn"
		(defn- private-function
		  "Just an example function that does nothing"
		  [])

;; - alias let you shorten a namespace name for using fully qualified symbols:

		cheese.analysis=> (clojure.core/alias 'taxonomy 'cheese.taxonomy)
		cheese.analysis=> taxonomy/bries
		; => ["Wisconsin" "Somerset" "Brie de Meaux" "Brie de Melun"]

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Real Project Organization

;; - For example, the name of the namespace is the-divine-cheese-code.core. 

;; - There’s a one-to-one mapping between a namespace name and the path of the file where the namespace is declared, according to the following conventions:

;;     1. When you create a directory with lein, the source code’s root is src by default.
;;     2. Dashes in namespace names correspond to underscores in the file­system.
;;     3. The component preceding a period (.) in a namespace name corresponds to a directory.
;;     4. The final component of a namespace corresponds to a file with the .clj extension.

;; - require takes a symbol designating a namespace and ensures that the namespace exists and is ready to be used.

;; - Even though the file you want to require is in your project’s directory, Clojure doesn’t automatically evaluate it when it runs your project.

;; - After requiring the namespace, you can refer it so that you don’t have to use fully qualified names to reference the functions.

;; - require tells Clojure the following:

;;     1. Do nothing if you’ve already called require with this symbol.
;;     2. Otherwise, find the file that corresponds to this symbol using the file path/namespace name rules.
;;     3. Read and evaluate the contents of that file. Clojure expects the file to declare a namespace corresponding to its path.

;; - require also lets you alias a namespace when you require it, using :as or alias:

		(require '[the-divine-cheese-code.visualization.svg :as svg])

;; - Instead of calling require and refer separately, the function use does both.

		(use 'the-divine-cheese-code.visualization.svg)

;; - You can alias a namespace with use just like you can with require:

		(use '[the-divine-cheese-code.visualization.svg :as svg])
		(= svg/points points)
		; => true

;; - Aliasing a namespace after you use it lets you refer to symbols that you excluded.

		(use '[the-divine-cheese-code.visualization.svg :as svg :only [points]])

		(= svg/points points)
		; => true

		;; We can use the alias to reach latlng->point
		svg/latlng->point
		; This doesn't throw an exception

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The ns macro

;; - One useful task ns does is refer the clojure.core namespace by default.

;; - You can control what gets referred from clojure-core with :refer-clojure:

		(ns the-divine-cheese-code.core
		  (:refer-clojure :exclude [println]))

;; - Within ns, the form (:refer-clojure) is called a reference.

;; - There are six possible kinds of references within ns: (:refer-clojure), (:require), (:use), (:import), (:load) and (:gen-class).

;; - (:require) works a lot like the require function:

		(ns the-divine-cheese-code.core
		  (:require the-divine-cheese-code.visualization.svg))

;; - Notice that in the ns form (unlike the in-ns function call), you don’t have to quote your symbol with '.

;; - You can also alias a library that you require within ns:

		(ns the-divine-cheese-code.core
		  (:require [the-divine-cheese-code.visualization.svg :as svg]))

;; - You can require multiple libraries in a (:require) reference:

		(ns the-divine-cheese-code.core
		  (:require [the-divine-cheese-code.visualization.svg :as svg]
		            [clojure.java.browse :as browse]))

;; - One difference between the (:require) reference and the require function is that the reference also allows you to refer names:

		(ns the-divine-cheese-code.core
		  (:require [the-divine-cheese-code.visualization.svg :refer [points]]))

;; - You can also refer all symbols (notice the :all keyword):

		(ns the-divine-cheese-code.core
		  (:require [the-divine-cheese-code.visualization.svg :refer :all]))

;; - It’s recommended that you not use (:use). This:

		(ns the-divine-cheese-code.core
		  (:use [clojure.java browse io]))

;; - Does this:

		(in-ns 'the-divine-cheese-code.core)
		(use 'clojure.java.browse)
		(use 'clojure.java.io)

;; - When you follow :use with a vector, it takes the first symbol as the base and then calls use with each symbol that follows.










