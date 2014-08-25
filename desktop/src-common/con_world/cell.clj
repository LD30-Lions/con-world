(ns con-world.cell
  (:import (com.badlogic.gdx.physics.box2d Filter)
           (com.badlogic.gdx.math Vector2))
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.core :refer [sound!] :as pcore]
            [con-world.utils :as u]))

(defn find-plante-zone [entities]
  (some #(when (:plante-zone? %) %) entities))

(defn create-rect-body!
  [screen width height]
  (let [body (add-body! screen (body-def :static))]
    (->> [0 0
          0 height
          width height
          width 0
          0 0]
         float-array
         (chain-shape :create-chain)
         (fixture-def :density 1 :shape)
         (body! body :create-fixture))
    body))

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
      :body (doto (create-rect-body! screen width height)
              (body-position! (- (/ u/z-width 2) (/ width 2)) (- u/z-height height) 0))
      :width width :height height
      :plante-zone? true)))

(defn calculate-level [{:keys [life]}]
  (let [mapping [[5 1], [10 2], [15 3], [20 4], [25 5]]]
    (some (fn [[map-life map-level]]
            (when (< life map-life) map-level))
          mapping)))

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

(defn animate-plante [screen entities]
  (let [plante (find-plante-zone entities)]
    (replace {plante (merge plante (animation->texture screen (:move plante)))} entities)))
