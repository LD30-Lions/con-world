(ns con-world.screen-main
  (:import (com.badlogic.gdx.physics.box2d Contact))
  (:require   [con-world.entity-enemy :as enm]
              [con-world.entity-player :as ply]
              [con-world.entity-plante :as plt]
              [con-world.entity-wall :as wll]
              [con-world.utils :as u]
              [con-world.physics :as phy]
              [con-world.sound :as snd]
              [con-world.utils-graphics :as gfx]
              [con-world.event-handlers :as evt]
              [con-world.debug :as dbg]
              [play-clj.core :refer :all]
              [play-clj.g2d :refer :all]
              [play-clj.g2d-physics :refer :all]))

(declare con-world main-screen game-over-screen)

(defn game-over [screen]
  (u/transition-screen! screen :gameover)
  (sound! (:music screen) :stop))

(defn on-show [screen _]

  (let [background (u/memo-texture "main-screen-background-1.png")
        background (assoc background
                     :width (u/pixels->world (texture! background :get-region-width))
                     :height (u/pixels->world (texture! background :get-region-height)))
        screen (-> (gfx/init-graphic-settings screen)
                   (update!
                     :world (box-2d 0 0)
                     :music (snd/memo-sound "sound/fond.mp3")
                     :bg-width (:width background)
                     :bg-height (:height background)
                     :last-spawn 0))]

    (add-timer! screen :ambiant-sound 3 3)
    (sound! (:music screen) :loop)

    [background
     (wll/create-uber-wall-entity screen)
     (wll/create-wall-entity screen)
     (plt/create-plante-zone! screen)
     (ply/create-player-entity screen)]))



(defn on-render [{:keys [debug-physics?] :as screen} entities]

  (clear!)

  (let [[screen entities] (enm/may-spawn-enemy screen entities)]
    (when debug-physics?
      (dbg/draw-physics-bodies screen))

    (if (ply/player-dead? entities)
      (game-over screen)
      (let [result-entities (->> entities
                                 (step! screen)
                                 (evt/apply-events)
                                 ply/change-player-level
                                 (ply/animate-player screen)
                                 (plt/animate-plante screen)
                                 (enm/animate-enemies screen)
                                 (map enm/set-enemy-in-zone)
                                 (map enm/move-enemy)
                                 (render! screen))]
        (evt/clear-events screen)
        result-entities))))

(defn on-key-down [{:keys [key debug-physics?] :as screen} entities]
  (when-let [direction (u/key->direction key)]
    (evt/add-event screen [:player-moved direction]))
  (when (= 255 key)
    (update! screen :debug-physics? (not debug-physics?)))
  entities)

(defn on-resize [screen _]
  (size! screen (:bg-width screen) (:bg-height screen)))

(defn on-end-contact [screen entities]
  (let [{:keys [player enemy]} (enm/coliding-entities screen entities)]
    (when (and player enemy)
      (if (ply/player-win? player enemy)
        (evt/add-event screen [:player-ate-enemy (:id enemy)])
        (evt/add-event screen [:player-hurt-by-enemy (:id enemy)])))
    entities))

(defn on-pre-solve [{:keys [^Contact contact] :as screen} entities]
  (let [{:keys [player enemy enemy-1 enemy-2 wall plante-zone]} (enm/coliding-entities screen entities)]
    (when (or (and enemy wall (not (:in-zone? enemy)))
              (and enemy-1 enemy-2 (or (not (:in-zone? enemy-1)) (not (:in-zone? enemy-2)))))
      (.setEnabled contact false))
    (when (and plante-zone player)
      (.setEnabled contact false))
    entities))

(defn on-timer [screen entities]
  (when (= :ambiant-sound (:id screen))
    (when (even? (rand-int 2))
      (snd/ambiant-sound (ply/find-player entities)))
    entities))

(defscreen main-screen
           :on-show on-show
           :on-render on-render
           :on-key-down on-key-down
           :on-resize on-resize
           :on-end-contact on-end-contact
           :on-pre-solve on-pre-solve
           :on-timer on-timer)
