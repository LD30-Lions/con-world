(ns con-world.utils
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.core :refer :all]))

(def pixels-per-tile 22) ; 528 / 24 = 22
(defn pixels->world [size]
  (/ size pixels-per-tile))

(def w-width (pixels->world 1024 #_(game :width)))
(def w-height (pixels->world 768 #_(game :height)))
(def z-width w-width)
(def z-height (- w-height (pixels->world 290)))

(def x-velocity 25)
(def y-velocity 25)

