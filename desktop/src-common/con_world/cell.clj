(ns con-world.cell
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]))

(defn create-cell-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 5))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn create-cell-entity!
  [screen]
  (let [ball (texture "ball.png")
        width (/ (texture! ball :get-region-width) con-world.core/pixels-per-tile)
        height (/ (texture! ball :get-region-height) con-world.core/pixels-per-tile)]
    (assoc ball
      :body (create-cell-body! screen (/ width 2))
      :width 1 :height height
      :cell? true)))

(def direction->velocity
  {:up    (vector-2 0 25)
   :down  (vector-2 0 -25)
   :left  (vector-2 -25 0)
   :right (vector-2 25 0)})

(defn change-velocity [entity direction]
  (let [current-velocity (body! entity :get-linear-velocity)
        new-velocity (direction direction->velocity)]
    (body! entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    entity))

(defn move-cell [entities direction]
  (let [cell (some #(when (:cell? %) %) entities)]
    (println (body! cell :get-linear-velocity))
    (change-velocity cell direction)))

(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))
