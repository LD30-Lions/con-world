(ns con-world.core
  (:import (com.badlogic.gdx.physics.box2d Contact WorldManifold)
           (com.badlogic.gdx.math Vector2))
  (:require [clojure.pprint :refer [pprint]]
            [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.cell :as cell]
            [con-world.utils :as u]))

(declare main-screen con-world intro-screen game-over-screen)



(defn game-over? [entities]
  (-> (cell/find-cell entities)
      :life
      (<= 0)))

(defscreen main-screen
           :on-show
           (fn [screen _]
             (let [screen (update! screen
                                   :camera (orthographic)
                                   :renderer (stage)
                                   :world (box-2d 0 0))

                   player (cell/create-cell-entity! screen)
                   wall (doto {:body  (cell/create-rect-body! screen u/w-width u/z-height)
                               :wall? true}
                          (body-position! 0 0 0))]
               (size! screen u/w-width u/w-height)
               (add-timer! screen :spawn-enemy 5 3)
               (println "on-show")
               [wall
                (doto player
                  (body-position! 0 0 0)
                  (body! :set-linear-velocity 0 0))
                #_(assoc (label (str (:life player))
                              (color :white)
                              :set-width 30)
                  :y (- u/w-height 16)
                  :score? true)]))

           :on-render
           (fn [screen entities]
             (clear!)
             (if (game-over? entities)
               (set-screen! con-world game-over-screen)
               (->> entities
                    (step! screen)
                    cell/change-cell-level
                    (map cell/set-enemy-in-zone)
                    (map cell/move-enemy)
                    (render! screen))))

           :on-key-down
           (fn [screen entities]
             (condp = (:key screen)
               (key-code :dpad-up)
               (replace {(cell/find-cell entities)
                          (cell/move-cell entities :up)}
                        entities)
               (key-code :dpad-down)
               (replace {(cell/find-cell entities)
                          (cell/move-cell entities :down)}
                        entities)
               (key-code :dpad-left)
               (replace {(cell/find-cell entities)
                          (cell/move-cell entities :left)}
                        entities)
               (key-code :dpad-right)
               (replace {(cell/find-cell entities)
                          (cell/move-cell entities :right)}
                        entities)
               nil))

           :on-resize
           (fn [screen _]
             (size! screen u/w-width u/w-height))

           :on-end-contact
           (fn [screen entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {p-width :width :as player} (cell/find-cell coliding-entities)
                   {e-width :width :as enemy} (cell/find-enemy coliding-entities)
                   score (cell/find-score entities)]
               (if (and player enemy)
                 (if (< p-width e-width)
                   (let [new-life (- (:life player) 5)
                         new-level (condp <= new-life
                                     5 1
                                     10 2
                                     15 3
                                     20 4
                                     25 5
                                     (:level player))]
                     (->> entities
                          (replace {player (assoc player :life new-life :level new-level)})
                          #_(replace {score (doto score
                                            (label! :set-text (str new-life)))})))
                   (let [new-life (inc (:life player))]
                     (->> entities
                          (remove #(= % enemy))
                          #_(replace {score (doto score
                                            (label! :set-text (str new-life)))})
                          (replace {player (assoc player :life new-life)}))))
                 entities)))

           :on-pre-solve
           (fn [{:keys [^Contact contact] :as screen} entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {:keys [z-side in-zone?] :as enemy} (cell/find-enemy coliding-entities)
                   wall (cell/find-wall coliding-entities)
                   normal (-> contact
                              (.getWorldManifold)
                              (.getNormal))
                   disable-contact (and (not in-zone?)
                                        (or
                                          (and (= z-side :right) (= normal (vector-2 1.0 0.0)))
                                          (and (= z-side :bottom) (= normal (vector-2 0.0 -1.0)))
                                          (and (= z-side :left) (= normal (vector-2 -1.0 0.0)))
                                          ))]
               (when (and enemy wall disable-contact disable-contact)
                 (.setEnabled contact false))
               entities))

           :on-timer
           (fn [screen entities]
             (println "\n------ O N  T I M E R ------------\n")
             (if (= :spawn-enemy (:id screen))
               (conj entities (cell/spawn-enemy screen))
               entities)))

(defscreen intro-screen
           :on-show
           (fn [screen _]
             (update! screen :renderer (stage) :camera (orthographic))
             (label "Tu veux jouer avec des spores et des arbres ?" (color :green)))

           :on-render
           (fn [screen entities]
             (clear! 0 0 0 1)
             (render! screen entities))

           :on-key-down
           (fn [_ entities]
             (set-screen! con-world main-screen)
             entities)

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))

(defscreen game-over-screen
           :on-show
           (fn [screen _]
             (update! screen :renderer (stage) :camera (orthographic))
             (label "Game over" (color :red)))

           :on-render
           (fn [screen entities]
             (clear! 0 0 0 1)
             (render! screen entities))

           :on-key-down
           (fn [_ entities]
             (set-screen! con-world main-screen)
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
