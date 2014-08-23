(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]))

(def tile-width 50)

(def w-width (* 10 tile-width))
(def w-height (* 30 tile-width))
(def z-width w-width)
(def z-height (- w-height 200))

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
