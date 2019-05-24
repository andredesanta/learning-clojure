;; MULTIMETHODS, PROTOCOLS AND RECORDS

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - In Clojure, an abstraction is a collection of operations, and data types implement abstractions.

;; - The seq abstraction consists of operations like first and rest, and the vector data type is an implementation of that abstraction.

;; - A specific vector like [:seltzer :water] is an instance of that data type.

;; - The more a programming language lets you think and write in terms of abstractions, the more productive you will be.

;; - If you extend a data structure to work with the seq abstraction, you can use the extensive library of seq functions on it.

;; - As a result, you spend time actually using the data structure instead of constantly looking up documentation on how it works.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Polymorphism

;; - The main way we achieve abstraction in Clojure is by associating an operation name with more than one algorithm. This technique is
;; called poly­morphism.

;; - The algorithm for performing conj on a list is different from the one for vectors, but we unify them under the same name to 
;; indicate that they implement the same concept, namely, add an element to this data structure.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Multimethods

;; - Using multimethods, you associate a name with multiple implementations by defining a dispatching function.

;; - A dispatching function produces dispatching values that are used to determine which method to use.

;; - when you call a multimethod, the dispatching function will interrogate the arguments and send them to the right method:

		(ns were-creatures)
		  (defmulti full-moon-behavior (fn [were-creature] (:were-type were-creature)))

		  (defmethod full-moon-behavior :wolf
		  [were-creature]
		  (str (:name were-creature) " will howl and murder"))
		  
		  (defmethod full-moon-behavior :simmons
		  [were-creature]
		  (str (:name were-creature) " will encourage people and sweat to the oldies"))

		(full-moon-behavior {:were-type :wolf
		                       :name "Rachel from next door"})
		; => "Rachel from next door will howl and murder"

		(full-moon-behavior {:name "Andy the baker"
		                       :were-type :simmons})
		; => "Andy the baker will encourage people and sweat to the oldies"

;; - Whenever someone calls full-moon-behavior, run the dispatching function (fn [were-creature] (:were-type were-creature)) on
;; the arguments.

;; - Use the result of that function, aka the dispatching value, to decide which specific method to use.

;; - The method name is immediately followed by the dispatch value. :wolf and :simmons are both dispatch values.

;; - Dispatch value is different from a dispatching value, which is what the dispatching function returns.

;; - The full dispatch sequence goes like this:

;;    1. The form (full-moon-behavior {:were-type :wolf :name "Rachel from next door"}) is evaluated.
;;    2. full-moon-behavior’s dispatching function runs, returning :wolf as the dispatching value.
;;    3. Clojure compares the dispatching value :wolf to the dispatch values of all the methods defined
;;       for full-moon-behavior. The dispatch values are :wolf and :simmons.
;;    4. Because the dispatching value :wolf is equal to the dispatch value :wolf, the algorithm for :wolf runs.

;; - The main idea is that the dispatching function returns some value, and this value is used to determine which method definition
;; to use.

;; - You can define a method with nil as the dispatch value:

		(defmethod full-moon-behavior nil
		  [were-creature]
		  (str (:name were-creature) " will stay at home and eat ice cream"))

		(full-moon-behavior {:were-type nil
		                     :name "Martin the nurse"})
		; => "Martin the nurse will stay at home and eat ice cream"

;; - You can also define a default method to use if no other methods match by specifying :default as the dispatch value.

		(defmethod full-moon-behavior :default
		  [were-creature]
		  (str (:name were-creature) " will stay up all night fantasy footballing"))

		(full-moon-behavior {:were-type :office-worker
		                     :name "Jimmy from sales"})
		; => "Jimmy from sales will stay up all night fantasy footballing"

;; - One cool thing about multimethods is that you can always add new methods.

;; - This example shows that you’re creating your own random namespace and including the were-creatures namespace, and then
;; defining another method for the full-moon-behavior multimethod:

		(ns random-namespace
		  (:require [were-creatures]))
		(defmethod were-creatures/full-moon-behavior :bill-murray
		  [were-creature]
		  (str (:name were-creature) " will be the most likeable celebrity"))
		(were-creatures/full-moon-behavior {:name "Laura the intern" 
		                                    :were-type :bill-murray})
		; => "Laura the intern will be the most likeable celebrity"

;; - Your dispatching function can return arbitrary values using any or all of its arguments.

;; - The next example defines a multimethod that takes two arguments and returns a vector containing the type of each argument.

;; - It also defines an implementation of that method, which will be called when each argument is a string:

		(ns user)
		(defmulti types (fn [x y] [(class x) (class y)]))
		(defmethod types [java.lang.String java.lang.String]
		  [x y]
		  "Two strings!")

		(types "String 1" "String 2")
		; => "Two strings!"

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Protocols

;; - Protocols are optimized for type dispatch.

;; - A multimethod is just one polymorphic operation, whereas a protocol is a collection of one or more polymorphic operations.

;; - Protocol operations are called methods, just like multimethod operations. 

;; - Protocol methods are dispatched based on the type of the first argument, as shown in this example:

		(ns data-psychology)
		 (defprotocol Psychodynamics
		   "Plumb the inner depths of your data types"
		   (thoughts [x] "The data type's innermost thoughts")
		   (feelings-about [x] [x y] "Feelings about self or other"))

;; - The defprotocol takes a name (Psychodynamics) and an optional docstring.

;; - A method signature consists of a name, an argument specification, and an optional docstring.

;; - The second method signture can take one or two arguments.

;; - Protocols do have one limitation: the methods can’t have rest arguments.

;; - By defining a protocol, you’re defining an abstraction and you need to define how that abstraction is implemented.

;; - Protocols dispatch on the first argument’s type, so when you call (thoughts "blorb"), Clojure tries to look up the 
;; implementation of the thoughts method for strings.

;; In this case, you implement the Psychodynamics protocol by exceding the string data type:

		(extend-type java.lang.String
	   Psychodynamics
	   (thoughts [x] (str x " thinks, 'Truly, the character defines the data type'")
	   (feelings-about
	     ([x] (str x " is longing for a simpler way of life"))
	     ([x y] (str x " is envious of " y "'s simpler way of life")))))

		(thoughts "blorb")
		; => "blorb thinks, 'Truly, the character defines the data type'"

		(feelings-about "schmorb")
		; => "schmorb is longing for a simpler way of life"

		(feelings-about "schmorb" 2)
		; => "schmorb is envious of 2's simpler way of life"

;; - extend-type is followed by the name of the class or type you want to extend and the protocol you want it to support.

;; - After that, you provide an implementation for both the thoughts method and the feelings-about method.

;; - If you’re extending a type to implement a protocol, you have to implement every method in the protocol or Clojure will
;; throw an exception.

;; - To define a method implementation, you write a form that starts with the method’s name, like thoughts, then supply a
;; vector of parameters and the method’s body.

;; - These methods also allow arity overloading, just like functions, and you define multiple-arity method implementations
;; similarly to multiple-arity functions.

;; - You can extend java.lang.Object to provide a default implementation. This works because every type in Java (and hence, Clojure)
;; is a descendant of java.lang.Object.

		(extend-type java.lang.Object
		  Psychodynamics
		  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
		  (feelings-about
		    ([x] "meh")
		    ([x y] (str "meh about " y))))
		  
		(thoughts 3)
		; => "Maybe the Internet is just a vector for toxoplasmosis"

		(feelings-about 3)
		; => "meh"

		(feelings-about 3 "blorb")
		; => "meh about blorb"

;; - Instead of making multiple calls to extend-type to extend multiple types, you can use extend-protocol, which lets you define
;; protocol implementations for multiple types at once.

		(extend-protocol Psychodynamics
		  java.lang.String
		  (thoughts [x] "Truly, the character defines the data type")
		  (feelings-about
		    ([x] "longing for a simpler way of life")
		    ([x y] (str "envious of " y "'s simpler way of life")))
		  
		  java.lang.Object
		  (thoughts [x] "Maybe the Internet is just a vector for toxoplasmosis")
		  (feelings-about
		    ([x] "meh")
		    ([x y] (str "meh about " y))))

;; - A protocol’s methods “belong” to the namespace that they’re defined in.

;; - In these examples, the fully qualified names of the Psychodynamics methods are data-psychology/thoughts and
;; data-psychology/feelings-about. 

;; - If you want two different protocols to include methods with the same name, you’ll need to put the protocols in
;; different namespaces.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Records

;; - Records are custom, maplike data types.

;; - They associate keys with values, you can look up their values the same way you can with maps and they’re immutable like maps.

;; - You specify fields for records. Fields are slots for data; using them is like specifying which keys a data structure should have.

;; - Records are also different from maps in that you can extend them to implement protocols.

;; - To create a record, you use defrecord to specify its name and fields:

		(ns were-records)
		(defrecord WereWolf [name title])

;; - This record’s name is WereWolf, and its two fields are name and title. You can create an instance of this record in three ways:

		(WereWolf. "David" "London Tourist")
		; => #were_records.WereWolf{:name "David", :title "London Tourist"}

		(->WereWolf "Jacob" "Lead Shirt Discarder")
		; => #were_records.WereWolf{:name "Jacob", :title "Lead Shirt Discarder"}

		(map->WereWolf {:name "Lucian" :title "CEO of Melodrama"})
		; => #were_records.WereWolf{:name "Lucian", :title "CEO of Melodrama"}

;; - First, we create an instance the same way we’d create a Java object, using the class instantiation interop call.

;; - ->WereWolf is a function. When you create a record, the factory functions ->RecordName and map->RecordName are created automatically.

;; - map->WereWolf takes a map as an argument with keywords that correspond to the record type’s fields and returns a record.

;; - If you want to use a record type in another namespace, you’ll have to import it. Be careful to replace all dashes in the namespace
;; with underscores.

		(ns monster-mash
		  (:import [were_records WereWolf]))
		(WereWolf. "David" "London Tourist")
		; => #were_records.WereWolf{:name "David", :title "London Tourist"}

;; - You can look up record values in the same way you look up map values, and you can also use Java field access interop:

		(def jacob (->WereWolf "Jacob" "Lead Shirt Discarder"))
		(.name jacob) 
		; => "Jacob"

		(:name jacob) 
		; => "Jacob"

		(get jacob :name) 
		; => "Jacob"

;; - When testing for equality, Clojure will check that all fields are equal and that the two comparands have the same type:

		(= jacob (->WereWolf "Jacob" "Lead Shirt Discarder"))
		; => true

		(= jacob (WereWolf. "David" "London Tourist"))
		; => false

		(= jacob {:name "Jacob" :title "Lead Shirt Discarder"})
		; => false

;; - Any function you can use on a map, you can also use on a record:

		(assoc jacob :title "Lead Third Wheel")
		; => #were_records.WereWolf{:name "Jacob", :title "Lead Third Wheel"}

;; - However, if you dissoc a field, the result’s type will be a plain ol’ Clojure map:

		(dissoc jacob :title)
		; => {:name "Jacob"} <- that's not a were_records.WereWolf

;; - Accessing map values is slower than accessing record values, so watch out if you’re building a high-performance program.

;; - When you create a new record type, you can extend it to implement a protocol, similar to how you extended a type using
;; extend-type earlier.

;; - If you dissoc a record and then try to call a protocol method on the result, the record’s protocol method won’t be called.

;; - Here’s how you would extend a protocol when defining a record:

		(defprotocol WereCreature
		  (full-moon-behavior [x]))

		(defrecord WereWolf [name title]
		WereCreature
		(full-moon-behavior [x]
		  (str name " will howl and murder")))

		(full-moon-behavior (map->WereWolf {:name "Lucian" :title "CEO of Melodrama"}))
		; => "Lucian will howl and murder"

;; - We’ve created a new protocol, WereCreature, with one method, full-moon-behavior. 

;; - defrecord implements WereCreature for WereWolf.

;; - The most interesting part of the full-moon-behavior implementation is that you have access to the fields defined for the record.

;; - You can also extend records using extend-type and extend-protocol.

;; - In general, you should consider using records if you find yourself creating maps with the same fields over and over.

;; - Record access is more performant than map access, so your program will become a bit more efficient.

;; - If you want to use protocols, you’ll need to create a record.
