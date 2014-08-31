(in-ns 'con-world.core)


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




(defn on-show [screen _]

  (let [background (u/memo-texture "main-screen-background-1.png")
        background (assoc background
                     :width (u/pixels->world (texture! background :get-region-width))
                     :height (u/pixels->world (texture! background :get-region-height))
                     :background? true)
        screen (-> (init-graphic-settings screen)
                   (update!
                     :world (box-2d 0 0)
                     :music (memo-sound "sound/fond.mp3")
                     :bg-width (:width background)
                     :bg-height (:height background)
                     :last-spawn 0))]

    (add-timer! screen :ambiant-sound 3 3)
    (add-timer! screen :spawn-enemy 3 5)
    (sound! (:music screen) :loop)

    [background
     (create-uber-wall-entity screen)
     (create-wall-entity screen)
     (create-plante-zone! screen)
     (create-player-entity screen)]))



(defn on-render [{:keys [debug-physics?] :as screen} entities]

  (clear!)

  (if (player-dead? entities)

    (game-over screen)
    (do
      (add-event [:step])
      (let [result-entities (->> entities
                                 (apply-events screen)
                                 change-player-level
                                 (animate-player screen)
                                 (animate-plante screen)
                                 (animate-enemies screen)
                                 (map set-enemy-in-zone)
                                 (map move-enemy)
                                 (render! screen))]

        (when debug-physics?
          (draw-physics-bodies screen))
        result-entities))))

(defn on-key-down [{:keys [key debug-physics?] :as screen} entities]
  (when-let [direction (key->direction key)]
    (add-event [:player-moved direction]))
  (when (= (key-code :F12) key)
    (update! screen :debug-physics? (not debug-physics?)))
  entities)

(defn on-resize [screen _]
  (size! screen (:bg-width screen) (:bg-height screen)))

(defn on-end-contact [screen entities]
  (let [{:keys [player enemy]} (coliding-entities screen entities)]
    (when (and player enemy)
      (if (player-win? player enemy)
        (add-event [:player-ate-enemy (:id enemy)])
        (add-event [:player-hurt-by-enemy (:id enemy)])))
    entities))

(defn on-pre-solve [{:keys [^Contact contact] :as screen} entities]
  (let [{:keys [player enemy enemy-1 enemy-2 wall plante-zone]} (coliding-entities screen entities)]
    (when (or (and enemy wall (not (:in-zone? enemy)))
              (and enemy-1 enemy-2 (or (not (:in-zone? enemy-1)) (not (:in-zone? enemy-2)))))
      (.setEnabled contact false))
    (when (and plante-zone player)
      (.setEnabled contact false))
    entities))

(defn on-timer [screen entities]
  (when (= :ambiant-sound (:id screen))
    (when (even? (rand-int 2))
      (ambiant-sound (find-player entities))))
  (when (= :spawn-enemy (:id screen))
    (add-event [:enemy-spawned]))
  entities)

(defscreen main-screen
           :on-show on-show
           :on-render on-render
           :on-key-down on-key-down
           :on-resize on-resize
           :on-end-contact on-end-contact
           :on-pre-solve on-pre-solve
           :on-timer on-timer)