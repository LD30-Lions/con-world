(in-ns 'con-world.core)

(defn find-wall [entities]
  (some #(when (:wall? %) %) entities))

(defn create-wall-entity [screen]
  (doto {:body  (phy/create-rect-body! screen u/w-width u/z-height)
                                 :wall? true}
                            (body-position! 0 0 0)))

(defn create-uber-wall-entity [screen]
  (doto {:body  (phy/create-rect-body! screen (+ 7 u/w-width) (+ 7 u/z-height))
                                 :wall? true}
                            (body-position! (- (/ 7 2)) (- (/ 7 2)) 0)))