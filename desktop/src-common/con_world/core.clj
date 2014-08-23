(ns con-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            ))

(declare main-screen con-world)

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
           :width width :height height)))


(defscreen main-screen
  :on-show
  (fn [screen _]
    (update! screen
             :renderer (stage)
             :camera (orthographic)
             :world (box-2d 0 0))
    (create-cell-entity! screen))
  
  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities))

  :on-resize
  (fn [screen entities]
    (height! screen 300)))



(defgame con-world
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
