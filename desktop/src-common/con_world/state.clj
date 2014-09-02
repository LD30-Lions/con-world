(ns con-world.state)

(def events (atom []))

; ids generator
(def id (atom 0))
(def inc-id #(swap! id inc))
