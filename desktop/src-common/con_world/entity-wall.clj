(in-ns 'con-world.core)

(defn find-wall [entities]
  (some #(when (:wall? %) %) entities))

(defn create-wall-entity [screen]
  (doto {:body  (phy/create-rect-body! screen u/w-width u/z-height)
                                 :wall? true}
                            (body-position! 0 0 0)))

(def max-enemy-size (u/pixels->world 170))                  ; biggest enemy

(defn create-uber-wall-entity [screen]
  (doto {:body  (phy/create-rect-body! screen (+ (* 2 max-enemy-size) u/w-width) (+ (* 2 max-enemy-size) u/z-height))
                                 :uber-wall? true}
                            (body-position! (- max-enemy-size) (- max-enemy-size) 0)))