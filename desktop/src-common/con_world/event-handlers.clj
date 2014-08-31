(in-ns 'con-world.core)

(defmethod apply-event :world-stepped [screen entities _]
  (step! screen entities))

(defmethod apply-event :enemy-spawned [screen entities _]
  (let [enemy (spawn-enemy screen entities)]
    (conj entities enemy)))

(defmethod apply-event :enemy-entered-zone [_ entities [_ id]]
  (let [enemy (find-enemy entities id)
        enemy-updated (-> (assoc enemy :in-zone? true)
                          (set-enemy-restitution enemy-restitution)
                          (doto (body! :set-linear-damping enemy-damping)))]
    (replace {enemy enemy-updated} entities)))

(defmethod apply-event :enemy-moved [_ entities [_ id vec2-velocity]]
  (let [enemy (find-enemy entities id)]
    (replace {enemy (change-enemy-velocity enemy vec2-velocity)}
             entities)))

(defmethod apply-event :player-moved [_ entities [_ direction]]
  (cell-move-sound)
  (let [cell (find-player entities)]
    (replace {cell (move-cell cell direction)}
             entities)))

(defn do-player-win [entities {:keys [level life] :as player} enemy]
  (let [new-life (inc life)
        new-level (or (calculate-level player) level)
        new-level? (not= new-level level)]
    (kill-enemy-sound player)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-level :level new-level)
    (run! score-screen :update-score :score new-life)
    (->> entities
         (remove #(= % enemy))
         (replace {player (->> (assoc player :life new-life :level new-level)
                               (update-cell-sprite!))}))))

(defmethod apply-event :player-ate-enemy [_ entities [_ enemy-id]]
  (do-player-win entities (find-player entities) (find-enemy entities enemy-id)))

(defn do-player-lose [entities {:keys [life level] :as player}]
  (let [new-life (- life 5)
        new-level (calculate-level player)
        new-level? (not= new-level level)]
    (touched-by-enemy-sound)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-score :score new-life)
    (run! score-screen :update-level :level new-level)
    (replace {player (->> (assoc player :life new-life :level new-level)
                          (update-cell-sprite!))} entities)))

(defmethod apply-event :player-hurt-by-enemy [_ entities _]
  (do-player-lose entities (find-player entities)))