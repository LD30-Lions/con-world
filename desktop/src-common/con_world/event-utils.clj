(in-ns 'con-world.core)

(def events (atom []))

(defn add-event [event]
  (swap! events conj event))

(defmulti apply-event (fn [_ _ [type & _]] type))

(defn apply-events [screen entities]
  (loop [evts @events entities entities]
    (if (not-empty evts)
      (recur
        (swap! events rest)
        (vec (apply-event screen (vec entities) (first evts))))
      entities)))