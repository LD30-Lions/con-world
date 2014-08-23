(ns con-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.cell :as cell]))

(declare main-screen con-world)

(def w-width (* 10 50))

(defscreen main-screen
  :on-show
  (fn [screen _]
    (let [screen (update! screen
                          :camera (orthographic)
                          :renderer (stage)
                          :world (box-2d 0 0))]

      (width! screen w-width)
      [(doto (cell/create-cell-entity! screen)
         (body-position! 30
                         30
                         0)
         (body! :set-linear-velocity 10 10))]))
  
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
        nil)

      :on-resize
        (fn [screen _]
          (width! screen w-width))))

(defgame con-world
  :on-create
  (fn [this]
    (set-screen! this main-screen)))
