(in-ns 'con-world.core)

(defn find-enemy
  ([entities]
   (some #(when (:enemy? %) %) entities))
  ([entities enemy-id]
   (some
     (fn [{:keys [enemy? id] :as enemy}]
       (when (and enemy? (= id enemy-id)) enemy))
     entities)))

(defn random-vec2
  [x-max y-max]
  {:post [(and (< (.x %) x-max) (> (.x %) (- x-max))
               (< (.y %) y-max) (> (.y %) (- y-max)))]}
  (let [added-velocity (map #(-> % rand-int u/rand-sign) [u/enemy-x-velocity u/enemy-y-velocity])]
    (apply vector-2* added-velocity)))

(defn change-enemy-velocity [enemy-entity added-velocity]
  (phy/add-velocity enemy-entity added-velocity))

(defn moving-fast? [entity]
  (let [^Vector2 vec2-velocity (body! entity :get-linear-velocity)
        x-velocity (.x vec2-velocity)
        y-velocity (.y vec2-velocity)]
    (or (> x-velocity u/moving-slow) (> y-velocity u/moving-slow))))

(defn move-enemy [{:keys [enemy? in-zone? id] :as enemy-entity}]
  (when (and enemy? in-zone? (not (moving-fast? enemy-entity)))
    (let [added-velocity (random-vec2 u/enemy-x-velocity u/enemy-y-velocity)]
      (add-event [:enemy-moved id added-velocity]))))

(def enemy-damping 3)
(def enemy-restitution 0.7)

(defn set-enemy-restitution [enemy-entity enemy-restitution]
  (-> enemy-entity
      (body! :get-fixture-list)
      (.get 0)
      (fixture! :set-restitution enemy-restitution))
  enemy-entity)

(defn set-enemy-in-zone [{:keys [in-zone? enemy? id] :as enemy-entity}]
  (when (and (not in-zone?) enemy?)
    (let [r (rectangle 0 0 u/z-width u/z-height)]
      (when (u/in-rectangle? enemy-entity r)
        (add-event [:enemy-entered-zone id])))))

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
  (let [body (add-body! screen (body-def :dynamic :linear-damping 0))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn init-enemy-body-spacial-settings [{:keys [width height z-side] :as enemy-entity}]
  (let [x-max (- u/z-width width)
        y-max (- u/z-height height)
        box2d-margin-bug 0.5                                ; when a body is precisely created against another body it seems that we can't disable collision. So this value is used to unstick the two bodies. See http://box2d.org/forum/viewtopic.php?f=3&t=9890&p=40789#p40789
        [x y x-velocity y-velocity]
        (condp = z-side
          :right [(+ x-max width box2d-margin-bug) (rand-int y-max) (- u/cell-x-velocity) 0]
          :bottom [(rand-int x-max) (- (+ height box2d-margin-bug)) 0 u/cell-y-velocity]
          :left [(- (+ width box2d-margin-bug)) (rand-int y-max) u/cell-x-velocity 0])]
    (doto
      (assoc enemy-entity :x x :y y)                                     ; body position will just set physical x and y. When calling (step!) then :x and :y is set
      (body-position! x y 0)
      (body! :set-linear-velocity x-velocity y-velocity))))

(defn spawn-enemy [screen entities]
  (let [{:keys [level]} (find-player entities)
        e-level (inc (rand-int (inc level)))
        [size nb-sprite] (enemy-index e-level)
        {en-images :sprites stand :first} (image->sprite (str "entities/ennemi" e-level ".png") size size nb-sprite)
        width (u/pixels->world size)]
    (->
      (assoc stand
        :id (inc-id)
        :stand stand
        :walk (animation 0.1 en-images :set-play-mode (play-mode :loop-pingpong))
        :body (create-enemy-body! screen (/ width 2))
        :width width
        :height (u/pixels->world size)
        :enemy? true
        :z-side (directions (rand-int 3))
        :level e-level)
      (init-enemy-body-spacial-settings))))


(defn animate-enemies [screen entities]
  (map (fn [entity]
         (if (:enemy? entity)
           (let [velocity (body! entity :get-linear-velocity)
                 not-moving (vector-2! velocity :is-zero 1)]
             (if not-moving
               (merge entity (:stand entity))
               (merge entity (animation->texture screen (:walk entity)))))
           entity))
       entities))
