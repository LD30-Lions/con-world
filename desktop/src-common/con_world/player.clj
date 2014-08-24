(in-ns 'con-world.core)

(defn create-cell-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 5))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn create-cell-entity!
  [screen]
  (let [sheet (texture "entities/cell1.png")
        tiles (texture! sheet :split 40 40)
        cell-images (for [col [0 1]]
                      (texture (aget tiles 0 col)))
        stand (first cell-images)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))]
    (assoc stand
      :stand stand
      :walk (animation 0.2 cell-images :set-play-mode (play-mode :loop-pingpong))
      :body (create-cell-body! screen (/ width 2))
      :width width :height height
      :cell? true
      :life 1
      :level 1)))

(defn move-player [player-entity x y]
  (doto player-entity (body-position! x y 0)))

(defn set-player-velocity [player-entity x-velocity y-velocity]
  (doto player-entity (body! :set-linear-velocity x-velocity y-velocity)))

(defn set-player-initial-settings
  [player-entity]
  (-> player-entity
    (move-player 0 0)
    (set-player-velocity 0 0)))

(defn create-player-entity [ screen]
  (-> (create-cell-entity! screen)
      (set-player-initial-settings)))