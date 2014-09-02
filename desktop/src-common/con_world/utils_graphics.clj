(ns con-world.utils-graphics
  (:import (com.badlogic.gdx.utils.viewport FitViewport))
  (:require [con-world.utils :as u]
            [play-clj.g2d :refer :all]
            [play-clj.core :refer :all]))

(defn stage-fit-vp [camera]
  (stage :set-viewport (FitViewport. u/res-width u/res-height camera)))

(defn init-graphic-settings [screen]
  (let [camera (orthographic)]
    (update! screen
             :camera camera
             :renderer (stage-fit-vp camera))))

(defn image->sprite [image sprite-w sprite-h nb-sprite]
  (let [sheet (u/memo-texture image)
        tiles (texture! sheet :split sprite-w sprite-h)
        [first & _ :as sprites] (for [col (range nb-sprite)]
                  (texture (aget tiles 0 col)))]
    {:sprites sprites
     :first first}))
