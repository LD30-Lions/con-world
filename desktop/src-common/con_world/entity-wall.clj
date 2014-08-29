(in-ns 'con-world.core)

(defn find-wall [entities]
  (some #(when (:wall? %) %) entities))

(defn create-wall-entity [screen]
  (doto {:body  (phy/create-rect-body! screen u/w-width u/z-height)
                                 :wall? true}
                            (body-position! 0 0 0)))

(def max-enemy-size (u/pixels->world 170))                  ; biggest enemy

(defn create-uber-wall-entity [screen]
  (let [margin (* 3 max-enemy-size)]
    (doto {:body       (phy/create-rect-body! screen (+ margin u/w-width) (+ margin u/z-height))
           :uber-wall? true}
      (body-position! (- (/ margin 2)) (- (/ margin 2)) 0))))