(in-ns 'con-world.core)

(defmulti apply-event (fn [_ _ [type & _]] type))

(defn apply-events [{:keys [events] :as screen} entities]
  (reduce (fn [entities evt]
            (apply-event screen entities evt))
          entities
          events))

(defmethod apply-event :player-moved [_ entities [_ direction]]
  (cell-move-sound)
  (let [cell (find-player entities)]
    (replace {cell (move-cell cell direction)}
             entities)))

