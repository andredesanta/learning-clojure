;; BUILDING AND DEVELOPING WITH LEININGEN

;; Source: Clojure for the Brave and True - Daniel Higginbotham

;; ------------------------------------------------------------------------------------------------------------------------------------

;; - Writing software in any language involves generating artifacts, which are executable files or library packages that are meant to
;; be deployed or shared.

;; - It also involves managing dependent artifacts, also called dependencies, by ensuring that they’re loaded into the project you’re
;; building.

;; - The most popular tool among Clojurists for managing artifacts is Leiningen.

;; ------------------------------------------------------------------------------------------------------------------------------------

;; The Artifact Ecosystem

;; - Java land already has an entire artifact ecosystem for handling JAR files, and Clojure uses it.

;; - Artifact ecosystem isn’t an official programming term; I use it to refer to the suite of tools, resources, and conventions used to
;; identify and distribute artifacts.

;; - Java’s ecosystem grew up around the Maven build tool, and because Clojure uses this ecosystem, you’ll often see references to Maven.

;; - Maven specifies a pattern for identifying artifacts that Clojure projects adhere to, and it also specifies how to host these 
;; artifacts in Maven repositories.


