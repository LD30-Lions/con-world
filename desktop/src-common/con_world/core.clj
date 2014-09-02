(ns con-world.core
  (:require [play-clj.core :refer :all]
            [con-world.screen-score :as score]
            [con-world.screen-main :as main]
            [con-world.screen-gameover :as gameover]
            [con-world.screen-intro :as intro]))

(defgame con-world
         :on-create
         (fn [this]
           (swap! (-> intro/intro-screen :screen) assoc :next-screens {:main [main/main-screen score/score-screen]} :game con-world)
           (swap! (-> main/main-screen :screen) assoc :next-screens {:main [main/main-screen score/score-screen]} :game con-world)
           (swap! (-> gameover/game-over-screen :screen) assoc :next-screens {:main [main/main-screen score/score-screen]} :game con-world)
           ;(update! intro/intro-screen :next-screens {:main [main/main-screen score/score-screen]} :game con-world)
           ;(update! main/main-screen :next-screens {:gameover [gameover/game-over-screen]} :game con-world)
           ;(update! gameover/game-over-screen :next-screens {:main [main/main-screen score/score-screen]} :game con-world)
           (set-screen! this intro/intro-screen)))
