(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]))

(def pixels-per-tile 20) ; 540 / 27 = 20
(defn pixels->world [size]
  (/ size pixels-per-tile))

(def res-width 540)
(def res-height 768)

(def w-width (pixels->world res-width))
(def w-height (pixels->world res-height))

(def z-width w-width)
(def z-height (- w-height (pixels->world 290)))

(def cell-x-velocity 25)
(def cell-y-velocity 25)

(def enemy-x-velocity 15)
(def enemy-y-velocity 15)

(def moving-slow 1)

(def memo-texture (memoize (fn [name] (texture name))))

(def memo-sound (memoize (fn [name] (sound name))))

