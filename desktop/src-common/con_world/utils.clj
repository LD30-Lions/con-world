(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]))

(def pixels-per-tile 22) ; 528 / 24 = 22
(defn pixels->world [size]
  (/ size pixels-per-tile))

(def w-width (pixels->world 1024))
(def w-height (pixels->world 768))

(def z-width w-width)
(def z-height (- w-height (pixels->world 290)))

(def x-velocity 25)
(def y-velocity 25)

(def moving-slow -1)

(def memo-texture (memoize (fn [name] (texture name))))

(def memo-sound (memoize (fn [name] (sound name))))
