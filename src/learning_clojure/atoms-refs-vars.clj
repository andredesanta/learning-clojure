;;	ATOMS, REFS AND VARS

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Clojure metaphysics:

;; - Values are atomic: they’re indivisible, unchanging and stable entities.

;; - A value doesn’t change, but you can apply a process to a value to produce a new value.

;; - Identity is a succession of unchanging values produced by a process over time. Names designate identities.

;; - State means the value of an identity at a point in time.

;; - Change only occurs when a process generates a new value and we choose to associate the identity with the new value.

;; - Reference types let you manage identities in Clojure. Using them, you can name an identity and retrieve its state.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Atoms:

;; - Atoms implement Clojure’s concept of state.

;; - Clojure’s atom reference type allows you to endow a succession of related values with an identity.

;; - This atom refers to the value {:cuddle-hunger-level 0 :percent-deteriorated 0}, and you would say that that’s its current state:

		(def fred (atom {:cuddle-hunger-level 0
                   :percent-deteriorated 0}))

;; - To get an atom’s current state, you dereference it:

		@fred
; => {:cuddle-hunger-level 0, :percent-deteriorated 0}

;; - When you dereference a reference type, the operation doesn’t block, because it doesn’t have to wait for anything.

;; - Here’s how you could log a zombie’s state with println:

		(let [zombie-state @fred]
		  (if (>= (:percent-deteriorated zombie-state) 50)
		    (future (println (:cuddle-hunger-level zombie-state)))))

;; - By using atoms to refer to immutable data structures, you only have to perform one read, and the data structure returned won’t 
;; get altered by another thread.

;; - The atom reference type is a construct that refers to atomic values.

;; - By updating the atom, the atomic values don’t change, but the reference type can be updated and assigned a new value.

;; - swap! applies the function to the atom’s current state to produce a new value, and then it updates the atom to refer to this
;; new value.

		(swap! fred
		       (fn [current-state]
		         (merge-with + current-state {:cuddle-hunger-level 1
		                                      :percent-deteriorated 1})))
;          => {:cuddle-hunger-level 1, :percent-deteriorated 1}

;; - You can also pass swap! a function that takes multiple arguments:

		(defn increase-cuddle-hunger-level
		  [zombie-state increase-by]
		  (merge-with + zombie-state {:cuddle-hunger-level increase-by}))

;; - This code doesn’t actually update fred, because we’re not using swap!:

		(increase-cuddle-hunger-level @fred 10)
; => {:cuddle-hunger-level 11, :percent-deteriorated 1}

;; - Call swap! with the additional arguments, and @fred will be updated:

		(swap! fred increase-cuddle-hunger-level 10)
; => {:cuddle-hunger-level 11, :percent-deteriorated 1}

		@fred
; => {:cuddle-hunger-level 11, :percent-deteriorated 1}

;; - You cand update the atom using Clojure's built-in functions.

;; - The update-in function takes three arguments: a collection, a vector for identifying which value to update, and a function
;; to update that value:

		(update-in {:a {:b 3}} [:a :b] + 10)
; => {:a {:b 13}}

		(swap! fred update-in [:cuddle-hunger-level] + 10)
; => {:cuddle-hunger-level 22, :percent-deteriorated 1}

;; - You can dereference an atom to retrieve State 1, and then update the atom, creating State 2, and still make use of State 1:

		(let [num (atom 1)
		      s1 @num]
		  (swap! num inc)
		  (println "State 1:" s1)
		  (println "Current state:" @num))
;   => State 1: 1
;   => Current state: 2

;; - Solution to the reference cell and mutual exclusion problems: swap! implements compare-and-set semantics, meaning it does the 
;; following internally:

;;    1. It reads the current state of the atom.
;;    2. It then applies the update function to that state.
;;    3. Next, it checks whether the value it read in step 1 is identical to the atom’s current value.
;;    4. If it is, then swap! updates the atom to refer to the result of step 2.
;;    5. If it isn’t, then swap! retries, going through the process again with step 1.

;; - Atom updates happen synchronously and they will block their thread.

;; - Updating an atom without checking its current value:

		(reset! fred {:cuddle-hunger-level 0
		              :percent-deteriorated 0})

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Watches:

;; - Watches allow you to check in on your reference types’ every move.

;; - Watch functions take four arguments: a key that you can use for reporting, the atom being watched, the state of the atom before
;;  its update, and the state of the atom after its update.

(defn shuffle-speed
  [zombie]
  (* (:cuddle-hunger-level zombie)
     (- 100 (:percent-deteriorated zombie))))

(defn shuffle-alert
  [key watched old-state new-state]
  (let [sph (shuffle-speed new-state)]
    (if (> sph 5000)
      (do
        (println "Run, you fool!")
        (println "The zombie's SPH is now " sph)
        (println "This message brought to your courtesy of " key))
      (do
        (println "All's well with " key)
        (println "Cuddle hunger: " (:cuddle-hunger-level new-state))
        (println "Percent deteriorated: " (:percent-deteriorated new-state))
        (println "SPH: " sph)))))

;; - You can attach this function to fred with add-watch. The general form of add-watch is (add-watch ref key watch-fn).

;; - This example watch function didn’t use watched or old-state, but they’re there for you if the need arises.

		(reset! fred {:cuddle-hunger-level 22
		              :percent-deteriorated 2})

		(add-watch fred :fred-shuffle-alert shuffle-alert)

		(swap! fred update-in [:percent-deteriorated] + 1)
; => All's well with  :fred-shuffle-alert
; => Cuddle hunger:  22
; => Percent deteriorated:  3
; => SPH:  2134

		(swap! fred update-in [:cuddle-hunger-level] + 30)
; => Run, you fool!
; => The zombie's SPH is now 5044
; => This message brought to your courtesy of :fred-shuffle-alert

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Validator:

;; - Validators allow you to restrict what states are allowable for a reference.

;; - When you add a validator to a reference, the reference is modified so that, whenever it’s updated, it will call this validator 
;; with the value returned from the update function as its argument.

;; - If the validator fails by returning false or throwing an exception, the reference won’t change to point to the new value.

;; - Attaching a validator during atom creation:

		(defn percent-deteriorated-validator
		  [{:keys [percent-deteriorated]}]
		  (and (>= percent-deteriorated 0)
		       (<= percent-deteriorated 100)))

		(def bobby
		  (atom
		   {:cuddle-hunger-level 0 :percent-deteriorated 0}
		    :validator percent-deteriorated-validator))

		(swap! bobby update-in [:percent-deteriorated] + 200)
;  This throws "Invalid reference state"

;; - You can throw an exception to get a more descriptive error message:

		(defn percent-deteriorated-validator
		  [{:keys [percent-deteriorated]}]
		  (or (and (>= percent-deteriorated 0)
		           (<= percent-deteriorated 100))
		      (throw (IllegalStateException. "That's not mathy!"))))

		(def bobby
		  (atom
		   {:cuddle-hunger-level 0 :percent-deteriorated 0}
		    :validator percent-deteriorated-validator))

		(swap! bobby update-in [:percent-deteriorated] + 200)
;  This throws "IllegalStateException: That's not mathy!"

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Refs:

;; - Refs allow you to update the state of multiple identities using transaction semantics.

;; - These transactions have three features:

;;    1. They are atomic, meaning that all refs are updated or none of them are.
;;    2. They are consistent, meaning that the refs always appear to have valid states. A sock will always belong to a dryer or a gnome,
;;       but never both or neither.
;;    3. They are isolated, meaning that transactions behave as if they executed serially. If two threads are simultaneously running 
;;       transactions that alter the same ref, one transaction will retry.

;; - Modeling Sock Transfers (example):

		(def sock-varieties
		  #{"darned" "argyle" "wool" "horsehair" "mulleted"
		    "passive-aggressive" "striped" "polka-dotted"
		    "athletic" "business" "power" "invisible" "gollumed"})

		(defn sock-count
		  [sock-variety count]
		  {:variety sock-variety
		   :count count})

		(defn generate-sock-gnome
		  "Create an initial sock gnome state with no socks"
		  [name]
		  {:name name
		   :socks #{}})

		(def sock-gnome (ref (generate-sock-gnome "Barumpharumph")))

		(def dryer (ref {:name "LG 1337"
		                 :socks (set (map #(sock-count % 2) sock-varieties))}))

;; - Dereferencing a ref:

		(:socks @dryer)
; => #{{:variety "passive-aggressive", :count 2} {:variety "power", :count 2}
;       {:variety "athletic", :count 2} {:variety "business", :count 2}
;       {:variety "argyle", :count 2} {:variety "horsehair", :count 2}
;       {:variety "gollumed", :count 2} {:variety "darned", :count 2}
;       {:variety "polka-dotted", :count 2} {:variety "wool", :count 2}
;       {:variety "mulleted", :count 2} {:variety "striped", :count 2}
;       {:variety "invisible", :count 2}}

;; - You can modify refs using alter, and you must use alter within a transaction. 

;; - dosync initiates a transaction and defines its extent. You put all transaction operations in its body.

		(defn steal-sock
		  [gnome dryer]
		  (dosync
		   (when-let [pair (some #(if (= (:count %) 2) %) (:socks @dryer))]
		     (let [updated-count (sock-count (:variety pair) 1)]
		       (alter gnome update-in [:socks] conj updated-count)
		       (alter dryer update-in [:socks] disj pair)
		       (alter dryer update-in [:socks] conj updated-count)))))

		(steal-sock sock-gnome dryer)

		(:socks @sock-gnome)
; => #{{:variety "passive-aggressive", :count 1}}

;; - When you alter a ref, the change isn’t immediately visible outside of the current transaction. 

;; - This is what lets you call alter on the dryer twice within a transaction without worry­ing about whether dryer will be read 
;; in an inconsistent state.

;; - If you alter a ref and then deref it within the same transaction, the deref will return the new state.

;; - Example to demonstrate this idea of in-transaction state:

		(def counter (ref 0))

		(future
		  (dosync
		   (alter counter inc)
		   (println @counter)
		   (Thread/sleep 500)
		   (alter counter inc)
		   (println @counter)))

		(Thread/sleep 250)
		
		(println @counter)
;  This prints 1, 0 , and 2, in that order.

;; - The transaction will try to commit its changes only when it ends. The commit works similarly to the compare-and-set
;; semantics of atoms.

;; - Each ref is checked to see whether it’s changed since you first tried to alter it. 

;; - If any of the refs have changed, then none of the refs is updated and the transaction is retried.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Commute:

;; - Allows you to update a ref’s state within a transaction, just like alter.

;; - Here’s how alter behaves:

;;     1. Reach outside the transaction and read the ref’s current state.
;;     2. Compare the current state to the state the ref started with within the transaction.
;;     3. If the two differ, make the transaction retry.
;;     4. Otherwise, commit the altered ref state.

;; - commute behaves like this at commit time:

;;     1. Reach outside the transaction and read the ref’s current state.
;;     2. Run the commute function again using the current state.
;;     3. Commit the result.

;; - commute doesn’t force a transaction retry, what can help improve performance.

;; - It’s important that you only use commute when you’re sure that it’s not possible for your refs to end up in an invalid state.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Dynamic Vars 

;; - vars are associations between symbols and objects. You create new vars with def.

;; - You can create a dynamic var whose binding can be changed.

;; - Dynamic vars can be useful for creating a global name that should refer to different values in different contexts.

;; - Creating a dynamic var:

		(def ^:dynamic *notification-address* "dobby@elf.org")

;; - You use ^:dynamic to signal to Clojure that a var is dynamic.

;; - Clojure requires you to enclose the names of dynamic vars in earmuffs (asterisks). This helps signal the var’s dynamicaltude 
;; to other programmers.

;; - You can temporarily change the value of dynamic vars by using binding:

		(binding [*notification-address* "tester-1@elf.org"]
		  (println *notification-address*)
		  (binding [*notification-address* "tester-2@elf.org"]
		    (println *notification-address*))
		  (println *notification-address*))
;   => tester-1@elf.org
;   => tester-2@elf.org
;   => tester-1@elf.org

;; - Example of dynamic var:

		(defn notify
		  [message]
		  (str "TO: " *notification-address* "\n"
		       "MESSAGE: " message))
		(notify "I fell.")
; => "TO: dobby@elf.org\nMESSAGE: I fell."

		(binding [*notification-address* "test@elf.org"]
		  (notify "test!"))
; => "TO: test@elf.org\nMESSAGE: test!"

;; - Dynamic vars are most often used to name a resource that one or more functions target. Clojure comes with a ton of built-in 
;; dynamic vars for this purpose.

;; - *out*, for example, represents the standard output for print operations.

;; - In your program, you could re-bind *out* so that print statements write to a file:

		(binding [*out* (clojure.java.io/writer "print-output")]
		  (println "A man who carries a cat by the tail learns 
		something he can learn in no other way.
		-- Mark Twain"))
		(slurp "print-output")
; => A man who carries a cat by the tail learns
;     something he can learn in no other way.
;     -- Mark Twain

;; - Dynamic vars are also used for configuration. For example, the built-in var *print-length* allows you to specify how many items
;; in a collection Clojure should print:

		(println ["Print" "all" "the" "things!"])
;  => [Print all the things!]

		(binding [*print-length* 1]
		  (println ["Print" "just" "one!"]))
;  => [Print ...]

;; - It’s possible to set! dynamic vars that have been bound. 

;; - set! allows you convey information out of a function without having to return it as an argument.

;; - thread-bound? returns true if all of the vars provided as arguments have thread-local bindings.

;; - Notice that you have to pass #'*troll-thought*. This is because thread-bound? takes the var itself as an argument, not the value
;; it refers to.

		(def ^:dynamic *troll-thought* nil)
		(defn troll-riddle
		  [your-answer]
		  (let [number "man meat"]
		     (when (thread-bound? #'*troll-thought*)
		       (set! *troll-thought* number))
		    (if (= number your-answer)
		      "TROLL: You can cross the bridge!"
		      "TROLL: Time to eat you, succulent human!")))

		(binding [*troll-thought* nil]
		  (println (troll-riddle 2))
		  (println "SUCCULENT HUMAN: Oooooh! The answer was" *troll-thought*))

;   => TROLL: Time to eat you, succulent human!
;   => SUCCULENT HUMAN: Oooooh! The answer was man meat

		*troll-thought*
; => nil

;; - If you access a dynamically bound var from within a manually created thread, the var will evaluate to the original value.

;; - Bindings don’t get passed on to manually created threads. They do, however, get passed on to futures.

;; - The REPL binds *out*. If you create a new thread, *out* won’t be bound to the REPL printer.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Altering the Var Root

;; - When you create a new var, the initial value that you supply is its root:

		(def power-source "hair")

;; - In this example, "hair" is the root value of power-source. 

;; - Clojure lets you permanently change this root value with the function alter-var-root:

		(alter-var-root #'power-source (fn [_] "7-eleven parking lot"))
		power-source
; => "7-eleven parking lot"

;; - Just like when using swap! to update an atom or alter! to update a ref, you use alter-var-root along with a function to update 
;; the state of a var.

;; - You’ll hardly ever want to do this. You especially don’t want to do this to perform simple variable assignment.

;; - If you did, you’d be going out of your way to create the binding as a mutable variable, which goes against Clojure’s philosophy.

;; - You can also temporarily alter a var’s root with with-redefs. This works similarly to binding except the alteration will appear 
;; in child threads:

		(with-redefs [*out* *out*]
		        (doto (Thread. #(println "with redefs allows me to show up in the REPL"))
		          .start
		          .join))

;; - with-redefs can be used with any var, not just dynamic ones. Because it has has such far-reaching effects, you should only use
;;  it during testing.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Stateless Concurrency and Parallelism with pmap

;; - With pmap, Clojure handles the running of each application of the map function on a separate thread.

;; - The repeatedly function takes another function as an argument and returns a lazy sequence:

		(defn always-1
		  []
		  1)
		(take 5 (repeatedly always-1))
; => (1 1 1 1 1)

		(take 5 (repeatedly (partial rand-int 10)))
; => (1 5 0 3 4)

;; - Let’s use repeatedly to create example data that consists of a sequence of 3000 random strings, each 7000 characters long.

		(def alphabet-length 26)

;; Vector of chars, A-Z
		(def letters (mapv (comp str char (partial + 65)) (range alphabet-length)))

		(defn random-string
		  "Returns a random string of specified length"
		  [length]
		  (apply str (take length (repeatedly #(rand-nth letters)))))
		  
		(defn random-string-list
		  [list-length string-length]
		  (doall (take list-length (repeatedly (partial random-string string-length)))))

		(def orc-names (random-string-list 3000 7000))

;; - The dorun function realizes the sequence but returns nil:

		(time (dorun (map clojure.string/lower-case orc-names)))
; => "Elapsed time: 270.182 msecs"

		(time (dorun (pmap clojure.string/lower-case orc-names)))
; => "Elapsed time: 147.562 msecs"

;; - The serial execution with map took about 1.8 times longer than pmap.

;; - Grain size: the amount of work done by each parallelized task.






















