(in-ns 'con-world.core)

(defn random-vec2
  [x-max y-max]
  {:post [(and (< (.x %) x-max) (> (.x %) (- x-max))
               (< (.y %) y-max) (> (.y %) (- y-max)))]}
  (let [added-velocity (map #(-> % rand-int u/rand-sign) [u/enemy-x-velocity u/enemy-y-velocity])]
    (apply vector-2* added-velocity)))

(defn change-enemy-velocity [enemy-entity]
  (let [added-velocity (random-vec2 u/enemy-x-velocity u/enemy-y-velocity)]
    (phy/add-velocity enemy-entity added-velocity)))

(defn moving-fast? [entity]
  (let [^Vector2 vec2-velocity (body! entity :get-linear-velocity)
        x-velocity (.x vec2-velocity)
        y-velocity (.y vec2-velocity)]
    (or (> x-velocity u/moving-slow) (> y-velocity u/moving-slow))))

(defn move-enemy [{:keys [enemy? in-zone?] :as enemy-entity}]
  (if (and enemy? in-zone? (not (moving-fast? enemy-entity)))
    (change-enemy-velocity enemy-entity)
    enemy-entity))

(defn set-enemy-in-zone [{:keys [in-zone? enemy?] :as enemy-entity}]
  (if (and (not in-zone?) enemy?)
    (let [r (rectangle 0 0 u/w-width u/w-height)]
      (if (u/in-rectangle? enemy-entity r)
        (assoc enemy-entity :in-zone? true)
        enemy-entity))
    enemy-entity))

(def enemy-index
  {1 [25 3]
   2 [50 3]
   3 [75 3]
   4 [105 3]
   5 [135 3]
   6 [170 3]})

(def directions [:right :bottom :left])

(defn create-enemy-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 3))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 0.7 :shape)
         (body! body :create-fixture))
    body))

(defn spawn-enemy [screen entities]
  (let [{:keys [level]} (find-cell entities)
         index (+ level 1 (u/rand-sign 1))
        sheet (u/memo-texture (str "entities/ennemi" index ".png"))
        tiles (texture! sheet :split (first (enemy-index index)) (first (enemy-index index)))
        en-images (for [col (range (second (enemy-index index)))]
                    (texture (aget tiles 0 col)))
        stand (first en-images)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))
        x-max (- u/z-width width)
        y-max (- u/z-height height)
        z-side (directions (rand-int 3))
        [x y x-velocity y-velocity]
        (condp = z-side
          :right [(+ x-max) (rand-int y-max) (- u/cell-x-velocity) 0]
          :bottom [(rand-int x-max) 0 0 u/cell-y-velocity]
          :left [0 (rand-int y-max) u/cell-x-velocity 0])]
    (println x y x-velocity y-velocity z-side)
    (doto
        (assoc stand
          :stand stand
          :walk (animation 0.1 en-images :set-play-mode (play-mode :loop-pingpong))
          :body (create-enemy-body! screen (/ width 2))
          :width width :height height
          :enemy? true
          :z-side z-side)
      (body-position! x y 0)
      (body! :set-linear-velocity x-velocity y-velocity))))

(defn may-spawn-enemy [{:keys [last-spawn total-time] :as screen} entities]
  (if (and (not= last-spawn (int total-time))
           (= 0 (mod (int total-time) 2)))
    (do
      (println "spawn!")
      (spawn-enemy-sound (find-cell entities))
      [(play-clj.core/update! screen :last-spawn (int total-time)) (conj entities (spawn-enemy screen entities))])
    [screen entities]))