(ns con-world.screen-intro
  (:require [con-world.sound :as snd]
            [con-world.utils :as u]
            [con-world.utils-graphics :as gfx]
            [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]))

(declare intro-screen)

(defn intro-screen-background []
  (u/memo-texture "intro-screen-background.png"))

(defscreen intro-screen
           :on-show
           (fn [screen _]
             (let [background (intro-screen-background)
                   music (snd/memo-sound "intro-screen-music.mp3")]
               (update! (gfx/init-graphic-settings screen)
                        :bg-width (texture! background :get-region-width)
                        :bg-height (texture! background :get-region-height)
                        :music music)
               (sound! music :loop)
               [background
                (label "Tu veux jouer avec des spores et des arbres ?" (color :green))]))

           :on-render
           (fn [{:keys [music kill-screen?] :as screen} entities]
             (if kill-screen?
               (do
                 (update! screen :kill-screen? false)
                 (u/transition-screen! screen :main)
                 (sound! music :stop))
               (do (clear! 0 0 0 1)
                   (render! screen entities))))

           :on-key-down
           (fn [screen entities]
             (update! screen :kill-screen? true)
             entities)

           :on-touch-down
           (fn [screen entities]
             (update! screen :kill-screen? true)
             entities)

           :on-resize
           (fn [screen entities]
             (size! screen (screen :bg-width) (screen :bg-height))
             entities))
