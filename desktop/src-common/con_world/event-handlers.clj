(in-ns 'con-world.core)

(defn clear-events [screen]
  (when (pos? (count @events)) (println "clearing " @events))
  (reset! events []))

(defn add-event [screen event]
  (println "adding" event (nil? screen))
  (swap! events conj event))

(defmulti apply-event (fn [_ [type & _]] type))

(defn apply-events [entities]
  (when (pos? (count @events)) (println "events" @events))
  (reduce (fn [entities evt]
            (let [res (apply-event entities evt)]
              (println "event" evt)
              res))
          entities
          @events))

(defmethod apply-event :player-moved [entities [_ direction]]
  (cell-move-sound)
  (let [cell (find-player entities)]
    (replace {cell (move-cell cell direction)}
             entities)))

(defn do-player-win [entities {:keys [level life] :as player} enemy]
  (println "do-player win " (count entities))
  (let [new-life (inc life)
        new-level (or (calculate-level player) level)
        new-level? (not= new-level level)]
    (println "new-life" new-life "new-level" new-level)
    (kill-enemy-sound player)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-level :level new-level)
    (run! score-screen :update-score :score new-life)
    (->> entities
         (remove #(= % enemy))
         (replace {player (->> (assoc player :life new-life :level new-level)
                               (update-cell-sprite!))}))))

(defmethod apply-event :player-ate-enemy [entities [_ enemy-id]]
  (println "player ate enemy")
  (do-player-win entities (find-player entities) (find-enemy entities enemy-id)))

(defn do-player-lose [entities {:keys [life level] :as player}]
  (let [new-life (- life 5)
        new-level (calculate-level player)
        new-level? (not= new-level level)]
    (println "new-life" new-life "new-level" new-level)
    (touched-by-enemy-sound)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-score :score new-life)
    (run! score-screen :update-level :level new-level)
    (replace {player (->> (assoc player :life new-life :level new-level)
                          (update-cell-sprite!))} entities)))

(defmethod apply-event :player-hurt-by-enemy [entities _]
  (println "player hurt by enemy")
  (do-player-lose entities (find-player entities)))