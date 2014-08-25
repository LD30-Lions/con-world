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



