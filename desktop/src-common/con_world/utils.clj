(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]))

(def pixels-per-tile 22) ; 528 / 24 = 22
(defn pixels->world [size]
  (/ size pixels-per-tile))

(def res-width 538)
(def res-height 768)

(def w-width (pixels->world res-width))
(def w-height (pixels->world res-height))

(def z-width w-width)
(def z-height (- w-height (pixels->world 290)))

(def cell-x-velocity 10)
(def cell-y-velocity 10)

(def enemy-x-velocity (+ cell-x-velocity 3))
(def enemy-y-velocity (+ cell-y-velocity 3))

(def moving-slow 1)

(def memo-texture (memoize (fn [name] (texture name))))

(def memo-sound (memoize (fn [name] (sound name))))

