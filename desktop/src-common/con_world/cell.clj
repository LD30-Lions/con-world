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
        width (u/pixels->world (texture! ball :get-region-width))
        height (u/pixels->world (texture! ball :get-region-height))]
    (assoc ball
      :body (create-cell-body! screen (/ width 2))
      :width width :height height
      :cell? true
      :life 1)))

(def direction->velocity
  {:up    (vector-2 0 u/y-velocity)
   :down  (vector-2 0 (- u/y-velocity))
   :left  (vector-2 (- u/x-velocity) 0)
   :right (vector-2 u/x-velocity 0)})

(defn change-velocity [entity direction]
  (let [current-velocity (body! entity :get-linear-velocity)
        new-velocity (direction direction->velocity)]
    (body! entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    entity))

(defn move-cell [entities direction]
  (let [cell (some #(when (:cell? %) %) entities)]
    (change-velocity cell direction)))

(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))

(defn find-enemy [entities]
  (some #(when (:enemy? %) %) entities))

(defn find-wall [entities]
  (some #(when (:wall? %) %) entities))

(defn find-score [entities]
  (some #(when (:score? %) %) entities))

(defn create-enemy-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 5))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 1 :shape)
         (body! body :create-fixture))
    body))

(defn spawn-enemy [screen]
  (let [enemy (texture (str "enemy" (+ 1 (rand-int 3)) ".png"))
        width (u/pixels->world (texture! enemy :get-region-width))
        height (u/pixels->world (texture! enemy :get-region-height))
        x-max (- u/z-width width)
        y-max (- u/z-height height)
        z-side (rand-int 4)
        [x y x-velocity y-velocity]
              (condp = z-side
                0 [(rand-int x-max) y-max u/x-velocity (- u/y-velocity)]
                1 [x-max (rand-int y-max) (- u/x-velocity) u/y-velocity]
                2 [(rand-int x-max) 0 u/x-velocity u/y-velocity]
                3 [0 (rand-int y-max) u/x-velocity u/y-velocity])]
    (doto
        (assoc enemy
          :body (create-enemy-body! screen (/ width 2))
          :width width :height height
          :enemy? true)
      (body-position! x y 0)
      (body! :set-linear-velocity x-velocity y-velocity))))
