(in-ns 'con-world.core)


(defn change-enemy-velocity [enemy-entity]
  (let [current-velocity (body! enemy-entity :get-linear-velocity)
        new-velocity (vector-2 (u/rand-sign (rand-int u/enemy-x-velocity)) (u/rand-sign (rand-int u/enemy-y-velocity)))]
    (body! enemy-entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    enemy-entity))

(defn moving-fast? [entity]
  (let [^Vector2 vec2-velocity (body! entity :get-linear-velocity)
        x-velocity (.x vec2-velocity)
        y-velocity (.y vec2-velocity)]
    (or (> x-velocity u/moving-slow) (> y-velocity u/moving-slow))))

(defn move-enemy [{:keys [enemy? in-zone?] :as entity}]
  (if (and enemy? in-zone? (not (moving-fast? entity)))
    (change-enemy-velocity entity)
    entity))



