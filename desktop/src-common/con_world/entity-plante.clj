(in-ns 'con-world.core)

(defn find-plante-zone [entities]
  (some #(when (:plante-zone? %) %) entities))

(defn create-plante-zone!
  [screen]
  (let [sheet (texture "entities/racines.png")
        tiles (texture! sheet :split 130 130)
        plante-images (for [col [0 1 2 3]]
                        (texture (aget tiles 0 col)))
        stand (first plante-images)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))]
    (assoc stand
      :stand stand
      :move (animation 0.1 plante-images :set-play-mode (play-mode :loop-pingpong))
      :body (doto (phy/create-rect-body! screen width height)
              (body-position! (- (/ u/z-width 2) (/ width 2)) (- u/z-height height) 0))
      :width width :height height
      :plante-zone? true)))


(defn animate-plante [screen entities]
  (let [plante (find-plante-zone entities)]
    (replace {plante (merge plante (animation->texture screen (:move plante)))} entities)))
