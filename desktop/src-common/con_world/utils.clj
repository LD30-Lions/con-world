(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.core :refer :all]))

(def pixels-per-tile 22) ; 528 / 24 = 22
(defn pixels->world [size]
  (/ size pixels-per-tile))

(def w-width (pixels->world (game :width)))
(def w-height (pixels->world (game :height)))
(def z-width w-width)
(def z-height (- w-height (pixels->world 290)))

(def x-velocity 25)
(def y-velocity 25)

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

