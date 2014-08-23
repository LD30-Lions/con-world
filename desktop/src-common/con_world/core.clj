(ns con-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.cell :as cell]))

(declare main-screen con-world)

(defscreen main-screen
  :on-show
  (fn [screen _]
    (update! screen
             :renderer (stage)
             :camera (orthographic)
             :world (box-2d 0 0))
    (cell/create-cell-entity! screen))
  
  :on-render
  (fn [screen entities]
    (clear!)
    (render! screen entities))

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
  (fn [screen entities]
    (height! screen 300)))



(defgame con-world
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
