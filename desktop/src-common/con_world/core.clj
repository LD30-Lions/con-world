(ns con-world.core
  (:import (com.badlogic.gdx.physics.box2d Contact WorldManifold)
           (com.badlogic.gdx.math Vector2)
           (com.badlogic.gdx.utils.viewport FitViewport))
  (:require [clojure.pprint :refer [pprint]]
            [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.physics :as phy]
            [con-world.utils :as u]))

(declare main-screen con-world intro-screen game-over-screen score-screen main-bg-screen)

(defn start-main-screens []
  (set-screen! con-world main-screen score-screen #_main-bg-screen))

(load "utils-graphics")
(load "sound")

(load "entity-wall")
(load "entity-player")
(load "entity-enemy")
(load "entity-plante")

(load "screen-main")
(load "screen-score")
(load "screen-intro")
(load "screen-gameover")


(defgame con-world
         :on-create
         (fn [this]
           (set-screen! this intro-screen)))

(load "debug")

