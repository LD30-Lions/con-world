(in-ns 'con-world.core)

(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))

(defn create-cell-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 5))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(def player-index
  {1 [40 2]
   2 [60 2]
   3 [75 2]
   4 [90 2]
   5 [120 2]})

(defn create-cell-entity!
  [screen]
  (let [{cell-images :sprites stand :first} (image->sprite "entities/cell1.png" 40 40 2)
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

(defn update-cell-sprite!
  [{:keys [level] :as player}]
  (let [[size nb] (player-index level)
        {cell-images :sprites stand :first} (image->sprite (str "entities/cell" level ".png") size size nb)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))]
    (assoc player
      :stand stand
      :walk (animation 0.2 cell-images :set-play-mode (play-mode :loop-pingpong))
      :width width :height height)))

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
  (-> (find-cell player-entity)
      :life
      (<= 0)))

(defn player-win? [{p-width :width} {e-width :width}]
  (> p-width e-width))

(defn change-cell-level [entities]
  (let [cell (find-cell entities)]
    (replace {cell (merge cell
                          (u/memo-texture (str "cell" (:level cell) ".png")))}
             entities)))

(defn animate-cell [screen entities]
  (let [player (find-cell entities)
        velocity (body! player :get-linear-velocity)
        not-moving (vector-2! velocity :is-zero 1)]
    (if not-moving
      (replace {player (merge player (:stand player))} entities)
      (replace {player (merge player (animation->texture screen (:walk player)))} entities))))

(defn calculate-level [{:keys [life]}]
  (let [mapping [[5 1], [10 2], [15 3], [20 4], [25 5]]]
    (some (fn [[map-life map-level]]
            (when (< life map-life) map-level))
          mapping)))
