(ns con-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.cell :as cell]
            [con-world.utils :as u]))

(declare main-screen con-world)

(defscreen main-screen
           :on-show
           (fn [screen _]
             (let [screen (update! screen
                                   :camera (orthographic)
                                   :renderer (stage)
                                   :world (box-2d 0 0))
                   wall (doto {:body (u/create-rect-body! screen u/w-width u/z-height)}
                          (body-position! 0 0 0))
                   player (cell/create-cell-entity! screen)]

               (size! screen u/w-width u/w-height)
               [wall
                (doto player
                  (body-position! 0 0 0)
                  (body! :set-linear-velocity 0 0))
                (cell/spawn-enemy screen)
                (assoc (label (str (:life player))
                              (color :white)
                              :set-width 30)
                    :y (- u/w-height 16)
                    :score? true)]))

           :on-render
           (fn [screen entities]
             (clear!)
             (->> entities
                  (step! screen)
                  (render! screen)))

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

           :on-begin-contact
           (fn [screen entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {p-width :width :as player} (cell/find-cell coliding-entities)
                   {e-width :width :as enemy} (cell/find-enemy coliding-entities)
                   score (cell/find-score entities)]
               (if (and player enemy)
                 (if (< p-width e-width)
                   (let [new-life (- (:life player) 5)]
                     (->> entities
                          (replace {player (assoc player :life new-life)})
                          (replace {score (doto score
                                            (label! :set-text (str new-life)))})))
                   (let [new-life (inc (:life player))]
                     (->> entities
                          (remove #(= % enemy))
                          (replace {score (doto score
                                            (label! :set-text (str new-life)))})
                          (replace {player (assoc player :life new-life)}))))
                 entities))))

(defgame con-world
         :on-create
         (fn [this]
           (set-screen! this main-screen)))
