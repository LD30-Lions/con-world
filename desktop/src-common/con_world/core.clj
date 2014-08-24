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
            [con-world.cell :as cell]
            [con-world.utils :as u]))

(declare main-screen con-world intro-screen game-over-screen score-screen main-bg-screen)



(defn game-over? [entities]
  (-> (cell/find-cell entities)
      :life
      (<= 0)))

(defn stage-fit-vp [camera]
  (stage :set-viewport (FitViewport. u/res-width u/res-height camera)))

(defn on-key-move-cell [entities direction]
  (cell/cell-move-sound)
  (replace {(cell/find-cell entities)
                            (cell/move-cell entities direction)}
                          entities))
(defscreen main-screen
           :on-show
           (fn [screen _]
             (let [camera (orthographic)
                   screen (update! screen
                                   :camera camera
                                   :renderer (stage-fit-vp camera)
                                   :world (box-2d 0 0)
                                   :last-spawn 0
                                   :music (u/memo-sound "sound/fond.mp3"))
                   player (cell/create-cell-entity! screen)
                   wall (doto {:body  (cell/create-rect-body! screen u/w-width u/z-height)
                               :wall? true}
                          (body-position! 0 0 0))]
               (add-timer! screen :ambiant-sound 3 3)
               (sound! (:music screen) :loop)
               [wall
                (cell/create-plante-zone! screen)
                (doto player
                  (body-position! 0 0 0)
                  (body! :set-linear-velocity 0 0))
                ]))

           :on-render
           (fn [screen entities]
             (let [[screen entities] (cell/may-spawn-enemy screen entities)]
               (clear!)
               (if (game-over? entities)
                 (do (set-screen! con-world game-over-screen)
                     (sound! (:music screen) :stop))
                 (->> entities
                      (step! screen)
                      cell/change-cell-level
                      (cell/animate-cell screen)
                      (cell/animate-plante screen)
                      (cell/animate-enemies screen)
                      (map cell/set-enemy-in-zone)
                      (map cell/move-enemy)
                      (render! screen)))))

           :on-key-down
           (fn [screen entities]
             (condp = (:key screen)

               (key-code :dpad-up)
               (on-key-move-cell entities :up)

               (key-code :dpad-down)
               (on-key-move-cell entities :down)

               (key-code :dpad-left)
               (on-key-move-cell entities :left)

               (key-code :dpad-right)
               (on-key-move-cell entities :right)

               nil))

           :on-resize
           (fn [screen _]
             (size! screen u/w-width u/w-height))

           :on-end-contact
           (fn [screen entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {p-width :width :as player} (cell/find-cell coliding-entities)
                   {e-width :width :as enemy} (cell/find-enemy coliding-entities)]
               (if (and player enemy)
                 (if (< p-width e-width)
                   (let [new-life (- (:life player) 5)
                         new-level (cell/calculate-level player)
                         new-level? (not= new-level (:level player))]
                     (cell/touched-by-enemy-sound)
                     (when new-level? (cell/changed-level-sound player))
                     (run! score-screen :update-score :score new-life)
                     (run! score-screen :update-level :level new-level)
                     (replace {player (assoc player :life new-life :level new-level)} entities))
                   (let [new-life (inc (:life player))
                         new-level (cell/calculate-level player)
                         new-level? (not= new-level (:level player))]
                     (println "new-life" new-life "new-level" new-level)
                     (cell/kill-enemy-sound player)
                     (when new-level? (cell/changed-level-sound player))
                     (run! score-screen :update-level :level new-level)
                     (run! score-screen :update-score :score new-life)
                     (->> entities
                          (remove #(= % enemy))
                          (replace {player (assoc player :life new-life :level new-level)}))))
                 entities)))

           :on-pre-solve
           (fn [{:keys [^Contact contact] :as screen} entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {:keys [z-side in-zone?] :as enemy} (cell/find-enemy coliding-entities)
                   wall (cell/find-wall coliding-entities)
                   plante-zone (cell/find-plante-zone coliding-entities)
                   player (cell/find-cell coliding-entities)
                   normal (-> contact
                              (.getWorldManifold)
                              (.getNormal))
                   disable-contact (and (not in-zone?)
                                        #_(cell/in-rectangle? enemy (rectangle 0 0 u/z-width u/z-height))
                                        (or
                                          (and (= z-side :right) (= normal (vector-2 1.0 0.0)))
                                          (and (= z-side :bottom) (= normal (vector-2 0.0 -1.0)))
                                          (and (= z-side :left) (= normal (vector-2 -1.0 0.0)))
                                          ))]
               (when (and enemy wall disable-contact disable-contact)
                 (.setEnabled contact false))
               (when (and plante-zone player)
                 (.setEnabled contact false))
               entities))

           :on-timer
           (fn [screen entities]
             (println "timer")
             (when (= :ambiant-sound (:id screen))
               (when (= 0 (mod (rand-int 2) 2))
                 (cell/ambiant-sound (cell/find-cell entities)))
               entities)))

(defscreen main-bg-screen
           :on-show
           (fn [screen _]
             (let [camera (orthographic)]
               (update! screen
                        :camera camera
                        :renderer (stage-fit-vp camera))
               (u/memo-texture "main-screen-background-1.png")))

           :on-render
           (fn [screen entities]
             #_(println entities)
             (clear! 0 1 0 1)
             (render! screen entities))

           :on-resize
           (fn [screen _]
             (size! screen u/w-width u/w-height)))

(defscreen score-screen
           :on-show
           (fn [screen _]
             (update! screen :renderer (stage) :camera (orthographic))
             [(assoc (label "vie 1" (color :white))
                :y (- (game :height) 16)
                :score? true)
              (assoc (label "plante 1" (color :white))
                :y (- (game :height) 16) :x 100
                :plante-vie? true)
              (assoc (label "level 1" (color :white))
                :y (- (game :height) 16) :x 200
                :level? true)])


           :on-render
           (fn [screen entities]
             (render! screen entities))

           :update-score
           (fn [{:keys [score]} entities]
             (let [score-label (cell/find-score entities)]
               (replace {score-label (doto score-label (label! :set-text (str "vie " score)))} entities)))

           :update-level
           (fn [{:keys [level]} entities]
             (let [level-label (cell/find-level entities)]
               (replace {level-label (doto level-label (label! :set-text (str "level " level)))} entities)))

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))

(defn intro-screen-background []
  (u/memo-texture "intro-screen-background.png"))

(defn start-main-screens []
  (set-screen! con-world main-screen score-screen #_main-bg-screen))

(defscreen intro-screen
           :on-show
           (fn [screen _]
             (let [background (intro-screen-background)
                   music (u/memo-sound "intro-screen-music.mp3")
                   camera (orthographic)]
               (update! screen
                        :renderer (stage-fit-vp camera)
                        :camera camera
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
             (let [camera (orthographic)]
               (update! screen
                        :camera camera
                        :renderer (stage-fit-vp camera)))
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
