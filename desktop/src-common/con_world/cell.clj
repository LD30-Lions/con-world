(ns con-world.cell
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [con-world.utils :as u]))

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
        width (texture! ball :get-region-width)
        height (texture! ball :get-region-height)]
    (assoc ball
      :body (create-cell-body! screen (/ width 2))
      :width width :height height
      :cell? true)))

(defn change-velocity [entity direction]
  (let [current-velocity (body! entity :get-linear-velocity)
        new-velocity (condp = direction
                       :up    (vector-2 0 200)
                       :down  (vector-2 0 -200)
                       :left  (vector-2 -200 0)
                       :right (vector-2 200 0))]
    (body! entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    entity))

(defn move-cell [entities direction]
  (let [cell (some #(when (:cell? %) %) entities)]
    (change-velocity cell direction)))

(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))

(defn create-enemy-body!
  [screen radius]
  (let [body (add-body! screen (body-def :kinematic :linear-damping 5))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn spawn-enemy [screen]
  (let [enemy (texture "enemy.png")
        width (texture! enemy :get-region-width)
        height (texture! enemy :get-region-height)
        x-max (- u/z-width width)
        y-max (- u/z-height height)
        z-side (rand-int 4)
        [x y] (condp = z-side
                0 [(rand-int x-max) y-max]
                1 [x-max (rand-int y-max)]
                2 [(rand-int x-max) 0]
                3 [0 (rand-int y-max)])]
    (println [x y])
    (doto
        (assoc enemy
          :body (create-enemy-body! screen (/ width 2))
          :width width :height height
          :enemy? true)
      (body-position! x y 0))))
