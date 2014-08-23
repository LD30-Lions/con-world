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
                          (body-position! 0 0 0))]

               (size! screen u/w-width u/w-height)
               [wall
                (doto (cell/create-cell-entity! screen)
                  (body-position! 0 0 0)
                  (body! :set-linear-velocity 0 0))
                (cell/spawn-enemy screen)]))

           :on-render
           (fn [screen entities]
             (clear!)
             #_(-> (cell/find-cell entities)
                 (body! :get-linear-velocity)
                 println)
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
             (size! screen u/w-width u/w-height)))

(defgame con-world
         :on-create
         (fn [this]
           (set-screen! this main-screen)))
