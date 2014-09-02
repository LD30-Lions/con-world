(ns con-world.entity-plante
  (:require [con-world.utils :as u]
            [con-world.physics :as phy]
            [con-world.utils-graphics :as gfx]
            [play-clj.g2d :refer :all]
            [play-clj.g2d-physics :refer [body-position!]]))

(defn find-plante-zone [entities]
  (some #(when (:plante-zone? %) %) entities))

(defn create-plante-zone!
  [screen]
  (let [{plante-images :sprites stand :first} (gfx/image->sprite "entities/racines.png" 130 130 4)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))]
    (assoc stand
      :move (animation 0.1 plante-images :set-play-mode (play-mode :loop-pingpong))
      :body (doto (phy/create-rect-body! screen width height)
              (body-position! (- (/ u/z-width 2) (/ width 2)) (- u/z-height height) 0))
      :width width :height height
      :plante-zone? true)))


(defn animate-plante [screen entities]
  (let [plante (find-plante-zone entities)]
    (replace {plante (merge plante (animation->texture screen (:move plante)))} entities)))
