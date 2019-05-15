;; DESTRUCTURING IN CLOJURE

;; Source: blog.brunobonacci.com/2014/11/16/clojure-complete-guide-to-destructuring/

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring allows you to separate a structured value into its constituent parts.

;; - Consider the following code:

		 (defn current-position []
	  	[51.503331, -0.119500])

	 (defn geohash [lat lng]
				(println "geohash:" lat lng)
;;  this function take two separate values as params.
;;  and it return a geohash for that position
		)

		(let [coord (current-position)
		      lat   (first coord)
		      lng   (second coord)]
				(geohash lat lng))

;;  geohash: 51.503331 -0.1195

;; - The code below does the same thing using destructuring:

		(let [[lat lng] (current-position)]
		  (geohash lat lng))

;;  geohash: 51.503331 -0.1195

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring of lists, vectors and sequences:

		(let [[one two three] [1 2 3]]
		  (println "one:" one)
		  (println "two:" two)
		  (println "three:" three))

		(let [[one two three] '(1 2 3)]
		  (println "one:" one)
		  (println "two:" two)
		  (println "three:" three))

		(let [[one two three] (range 1 4)]
		  (println "one:" one)
		  (println "two:" two)
		  (println "three:" three))

;;   one: 1
;;   two: 2
;;   three: 3

;; - If you are not interested in all values you can capture only the ones you are interested in and ignore the others by putting an 
;; underscore (_) as a placeholder for its value:

		(let [[_ _ three] (range 1 10)]
		  three)
;;  => 3

;; - Capturing all the remaining numbers as a sequence:

		(let [[_ _ three & numbers] (range 1 10)]
		  numbers)
;;  => (4 5 6 7 8 9)

;; - By using the clause :a followed by a symbol, you can keep the full structured parameter as it was originally:

		(let [[_ _ three & numbers :as all-numbers] (range 1 10)]
		  all-numbers)
;;  => (1 2 3 4 5 6 7 8 9)

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Maps destructuring:

;; - Writing the code without destructuring:

		(defn current-position []
		  {:lat 51.503331, :lng -0.119500})

		(let [coord (current-position)
		      lat   (:lat coord)
		      lng   (:lng coord)]
		  (geohash lat lng))

;;  geohash: 51.503331 -0.1195

;; - Using a basic type of destructuring and some repetition:

		(let [{lat :lat, lng :lng} (current-position)]
		  (geohash lat lng))

;;  geohash: 51.503331 -0.1195

;; - Most common way of destructuring Clojure's maps:

		(let [{:keys [lat lng]} (current-position)]
		  (geohash lat lng))

;;  geohash: 51.503331 -0.1195

;; - Both map destructuring methods allow you the retain the entire map with the :as clause in the same way of the lists:

		(let [{lat :lat, lng :lng :as coord} (current-position)]
		  (println "calculating geohash for coordinates: " coord)
		  (geohash lat lng))

;;  calculating geohash for coordinates:  {:lat 51.503331, :lng -0.1195}
;;  geohash: 51.503331 -0.1195


		(let [{:keys [lat lng] :as coord} (current-position)]
		  (println "calculating geohash for coordinates: " coord)
		  (geohash lat lng))

;;  calculating geohash for coordinates:  {:lat 51.503331, :lng -0.1195}
;;  geohash: 51.503331 -0.1195

;; - You can also specify the keys as keywords:

		(let [{:keys [:lat :lng] :as coord} (current-position)]
		  (println "calculating geohash for coordinates: " coord)
		  (geohash lat lng))

;;  calculating geohash for coordinates:  {:lat 51.503331, :lng -0.1195}
;;  geohash: 51.503331 -0.1195

;; - If the keys in the map are strings, you must use :strs:

		(let [{:strs [lat lng] :as coord} {"lat" 51.503331, "lng" -0.119500}]
		  (println "calculating geohash for coordinates: " coord)
		  (geohash lat lng))

;;  calculating geohash for coordinates:  {lat 51.503331, lng -0.1195}
;;  geohash: 51.503331 -0.1195

;; - If the keys in the map are symbols, you must use :syms:

		(let [{:syms [lat lng] :as coord} {'lat 51.503331, 'lng -0.119500}]
		  (println "calculating geohash for coordinates: " coord)
		  (geohash lat lng))

;;  calculating geohash for coordinates:  {lat 51.503331, lng -0.1195}
;;  geohash: 51.503331 -0.1195

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring maps with default values

;; - Merging the parameters passed to the function with the default values.

		(defn connect-db [{:keys [host port db-name username password]
		                   :or   {host     "localhost"
		                          port     12345
		                          db-name  "my-db"
		                          username "db-user"
		                          password "secret"}
		                   :as cfg}]
		   (println "connecting to:" host "port:" port "db-name:" db-name
		            "username:" username "password:" password))

		(connect-db {:host "server"})
;; connecting to: server port: 12345 db-name: my-db username: db-user password: secret

		(connect-db {:host "server" :username "user2" :password "Passowrd1"})
;; connecting to: server port: 12345 db-name: my-db username: user2 password: Passowrd1

;; - The function above with a mandatory parameter (when you need the user to enter at least one parameter):

		(defn connect-db [host ; mandatory parameter
		                  & {:keys [port db-name username password]
		                     :or   {port     12345
		                            db-name  "my-db"
		                            username "db-user"
		                            password "secret"}}]
		   (println "connecting to:" host "port:" port "db-name:" db-name
		            "username:" username "password:" password))

		(connect-db "server")
;; connecting to: server port: 12345 db-name: my-db username: db-user password: secret

		(connect-db "server" :username "user2" :password "Passowrd1")
;; connecting to: server port: 12345 db-name: my-db username: user2 password: Passowrd1

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Maps destructuring with custom key names:

		(defn distance [{x1 :x y1 :y} {x2 :x y2 :y}]
		  (let [square (fn [n] (* n n))]
		    (Math/sqrt
		     (+ (square (- x1 x2))
		        (square (- y1 y2))))))

		(distance {:x 3, :y 2} {:x 9, :y 7})
;; => 7.810249675906654

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring maps as key-value pairs:

;; - "map" takes the key-value pais one at a time:

  (map last {:x 1 :y 2 :z 3})
;; => (1 2 3)

;; - Using destructuring to build the sequence out of a map:

		(def contact
		  {:firstname "John"
		   :lastname  "Smith"
		   :age       25
		   :phone     "+44.123.456.789"
		   :emails    "jsmith@company.com"})

		(map (fn [[k v]] (str k " -> " v)) contact)
;; (":age -> 25"
;;  ":lastname -> Smith"
;;  ":phone -> +44.123.456.789"
;;  ":firstname -> John"
;;  ":emails -> jsmith@company.com")

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Nested destructuring

;; - Destructuring a nested vector or list:

		(def inventor-of-the-day
		  ["John" "McCarthy"
		   "1927-09-04"
		   "LISP"
		   ["Turing Award (1971)"
		    "Computer Pioneer Award (1985)"
		    "Kyoto Prize (1988)"
		    "National Medal of Science (1990)"
		    "Benjamin Franklin Medal (2003)"]])

;; (let [[firstname lastname _ _ [first-award & other-awards]] inventor-of-the-day]
		  (str firstname ", " lastname "'s first notable award was: " first-award)
;;  => "John, McCarthy's first notable award was: Turing Award (1971)"

;; - Destructuring nested maps:

		(def contact
		  {:firstname "John"
		   :lastname  "Smith"
		   :age       25
		   :contacts {:phone "+44.123.456.789"
		             :emails {:work "jsmith@company.com"
		                      :personal "jsmith@some-email.com"}}})

;; Just the top level
		(let [{lastname :lastname} contact]
		  (println lastname ))
;;  Smith

;; One nested level
		(let [{lastname :lastname
		       {phone :phone} :contacts} contact]
		  (println lastname phone))
;;  Smith +44.123.456.789

		(let [{:keys [firstname lastname]
		       {:keys [phone] } :contacts} contact]
		  (println firstname lastname phone ))
;;  John Smith +44.123.456.789

;; Two nested levels
		(let [{:keys [firstname lastname]
		       {:keys [phone]
		        {:keys [work personal]} :emails } :contacts} contact]
		  (println firstname lastname phone work personal))
;;  John Smith +44.123.456.789 jsmith@company.com jsmith@some-email.com

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring vectors by keys

;; - This method might be useful when you have to extract only few keys in high indices:

		(let [{one 1 two 2} [0 1 2]]
		  (println one two))
;;  => 1 2

		(let [{v1 100 v2 200} (apply vector (range 500))]
		  (println v1 v2))
;;  => 100 200

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Set’s destructuring

;; - This can be useful to test whether an element is part of a set:

		(let [{:strs [blue white black]} #{"blue" "white" "red" "green"}]
		  (println blue white black))
;;  => blue white nil

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring namespaced keys

		;; namespaced keys
		(def contact
		  {:firstname          "John"
		   :lastname           "Smith"
		   :age                25
		   :corporate/id       "LDF123"
		   :corporate/position "CEO"})

;; Notice how the namespaced `:corporate/position` is extracted
;; the symbol which is bound to the value has no namespace
		(let [{:keys [lastname corporate/position]} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; Like for normal keys, the vector of symbols can be
;; replaced with a vector of keywords
		(let [{:keys [:lastname :corporate/position]} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; Clojure 1.9 and subsequent releases
;; a default value might be provided
		(let [{:keys [lastname corporate/position]
		       :or {position "Employee"}} contact]
		  (println lastname "-" position))

;; Clojure 1.8 or previous (doesn't work on CLJ 1.9+)
;; a default value might be provided
		(let [{:keys [lastname corporate/position]
		       :or {corporate/position "Employee"}} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; - The double-colon :: is a shortcut to represent current namespace:

		(def contact
		  {:firstname "John"
		   :lastname  "Smith"
		   :age       25
		   ::id       "LDF123"
		   ::position "CEO"})

;; Clojure 1.9
		(let [{:keys [lastname ::position]
		       :or {position "Employee"}} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; Clojure 1.8 or previous
		(let [{:keys [lastname ::position]
		       :or {::position "Employee"}} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; - Applying the same rule to symbols:

		(def contact
		  {'firstname          "John"
		   'lastname           "Smith"
		   'age                25
		   'corporate/id       "LDF123"
		   'corporate/position "CEO"})

		(let [{:syms [lastname corporate/position]} contact]
		  (println lastname "-" position))
;;  => Smith - CEO

;; - Combining different types of destructuring 

		(def contact
		  {:firstname          "John"
		   :lastname           "Smith"
		   :age                25
		   :corporate/id       "LDF123"
		   :corporate/position "CEO"})

		(defn contact-line
;;  map destructuring
		  [{:keys [firstname lastname corporate/position] :as contact}]
;;  seq destructuring
		  (let [initial firstname]
		    (str "Mr " initial ". " lastname ", " position)))

		(contact-line contact)
;; => "Mr John. Smith, CEO"

;; - Another way of using destructuring with namespaced keys:

		(def contact
		  {:firstname          "John"
		   :lastname           "Smith"
		   :age                25
		   :corporate/id       "LDF123"
		   :corporate/position "CEO"
		   :corporate/phone    "+1234567890"
		   :personal/mobile    "0987654321"})

		(let [{:keys [lastname]
		       :corporate/keys [phone]
		       :personal/keys [mobile]} contact]
		  (println "Contact Mr." lastname "work:" phone " mobile:" mobile))
;;  => Contact Mr. Smith work: +1234567890  mobile: 0987654321

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Destructuring “gotchas”

;; - Notice how username is defined in the :keys part of destructuring while in the default values map (:or) is used user-name.
;; - The compiler won’t complain and the default value won’t be bound.

;; BAD DEFAULTS
		(defn connect-db [host ; mandatory parameter
		                  & {:keys [port db-name username password]
		                     :or   {port     12345
		                            db-name  "my-db"
		                            user-name "db-user"
		                            password "secret"}}]
		  (println "connecting to:" host "port:" port "db-name:" db-name
		           "username:" username "password:" password))

		(connect-db "server")

;; connecting to: server port: 12345 db-name: my-db username: nil password: secret
;;                notice the username is `nil` ---------------^

- Multi-matching values with namespaced keys:

		(def value {:id "id"
		            :fistname "John"
		            :lastname "Smith"
		            :customer/id "customer/id"})

;; nothing strange here
		(let [{:keys [:id]} value] (println id))
;; id

;; nothing strange here
		(let [{:keys [:customer/id]} value] (println id))
;; customer/id

;; KEY MIXUP - BAD
;; in the next two examples we attempt to destructure both keys
;; at the same time with two different namespaces.
;; NOTICE the one which appear later in the structure wins.
		(let [{:keys [:id :customer/id]} value] (println id))
;; customer/id

		(let [{:keys [:customer/id :id]} value] (println id))
;; id

;; -----------------------------------------------------------------------------------------------------------------------------------

;; Clojure destructuring cheatsheet

;; All the following destructuring forms can be used in any of the
;; Clojure's `let` derived bindings such as function's parameters,
;; `let`, `loop`, `binding`, `for`, `doseq`, etc.

;; list, vectors and sequences
		[zero _ _ three & four-and-more :as numbers] (range)
;; zero = 0, three = 3, four-and-more = (4 5 6 7 ...),
;; numbers = (0 1 2 3 4 5 6 7 ...)

;; maps and sets
		{:keys [firstname lastname] :as person} {:firstname "John"  :lastname "Smith"}
		{:keys [:firstname :lastname] :as person} {:firstname "John"  :lastname "Smith"}
		{:strs [firstname lastname] :as person} {"firstname" "John" "lastname" "Smith"}
		{:syms [firstname lastname] :as person} {'firstname "John"  'lastname "Smith"}
;; firstname = John, lastname = Smith, person = {:firstname "John" :lastname "Smith"}

;; maps destructuring with different local vars names
		{name :firstname family-name :lastname :as person} {:firstname "John"  :lastname "Smith"}
;; name = John, family-name = Smith, person = {:firstname "John" :lastname "Smith"}

;; default values
		{:keys [firstname lastname] :as person
		 :or {firstname "Jane"  :lastname "Bloggs"}} {:firstname "John"}
;; firstname = John, lastname = Bloggs, person = {:firstname "John"}

;; nested destructuring
		[[x1 y1] [x2 y2] [_ _ z]]  [[2 3] [5 6] [9 8 7]]
;; x1 = 2, y1 = 3, x2 = 5, y2 = 6, z = 7

		{:keys [firstname lastname]
		    {:keys [phone]} :contact} {:firstname "John" :lastname "Smith" :contact {:phone "0987654321"}}
;;    firstname = John, lastname = Smith, phone = 0987654321

;; namespaced keys in maps and sets
		{:keys [contact/firstname contact/lastname] :as person}     {:contact/firstname "John" :contact/lastname "Smith"}
		{:keys [:contact/firstname :contact/lastname] :as person}   {:contact/firstname "John" :contact/lastname "Smith"}
		{:keys [::firstname ::lastname] :as person}                 {::firstname "John"        ::lastname "Smith"}
		{:syms [contact/firstname contact/lastname] :as person}     {'contact/firstname "John"     'contact/lastname "Smith"}
;; firstname = John, lastname = Smith, person = {:firstname "John" :lastname "Smith"}