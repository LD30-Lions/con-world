(in-ns 'con-world.core)

(defn create-wall-entity [screen]
  (doto {:body  (cell/create-rect-body! screen u/w-width u/z-height)
                                 :wall? true}
                            (body-position! 0 0 0)))