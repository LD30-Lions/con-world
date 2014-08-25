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

(load "sound")
(load "entity-wall")
(load "entity-player")
(load "entity-enemy")
(load "entity-plante")

(defn stage-fit-vp [camera]
  (stage :set-viewport (FitViewport. u/res-width u/res-height camera)))

(defn init-graphic-settings [screen]
  (let [camera (orthographic)]
    (update! screen
             :camera camera
             :renderer (stage-fit-vp camera))))


(defn on-key-move-cell [entities direction]
  (cell-move-sound)
  (let [cell (find-cell entities)]
    (replace {cell (move-cell cell direction)}
             entities)))

(defn game-over [screen]
  (do (set-screen! con-world game-over-screen)
      (sound! (:music screen) :stop)))

(def key->direction
  {(key-code :dpad-up)    :up,
   (key-code :dpad-down)  :down,
   (key-code :dpad-left)  :left,
   (key-code :dpad-right) :right})

(defn coliding-entities [screen entities]
  (let [entities (filter #(contains? % :body) entities)
        coliding-entities [(first-entity screen entities) (second-entity screen entities)]]
    {:enemy       (find-enemy coliding-entities)
     :player      (find-cell coliding-entities)
     :plante-zone (find-plante-zone coliding-entities)
     :wall        (find-wall coliding-entities)}))

(defn do-player-lose [entities player]
  (let [new-life (- (:life player) 5)
        new-level (calculate-level player)
        new-level? (not= new-level (:level player))]
    (touched-by-enemy-sound)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-score :score new-life)
    (run! score-screen :update-level :level new-level)
    (replace {player (assoc player :life new-life :level new-level)} entities)))

(defn do-player-win [entities player enemy]
  (let [new-life (inc (:life player))
        new-level (calculate-level player)
        new-level? (not= new-level (:level player))]
    (println "new-life" new-life "new-level" new-level)
    (kill-enemy-sound player)
    (when new-level? (changed-level-sound player))
    (run! score-screen :update-level :level new-level)
    (run! score-screen :update-score :score new-life)
    (->> entities
         (remove #(= % enemy))
         (replace {player (assoc player :life new-life :level new-level)}))))

(defn disable-contact? [contact {:keys [z-side in-zone?]}]
  (let [normal (-> contact
                   (.getWorldManifold)
                   (.getNormal))]
    (and (not in-zone?)
         #_(cell/in-rectangle? enemy (rectangle 0 0 u/z-width u/z-height))
         (or
           (and (= z-side :right) (= normal (vector-2 1.0 0.0)))
           (and (= z-side :bottom) (= normal (vector-2 0.0 -1.0)))
           (and (= z-side :left) (= normal (vector-2 -1.0 0.0)))
           ))))
(defscreen main-screen

           :on-show
           (fn [screen _]

             (let [background (u/memo-texture "main-screen-background-1.png")
                   screen (-> (init-graphic-settings screen)
                              (update!
                                :world (box-2d 0 0)
                                :music (memo-sound "sound/fond.mp3")
                                :bg-width (u/pixels->world (texture! background :get-region-width))
                                :bg-height (u/pixels->world (texture! background :get-region-height))
                                :last-spawn 0))]

               (add-timer! screen :ambiant-sound 3 3)
               (sound! (:music screen) :loop)

               [background
                (create-wall-entity screen)
                (create-plante-zone! screen)
                (create-player-entity screen)]))

           :on-render

           (fn [screen entities]

             (clear!)

             (let [[screen entities] (may-spawn-enemy screen entities)]

               (if (player-dead? entities)

                 (game-over screen)

                 (->> entities
                      (step! screen)
                      change-cell-level
                      (animate-cell screen)
                      (animate-plante screen)
                      (animate-enemies screen)
                      (map set-enemy-in-zone)
                      (map move-enemy)
                      (render! screen)))))

           :on-key-down
           (fn [{:keys [key]} entities]
             (when-let [direction (key->direction key)]
               (on-key-move-cell entities direction)))

           :on-resize
           (fn [screen _]
             (size! screen (:bg-width screen) (:bg-height screen)))

           :on-end-contact
           (fn [screen entities]
             (let [{:keys [player enemy]} (coliding-entities screen entities)]
               (if (and player enemy)
                 (if (player-win? player enemy)
                   (do-player-win entities player enemy)
                   (do-player-lose entities player))
                 entities)))

           :on-pre-solve
           (fn [{:keys [^Contact contact] :as screen} entities]
             (let [{:keys [player enemy wall plante-zone]} (coliding-entities screen entities)
                   disable-contact? (disable-contact? contact enemy)]
               (when (and enemy wall disable-contact?)
                 (.setEnabled contact false))
               (when (and plante-zone player)
                 (.setEnabled contact false))
               entities))

           :on-timer
           (fn [screen entities]
             (when (= :ambiant-sound (:id screen))
               (when (even? (rand-int 2))
                 (ambiant-sound (find-cell entities)))
               entities)))

(defscreen main-bg-screen
           :on-show
           (fn [screen _]
             (init-graphic-settings screen)
             (u/memo-texture "main-screen-background-1.png"))

           :on-render
           (fn [screen entities]
             #!(clear! 0 1 0 1)
             (render! screen entities))

           :on-resize
           (fn [screen _]
             (size! screen u/w-width u/w-height)))


(load "screen-score")

(defn intro-screen-background []
  (u/memo-texture "intro-screen-background.png"))

(defn start-main-screens []
  (set-screen! con-world main-screen score-screen #_main-bg-screen))

(defscreen intro-screen
           :on-show
           (fn [screen _]
             (let [background (intro-screen-background)
                   music (memo-sound "intro-screen-music.mp3")]
               (update! (init-graphic-settings screen)
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
                 (start-main-screens)
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

(defscreen game-over-screen
           :on-show
           (fn [screen _]
             (init-graphic-settings screen)
             ; FIX son de Ben
             #_(sound! (u/memo-sound "sound/gameover.wav") :play)
             (label "Game over" (color :red)))

           :on-render
           (fn [screen entities]
             (clear! 0 0 0 1)
             (render! screen entities))

           :on-key-down
           (fn [_ entities]
             (start-main-screens)
             entities)

           :on-key-down
           (fn [_ entities]
             (set-screen! con-world main-screen score-screen)
             entities)

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))

(defgame con-world
         :on-create
         (fn [this]
           (set-screen! this intro-screen)))

(load "debug")

