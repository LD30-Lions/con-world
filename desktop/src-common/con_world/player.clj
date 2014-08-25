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

(defn set-player-initial-settings
  [player-entity]
  (-> player-entity
      (move-player 0 0)
      (phy/set-velocity 0 0)))

(defn create-player-entity [screen]
  (-> (create-cell-entity! screen)
      (set-player-initial-settings)))

(def direction->velocity
  {:up    (vector-2 0 u/cell-y-velocity)
   :down  (vector-2 0 (- u/cell-y-velocity))
   :left  (vector-2 (- u/cell-x-velocity) 0)
   :right (vector-2 u/cell-x-velocity 0)})

(defn move-cell [cell direction]
  (phy/add-velocity cell (direction direction->velocity)))

(defn player-dead? [player-entity]
  (-> (cell/find-cell player-entity)
      :life
      (<= 0)))

(defn player-win? [{p-width :width} {e-width :width}]
  (> p-width e-width))