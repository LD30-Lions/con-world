(ns con-world.core
  (:import (com.badlogic.gdx.physics.box2d Contact WorldManifold)
           (com.badlogic.gdx.math Vector2))
  (:require [play-clj.core :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
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
                   wall (doto {:body  (cell/create-rect-body! screen u/w-width u/z-height)
                               :wall? true}
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
             (size! screen u/w-width u/w-height))

           :on-pre-solve
           (fn [{:keys [^Contact contact] :as screen} entities]
             (let [coliding-entities [(first-entity screen entities) (second-entity screen entities)]
                   {:keys [z-side] :as enemy} (first (filter :enemy? coliding-entities))
                   wall (first (filter :wall? coliding-entities))

                   normal (-> contact
                              (.getWorldManifold)
                              (.getNormal))
                   disable-contact (or
                                     (and (= z-side :right) (= normal (vector-2 1.0 0.0)))
                                     (and (= z-side :bottom) (= normal (vector-2 0.0 -1.0)))
                                     (and (= z-side :left) (= normal (vector-2 -1.0 0.0))))]
               (if (and enemy wall disable-contact disable-contact)
                 (do (.setEnabled contact false)
                     (replace {enemy (assoc enemy :in-zone? true)} entities))
                 entities))))

(defgame con-world
         :on-create
         (fn [this]
           (set-screen! this main-screen)))
