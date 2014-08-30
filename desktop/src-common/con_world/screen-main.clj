(in-ns 'con-world.core)

(defn on-key-move-cell [entities direction]
  (cell-move-sound)
  (let [cell (find-player entities)]
    (replace {cell (move-cell cell direction)}
             entities)))

(defn game-over [screen]
  (do (set-screen! con-world game-over-screen)
      (sound! (:music screen) :stop)))

(def key->direction
  {(key-code :dpad-up)    :up,
   (key-code :dpad-down)  :down,
   (key-code :dpad-left)  :left,
   (key-code :dpad-right) :right})

(defn coliding-entities [screen entities]
  (let [entities (filter #(contains? % :body) entities)
        enemy-1 (find-enemy [(first-entity screen entities)])
        enemy-2 (find-enemy [(second-entity screen entities)])
        coliding-entities [(first-entity screen entities) (second-entity screen entities)]]
    {:enemy-1     enemy-1
     :enemy-2     enemy-2
     :enemy       (or enemy-1 enemy-2)
     :player      (find-player coliding-entities)
     :plante-zone (find-plante-zone coliding-entities)
     :wall        (find-wall coliding-entities)}))

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

(defn do-player-win [entities {:keys [level life] :as player} enemy]
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
(defmulti apply-event (fn [_ _ [type & _]] type))

(defmethod apply-event :player-moved [screen entities [_ direction]]
  (on-key-move-cell entities direction))

(defn apply-events [{:keys [events] :as screen} entities]
  (reduce (fn [entities evt]
            (apply-event screen entities evt))
          entities
          events))

(defscreen main-screen

           :on-show
           (fn [screen _]

             (let [background (u/memo-texture "main-screen-background-1.png")
                   background (assoc background
                                :width (u/pixels->world (texture! background :get-region-width))
                                :height (u/pixels->world (texture! background :get-region-height)))
                   screen (-> (init-graphic-settings screen)
                              (update!
                                :world (box-2d 0 0)
                                :music (memo-sound "sound/fond.mp3")
                                :bg-width (:width background)
                                :bg-height (:height background)
                                :last-spawn 0))]

               (add-timer! screen :ambiant-sound 3 3)
               (sound! (:music screen) :loop)

               [background
                (create-uber-wall-entity screen)
                (create-wall-entity screen)
                (create-plante-zone! screen)
                (create-player-entity screen)]))

           :on-render
           (fn [{:keys [debug-physics?] :as screen} entities]

             (clear!)

             (let [[screen entities] (may-spawn-enemy screen entities)]

               (when debug-physics?
                 (draw-physics-bodies screen))

               (if (player-dead? entities)

                 (game-over screen)

                 (let [result-entities (->> entities
                                            (apply-events screen)
                                            (step! screen)
                                            change-player-level
                                            (animate-player screen)
                                            (animate-plante screen)
                                            (animate-enemies screen)
                                            (map set-enemy-in-zone)
                                            (map move-enemy)
                                            (render! screen))]
                   (update! screen :events [])
                   result-entities))))

           :on-key-down
           (fn [{:keys [key debug-physics?] :as screen} entities]
             (when-let [direction (key->direction key)]
               (update! screen :events (conj (:events screen) [:player-moved direction])))
             (when (= 255 key)
                            (update! screen :debug-physics? (not debug-physics?)))
             entities)

           :on-resize
           (fn [screen _]
             (size! screen (:bg-width screen) (:bg-height screen)))

           :on-end-contact
           (fn [screen entities]
             (let [{:keys [player enemy]} (coliding-entities screen entities)]
               (if (and player enemy)
                 (if (player-win? player enemy)
                   (do-player-win entities player enemy)
                   (do-player-lose entities player))
                 entities)))

           :on-pre-solve
           (fn [{:keys [^Contact contact] :as screen} entities]
             (let [{:keys [player enemy enemy-1 enemy-2 wall plante-zone]} (coliding-entities screen entities)]
               (when (or (and enemy wall (not (:in-zone? enemy)))
                         (and enemy-1 enemy-2 (or (not (:in-zone? enemy-1)) (not (:in-zone? enemy-2)))))
                 (.setEnabled contact false))
               (when (and plante-zone player)
                 (.setEnabled contact false))
               entities))

           :on-timer
           (fn [screen entities]
             (when (= :ambiant-sound (:id screen))
               (when (even? (rand-int 2))
                 (ambiant-sound (find-player entities)))
               entities)))