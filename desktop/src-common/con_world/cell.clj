(ns con-world.cell
  (:import (com.badlogic.gdx.physics.box2d Filter)
           (com.badlogic.gdx.math Vector2))
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
      :life 1
      :level 1)))

(def direction->velocity
  {:up    (vector-2 0 u/y-velocity)
   :down  (vector-2 0 (- u/y-velocity))
   :left  (vector-2 (- u/x-velocity) 0)
   :right (vector-2 u/x-velocity 0)})

(defn change-cell-velocity [cell-entity direction]
  (let [current-velocity (body! cell-entity :get-linear-velocity)
        new-velocity (direction direction->velocity)]
    (body! cell-entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    cell-entity))

(defn rand-sign [x]
  (if (= 0 (mod (rand-int 2) 2)) (* -1 x) (* 1 x)))

(defn change-enemy-velocity [enemy-entity]
  (let [current-velocity (body! enemy-entity :get-linear-velocity)
        new-velocity (vector-2 (rand-sign (rand-int u/x-velocity)) (rand-sign (rand-int u/y-velocity)))]
    (body! enemy-entity
           :set-linear-velocity (vector-2! current-velocity :add new-velocity))
    enemy-entity))

(defn moving-fast? [entity]
  (let [^Vector2 vec2-velocity (body! entity :get-linear-velocity)
        x-velocity (.x vec2-velocity)
        y-velocity (.y vec2-velocity)]
    (or (> x-velocity u/moving-slow) (> y-velocity u/moving-slow))))

(defn move-enemy [{:keys [enemy?] :as entity}]
  (if (and enemy? (not (moving-fast? entity)))
    (change-enemy-velocity entity)
    entity))

(defn in-rectangle? [{:keys [x y width height]} r]
  (and (rectangle! r :contains x y) (rectangle! r :contains (+ x width) (+ y height))))

(defn set-enemy-in-zone [{:keys [in-zone? enemy?] :as entity}]
  (if (and (not in-zone?) enemy?)
    (let [r (rectangle 0 0 u/w-width u/w-height)]
      (if (in-rectangle? entity r)
        (do
          (println "in zone")
          (assoc entity :in-zone? true))
        entity))
    entity))


(defn move-cell [entities direction]
  (let [cell (some #(when (:cell? %) %) entities)]
    (change-cell-velocity cell direction)))

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
  (let [body (add-body! screen (body-def :dynamic :linear-damping 3))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 0.7 :shape)
         (body! body :create-fixture))
    body))

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

(def direction [:right :bottom :left])


(defn spawn-enemy [screen]
  (let [enemy (u/memo-texture (str "enemy" (+ 1 (rand-int 3)) ".png"))
        width (u/pixels->world (texture! enemy :get-region-width))
        height (u/pixels->world (texture! enemy :get-region-height))
        x-max (- u/z-width width)
        y-max (- u/z-height height)
        z-side (direction (rand-int 3))
        [x y x-velocity y-velocity]
        (condp = z-side
          :right [(+ x-max width) (rand-int y-max) (- u/x-velocity) u/y-velocity]
          :bottom [(rand-int x-max) (- height) u/x-velocity u/y-velocity]
          :left [(- width) (rand-int y-max) u/x-velocity u/y-velocity])]
    (doto
        (assoc enemy
          :body (create-enemy-body! screen (/ width 2))
          :width width :height height
          :enemy? true
          :z-side z-side)
      (body-position! x y 0)
      (body! :set-linear-velocity x-velocity y-velocity))))

(defn change-cell-level [entities]
  (let [cell (find-cell entities)]
    (replace {cell (merge cell
                          (u/memo-texture (str "cell" (:level cell) ".png")))}
             entities)))
