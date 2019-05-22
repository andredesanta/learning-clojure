;; CORE.ASYNC

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Clojure’s core.async library allows you to create multiple independent processes within a single program.

;; - At the heart of core.async is the process, a concurrently running unit of logic that responds to events.

;; - Processes interact with and respond to each other independently without some kind of central control mechanism pulling the strings.

;; - This differs from the view of concurrency where you’ve defined tasks that are either mere extensions of the main thread of control
;; or tasks that you have no interest in communicating with.

;; - If you want to get super philosophical, consider whether it’s possible to define every thing’s essence as the set of the events 
;; it recognizes and how it responds.

;; - Open the file project.clj and add core.async to the :dependencies vector so it reads as follows:

		[[org.clojure/clojure "1.9.0"]
		[org.clojure/core.async "0.1.346.0-17112a-alpha"]]

;; - Next, open src/playsync/core.clj and make it look like this:

		(ns playsync.core
		  (:require [clojure.core.async
		             :as a
		             :refer [>! <! >!! <!! go chan buffer close! thread
		                     alts! alts!! timeout]]))

;; - Create a process that simply prints the message it receives:

		(def echo-chan (chan))
		(go (println (<! echo-chan)))
		(>!! echo-chan "ketchup")
		; => true
		; => ketchup

;; - You used the chan function to create a channel named echo-chan. Channels can communicate messages (events).

;; - You can put messages on a channel and take messages off a channel.

;; - Processes wait for the completion of put and take — these are the events that processes respond to.

;; - You can think of processes as having two rules: 1) when trying to put a message on a channel or take a message off of it, wait 
;; and do nothing until the put or take succeeds, and 2) when the put or take succeeds, continue executing.

;; - You used go to create a new process. Everything within the go expression — called a go block — runs concurrently on a separate thread.

;; - Go blocks run your processes on a thread pool that contains a number of threads equal to two plus the number of cores on your machine.

;; - The process (println (<! echo-chan)) expresses “when I take a message from echo-chan, print it.”

;; - The process is shunted to another thread, freeing up the current thread and allowing you to continue interacting with the REPL.

;; - In the expression (<! echo-chan), <! is the take function.

;; - It listens to the channel you give it as an argument, and the process it belongs to waits until another process puts a message on
;; the channel. When <! retrieves a value, the value is returned and the println expression is executed.

;; - The expression (>!! echo-chan "ketchup") puts the string "ketchup" on echo-chan and returns true.

;; - When you put a message on a channel, the process blocks until another process takes the message.

;; - If you do the following, your REPL will block indefinitely:

  (>!! (chan) "mustard")

;; - You’ve created a new channel and put something on it, but there’s no process listening to that channel.

;; - Processes don’t just wait to receive messages; they also wait for the messages they put on a channel to be taken.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Buffering

;; - The previous exercise contained two processes: the one you created with go and the REPL process.

;; - You can create buffered channels:

		(def echo-buffer (chan 2))
		(>!! echo-buffer "ketchup")
		; => true
		(>!! echo-buffer "ketchup")
		; => true
		(>!! echo-buffer "ketchup")
		; This blocks because the channel buffer is full

;; - In this case, you’ve created a channel with buffer size 2. That means you can put two values on the channel without waiting, but
;; putting a third one on means the process will wait until another process takes a value from the channel.

;; - You can create buffers that won't ever cause >!! to block: sliding-buffer and dropping-buffer.

;; - sliding-buffer drops values in a first-in, first-out fashion and dropping-buffer discards values in a last-in, first-out fashion.

		(def sliding-chan (chan (sliding-buffer 1)))

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Blocking and Parking

;; - Both put and take have one-exclamation-point and two-exclamation-point varieties. When do you use which?

;;  	   | Inside go block |	Outside go block
;; put 	|   >! or >!!     |      >!!
;; take |	  <! or <!!     |      <!!

;; - You can create 1,000 go processes but use only a handful of threads:

		(def hi-chan (chan))
		(doseq [n (range 1000)]
		  (go (>! hi-chan (str "hi " n))))

;; - In this example, 1,000 processes are waiting for another process to take from hi-chan.

;; - There are two varieties of waiting: parking and blocking. 

;; - Blocking is the kind of waiting you’re familiar with: a thread stops execution until a task is complete.

;; - Parking frees up the thread so it can keep doing work.

;; - Parking allows the instructions from multiple processes to interleave on a single thread, similar to the way that using multiple
;; threads allows interleaving on a single core.

;; - The implementation of parking is only possible within go blocks, using >! and <!, or parking put and parking take.

;; - >!! and <!! are blocking put and blocking take.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Thread

;; - thread acts almost exactly like future: it creates a new thread and executes a process on that thread.

;; - Unlike future, instead of returning an object that you can dereference, thread returns a channel.

		(thread (println (<!! echo-chan)))
		(>!! echo-chan "mustard")
		; => true
		; => mustard

;; - When thread’s process stops, the process’s return value is put on the channel that thread returns:

		(let [t (thread "chili")]
		  (<!! t))
		; => "chili"

;; - In this case, the process doesn’t wait for any events; instead, it stops immediately. Its return value is "chili", which gets
;; put on the channel that’s bound to t. We take from t, returning "chili".

;; The reason you should use thread instead of a go block when you’re performing a long-running task is so you don’t clog your thread pool.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The Hot Dog Machine Process

		(defn hot-dog-machine-v2
		  [hot-dog-count]
		  (let [in (chan)
		        out (chan)]
		    (go (loop [hc hot-dog-count]
		          (if (> hc 0)
		            (let [input (<! in)]
		              (if (= 3 input)
		                (do (>! out "hot dog")
		                    (recur (dec hc)))
		                (do (>! out "wilted lettuce")
		                    (recur hc))))
		            (do (close! in)
		                (close! out)))))
		    [in out]))

;; - Once a process has taken the output, the hot dog machine process loops back with an updated hot dog count and is ready to receive
;; money again.

;; - When the machine process runs out of hot dogs, the process closes the channels. When you close a channel, you can no longer
;; perform puts on it, and once you’ve taken all values off a closed channel, any subsequent takes will return nil.

		(let [[in out] (hot-dog-machine-v2 2)]
		  (>!! in "pocket lint")
		  (println (<!! out))

		  (>!! in 3)
		  (println (<!! out))

		  (>!! in 3)
		  (println (<!! out))

		  (>!! in 3)
		  (<!! out))
		; => wilted lettuce
		; => hotdog
		; => hotdog
		; => nil

;; - hot-dog-machine-v2 does a put and a take within the same go block. This isn’t that unusual, and it’s one way you can create a
;; pipeline of processes: just make the in channel of one process the out channel of another.

;; - The following example does just that, passing a string through a series of processes that perform transformations until the
;; string finally gets printed by the last process:

		(let [c1 (chan)
		      c2 (chan)
		      c3 (chan)]
		  (go (>! c2 (clojure.string/upper-case (<! c1))))
		  (go (>! c3 (clojure.string/reverse (<! c2))))
		  (go (println (<! c3)))
		  (>!! c1 "redrum"))
		; => MURDER

;; - The hot dog machine doesn’t accept more money until you’ve dealt with whatever it’s dispensed. 

;; - This allows you to model state-machine-like behavior, where the completion of channel operations triggers state transitions.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - alts!! lets you use the result of the first successful channel operation among a collection of operations.

		(defn upload
		  [headshot c]
		  (go (Thread/sleep (rand 100))
		      (>! c headshot)))

		  (let [c1 (chan)
		      c2 (chan)
		      c3 (chan)]
		  (upload "serious.jpg" c1)
		  (upload "fun.jpg" c2)
		  (upload "sassy.jpg" c3)
		    (let [[headshot channel] (alts!! [c1 c2 c3])]
		    (println "Sending headshot notification for" headshot)))
		; => Sending headshot notification for sassy.jpg

;; - The upload function takes a headshot and a channel, and creates a new process that sleeps for a random amount of time (to simulate
;; the upload) and then puts the headshot on the channel.

;; - The alts!! function takes a vector of channels as its argument.

;; - All alts!! does is take a value from the first channel to have a value; it doesn’t touch the other channels.

;; - One cool aspect of alts!! is that you can give it a timeout channel, which waits the specified number of milliseconds and then closes.

		(let [c1 (chan)]
		  (upload "serious.jpg" c1)
		  (let [[headshot channel] (alts!! [c1 (timeout 20)])]
		    (if headshot
		      (println "Sending headshot notification for" headshot)
		      (println "Timed out!"))))
		; => Timed out!

;; - In this case, we set the timeout to 20 milliseconds. Because the upload didn’t finish in that time frame, we got a timeout message.

;; - You can also use alts!! to specify put operations. To do that, place a vector inside the vector you pass to alts!!:

		(let [c1 (chan)
		      c2 (chan)]
		  (go (<! c2))
		    (let [[value channel] (alts!! [c1 [c2 "put!"]])]
		    (println value)
		    (= channel c2)))
		; => true
		; => true

;; - Here you’re creating two channels and then creating a process that’s waiting to perform a take on c2.

;; - The vector that you supply to alts!! tells it, “Try to do a take on c1 and try to put "put!" on c2. If the take on c1 finishes first,
;; return its value and channel. If the put on c2 finishes first, return true if the put was successful and false otherwise.”

;; - Finally, the result of value (which is true, because the c2 channel was open) prints and shows that the channel returned was indeed c2.

;; - Like <!! and >!!, alts!! has a parking alternative, alts!, which you can use inside go blocks.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Queues

;; - Let’s say you want to get a bunch of random quotes from a website and write them to a single file.

;; - You want to make sure that only one quote is written to a file at a time so the text doesn’t get interleaved, so you put your
;; quotes on a queue.

		(defn append-to-file
		  "Write a string to the end of a file"
		  [filename s]
		  (spit filename s :append true))

		(defn format-quote
		  "Delineate the beginning and end of a quote because it's convenient"
		  [quote]
		  (str "=== BEGIN QUOTE ===\n" quote "=== END QUOTE ===\n\n"))

		(defn random-quote
		  "Retrieve a random quote and format it"
		  []
		  (format-quote (slurp "http://www.braveclojure.com/random-quote")))

		(defn snag-quotes
		  [filename num-quotes]
		  (let [c (chan)]
		    (go (while true (append-to-file filename (<! c))))
		    (dotimes [n num-quotes] (go (>! c (random-quote))))))

;; - First, snag-quotes creates a channel that’s shared between the quote-producing processes and the quote-consuming process.

;; - Then it creates a process that uses while true to create an infinite loop.

;; - On every iteration of the loop, it waits for a quote to arrive on c and then appends it to a file.

;; - Finally, snag-quotes creates a num-quotes number of processes that fetch a quote and then put it on c.

;; - If you evaluate (snag-quotes "quotes" 2) and check the quotes file in the directory where you started your REPL,
;; it should have two quotes:

		=== BEGIN QUOTE ===
		Nobody's gonna believe that computers are intelligent until they start
		coming in late and lying about it.
		=== END QUOTE ===

		=== BEGIN QUOTE ===
		Give your child mental blocks for Christmas.
		=== END QUOTE ===

;; - Here, each quote-retrieving task is handled in the order that it finishes. 

;; - In both cases, you ensure that only one quote at a time is written to a file.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; Escape Callback Hell with Process Pipelines

;; - In a language like JavaScript, callbacks are a way to define code that executes asynchronously once other code finishes.

;; - By creating a process pipeline, each unit of logic lives in its own isolated process, and all communication between units of logic
;; occurs through explicitly defined input and output channels.

;; - It can avoid creating dependencies among layers of callbacks that aren’t immediately obvious and end up sharing state.

;; - In the following example, we create three infinitely looping processes connected through channels, passing the out channel of one
;; process as the in channel of the next process in the pipeline:

		(defn upper-caser
		  [in]
		  (let [out (chan)]
		    (go (while true (>! out (clojure.string/upper-case (<! in)))))
		    out))

		(defn reverser
		  [in]
		  (let [out (chan)]
		    (go (while true (>! out (clojure.string/reverse (<! in)))))
		    out))

		(defn printer
		  [in]
		  (go (while true (println (<! in)))))

		(def in-chan (chan))
		(def upper-caser-out (upper-caser in-chan))
		(def reverser-out (reverser upper-caser-out))
		(printer reverser-out)

		(>!! in-chan "redrum")
		; => MURDER

		(>!! in-chan "repaid")
		; => DIAPER

;; - By handling events using processes like this, it’s easier to reason about the individual steps of the overall
;; data transformation system.




