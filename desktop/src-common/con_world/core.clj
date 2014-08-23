(ns con-world.core
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.ui :refer :all]
            [play-clj.g2d-physics :refer :all]
            [con-world.cell :as cell]))

(declare main-screen con-world)

(def tile-width 50)
(def w-width (* 10 tile-width))
(def w-height (* 30 tile-width))

(defn create-rect-body!
  [screen width height]
  (let [body (add-body! screen (body-def :static))]
    (->> [0 0
          0 height
          width height
          width 0
          0 0]
         float-array
         (chain-shape :create-chain)
         (fixture-def :density 1 :shape)
         (body! body :create-fixture))
    body))

(defscreen main-screen
           :on-show
           (fn [screen _]
             (let [screen (update! screen
                                   :camera (orthographic)
                                   :renderer (stage)
                                   :world (box-2d 0 0))
                   wall (doto {:body (create-rect-body! screen w-width w-height)}
                          (body-position! 0 0 0))]

               (size! screen w-width w-height)
               [wall
                (doto (cell/create-cell-entity! screen)
                  (body-position! 30
                                  30
                                  0)
                  (body! :set-linear-velocity 100 100))]))

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
