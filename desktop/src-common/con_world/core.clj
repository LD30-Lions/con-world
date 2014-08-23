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
             #_:world #_(box-2d 0 0))
    #_(create-cell-entity! screen)
    (assoc (label "foo" (color :white))
      :x 5
      :y 5))
  
  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities)
    entities))

(defgame con-world
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
