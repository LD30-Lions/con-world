(ns con-world.screen-gameover
  (:require [play-clj.core :refer :all]
            [play-clj.ui :refer :all]
            [con-world.utils-graphics :as gfx]
            [con-world.utils :as u]))

(declare game-over-screen)

(defscreen game-over-screen
           :on-show
           (fn [screen _]
             (gfx/init-graphic-settings screen)
             ; FIX son de Ben
             #_(sound! (u/memo-sound "sound/gameover.wav") :play)
             (label "Game over" (color :red)))

           :on-render
           (fn [screen entities]
             (clear! 0 0 0 1)
             (render! screen entities))

           :on-key-down
           (fn [screen entities]
             (u/transition-screen! screen :main)
             entities)

           :on-touch-down
           (fn [screen entities]
             (u/transition-screen! screen :main)
             entities)

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))
