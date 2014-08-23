(ns con-world.cell
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]))

(defn create-cell-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic))]
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

(defn move-cell [entities direction]
  (let [cell (some #(when (:cell? %) %) entities)]
    (println (body! cell :get-linear-velocity))
    cell
    #_(condp = direction
      :up   (assoc cell
              :y (+ (:y cell) 5))
      :down (assoc cell
              :y (- (:y cell) 5))
      :left (assoc cell
              :x (- (:x cell) 5))
      :right (assoc cell
               :x (+ (:x cell) 5))
      cell)))

(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))
