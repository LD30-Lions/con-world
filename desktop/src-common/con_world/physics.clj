(ns con-world.physics
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.core :refer :all]))

(defn set-velocity
  ([entity x-velocity y-velocity]
   (set-velocity entity (vector-2 x-velocity y-velocity)))
  ([entity vec-2]
   (doto entity (body! :set-linear-velocity vec-2))))

(defn add-velocity [entity vec-2]
  (let [current-velocity (body! entity :get-linear-velocity)
        new-velocity (vector-2! current-velocity :add vec-2)]
    (set-velocity entity new-velocity)))
