;; WORKING WITH THE JVM

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - A running JVM executes bytecode by translating it on the fly into machine code that its host will understand, a process called
;; just-in-time compilation.

;; - For a program to run on the JVM, it must get compiled to Java bytecode.

;; - How a program is translated into machine code by the JVM:
;;    1. The Java compiler reads source code.
;;    2. The compiler outputs bytecode, often to a JAR file.
;;    3. JVM executes the bytecode.
;;    4. The JVM sends machine instructions to the CPU.

;; - When someone says that Clojure runs on the JVM, one of the things they mean is that Clojure programs get compiled to Java bytecode
;; and JVM processes execute them.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Packages and Imports in Java

;; - Packages organize code and require a matching directory structure.

;; - Importing classes allows you to refer to them without having to prepend the entire class’s package name.

;; - The classpath is the list of filesystem paths that the JVM searches to find a file that defines a class. 

;; - javac and Java find packages using the classpath.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - JAR Files: allow you to bundle all your .class files into one single file.

;; - Running Clojure's jar file, you can see that it is a JVM program just like any other.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Clojure App JARs

;; - You can make the Clojure compiler generate a class for a namespace by putting the (:gen-class) directive in the namespace declaration.

;; - The compiler produces the bytecode necessary for the JVM to treat the namespace as if it defines a Java class.

;; - You set the namespace of the entry point for your program in the program’s project.clj file, using the :main attribute.

;; - When Leiningen compiles this file, it will add a meta-inf/manifest.mf file that contains the entry point to the resulting JAR file.

;; - So, if you define a -main function in a namespace and include the (:gen-class) directive, and also set :main in your project.clj file,
;; your program will have everything it needs for Java to run it when it gets compiled as a JAR.

		lein uberjar
		java -jar target/uberjar/clojure-noob-0.1.0-SNAPSHOT-standalone.jar

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Java Interop Syntax

;; - The ability to use Java classes, objects, and methods is called Java interop.

;; - You can call methods on an object using (.methodName object). 

;; - Because all Clojure strings are implemented as Java strings, you can call Java methods on them: 

		(.toUpperCase "By Bluebeard's bananas!")
		; => "BY BLUEBEARD'S BANANAS!"

		(.indexOf "Let's synergize our bleeding edges" "y") 
		; => 7

;; - Notice that Clojure’s syntax allows you to pass arguments to Java methods (ex: "y").

;; - You can also call static methods on classes and access classes’ static fields:

		(java.lang.Math/abs -3) 
		; => 3

		java.lang.Math/PI 
		; => 3.141592653589793

;; - All of these examples (except java.lang.Math/PI) use macros that expand to use the dot special form.

		(macroexpand-1 '(.toUpperCase "By Bluebeard's bananas!"))
		; => (. "By Bluebeard's bananas!" toUpperCase)

		(macroexpand-1 '(.indexOf "Let's synergize our bleeding edges" "y"))
		; => (. "Let's synergize our bleeding edges" indexOf "y")

		(macroexpand-1 '(Math/abs -3))
		; => (. Math abs -3)

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Creating and Mutating Objects

;; - You can create a new object in two ways: (new ClassName optional-args) and (ClassName. optional-args):

		(new String)
		; => ""

		(String.)
		; => ""

		(String. "To Davey Jones's Locker with ye hardies")
		; => "To Davey Jones's Locker with ye hardies"

;; - To modify an object, you call methods on it.

;; - Let’s use java.util.Stack. This class represents a last-in, first-out (LIFO) stack of objects.

;; - Unlike Clojure data structure, Java stacks are mutable. You can add items to them and remove items, changing the object instead of
;; deriving a new value.

		(java.util.Stack.)
		; => []

		(let [stack (java.util.Stack.)] 
		  (.push stack "Latest episode of Game of Thrones, ho!")
		  stack)
		; => ["Latest episode of Game of Thrones, ho!"]

;; - Clojure prints the stack with square brackets, the same textual representation it uses for a vector, which could throw you because
;; it’s not a vector.

;; - You can use Clojure’s seq functions for reading a data structure, like first, on the stack:

		(let [stack (java.util.Stack.)]
		  (.push stack "Latest episode of Game of Thrones, ho!")
		  (first stack))
		; => "Latest episode of Game of Thrones, ho!"

;; - But you can’t use functions like conj and into to add elements to the stack.

;; - Clojure provides the doto macro, which allows you to execute multiple methods on the same object more succinctly:

		(doto (java.util.Stack.)
		  (.push "Latest episode of Game of Thrones, ho!")
		  (.push "Whoops, I meant 'Land, ho!'"))
		; => ["Latest episode of Game of Thrones, ho!" "Whoops, I meant 'Land, ho!'"]

;; - The doto macro returns the object rather than the return value of any of the method calls, and it’s easier to understand.

;; - If you expand it using macroexpand-1, you can see its structure is identical to the let expression you just saw in an 
;;earlier example:

		(macroexpand-1
		 '(doto (java.util.Stack.)
		    (.push "Latest episode of Game of Thrones, ho!")
		    (.push "Whoops, I meant 'Land, ho!'")))
		; => (clojure.core/let
		;      [G__2876 (java.util.Stack.)]
		;      (.push G__2876 "Latest episode of Game of Thrones, ho!")
		;      (.push G__2876 "Whoops, I meant 'Land, ho!'")
		;      G__2876)

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Importing

;; - Importing has the same effect as it does in Java: you can use classes without having to type out their entire package prefix:

		(import java.util.Stack)
		(Stack.)
		; => []

;; - You can also import multiple classes at once:
		
		;(import [package.name1 ClassName1 ClassName2]
		;        [package.name2 ClassName3 ClassName4])

		(import [java.util Date Stack]
		        [java.net Proxy URI])

		(Date.)
		; => #inst "2016-09-19T20:40:02.733-00:00"

;; - But usually, you’ll do all your importing in the ns macro, like this:

		(ns pirate.talk
		  (:import [java.util Date Stack]
		           [java.net Proxy URI]))

;; - To make life even easier, Clojure automatically imports the classes in java.lang, including java.lang.String and
;; java.lang.Math, which is why you were able to use String without a preceding package name.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The System Class

;; - You can use the system class to get environment variables and interact with the standard input, standard output, and error
;; output streams.

;; - The most useful methods and members are exit, getenv, and getProperty.

;; - System/exit terminates the current program, and you can pass it a status code as an argument.

;; - System/getenv will return all of your system’s environment variables as a map:

		(System/getenv)
		{"USER" "the-incredible-bulk"
		 "JAVA_ARCH" "x86_64"}

 ;; - The JVM has its own list of properties separate from the computer’s environment variables, and if you need to read them, you
 ;; can use System/getProperty:

		(System/getProperty "user.dir")
		; => "/Users/dabulk/projects/dabook"

		(System/getProperty "java.version")
		; => "1.7.0_17"

;; - The first call returned the directory that the JVM started from, and the second call returned the version of the JVM.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The Date Class

;; - java.util.Date

;; - Clojure allows you to represent dates as literals using a form like this:

  #inst "2016-09-19T20:40:02.733-00:00"

;; - You need to use the java.util.DateFormat class if you want to customize how you convert dates to strings or if you want to convert
;; strings to dates.

;; - If you’re doing tasks like comparing dates or trying to add minutes, hours, or other units of time to a date, you should use the
;; immensely useful clj-time library

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Files and Input/Output

;; - The clojure.java.io namespace provides many handy functions for simplifying IO.

;; - IO involves resources, be they files, sockets, buffers, or whatever.

;; - The java.io.File class is used to interact with a file’s properties:

		(let [file (java.io.File. "/")]
		   (println (.exists file))  
		   (println (.canWrite file))
		   (println (.getPath file))) 
		; => true
		; => false
		; => /

;; - You can use it to check whether a file exists, to get the file’s read/write/execute permissions, and to get its filesystem path.

;; - To read a file, you could use the java.io.BufferedReader class or perhaps java.io.FileReader. 

;; - Likewise, you can use the java.io.BufferedWriter or java.io.FileWriter class for writing.

;; - Reader and writer classes all have the same base set of methods for their interfaces; readers implement read, close, and more, 
;; while writers implement append, write, close, and flush.

;; - Clojure makes reading and writing easier for you because it includes functions that unify reading and writing across different
;; kinds of resources.

;; - spit writes to a resource, and slurp reads from one. Here’s an example of using them to write and read a file:

		(spit "/tmp/hercules-todo-list"
		"- kill dat lion brov
		- chop up what nasty multi-headed snake thing")

		(slurp "/tmp/hercules-todo-list")

		; => "- kill dat lion brov
		;      - chop up what nasty multi-headed snake thing"

;; - You can also use these functions with objects representing resources other than files. The next example uses a StringWriter,
;; which allows you to perform IO operations on a string:

		(let [s (java.io.StringWriter.)]
		  (spit s "- capture cerynian hind like for real")
		  (.toString s))
		; => "- capture cerynian hind like for real"

;; - You can also read from a StringReader using slurp:

		(let [s (java.io.StringReader. "- get erymanthian pig what with the tusks")]
		  (slurp s))
		; => "- get erymanthian pig what with the tusks"

;; - The with-open macro is another convenience: it implicitly closes a resource at the end of its body, ensuring that you don’t
;; accidentally tie up resources by forgetting to manually close the resource.

;; - You could use reader along with with-open and the line-seq function if you’re trying to read a file one line at a time.

;; - Here’s how you could print just the first item of the Hercules to-do list:

		(with-open [todo-list-rdr (clojure.java.io/reader "/tmp/hercules-todo-list")]
		  (println (first (line-seq todo-list-rdr))))
		; => - kill dat lion brov


