(ns con-world.cell
  (:import (com.badlogic.gdx.physics.box2d Filter)
           (com.badlogic.gdx.math Vector2))
  (:require [play-clj.g2d-physics :refer :all]
            [play-clj.g2d :refer :all]
            [play-clj.math :refer :all]
            [play-clj.core :refer [sound!] :as pcore]
            [con-world.utils :as u]))






(defn find-cell [entities]
  (some #(when (:cell? %) %) entities))

(defn find-enemy [entities]
  (some #(when (:enemy? %) %) entities))

(defn find-wall [entities]
  (some #(when (:wall? %) %) entities))

(defn find-plante-zone [entities]
  (some #(when (:plante-zone? %) %) entities))



(defn create-enemy-body!
  [screen radius]
  (let [body (add-body! screen (body-def :dynamic :linear-damping 3))]
    (->> (circle-shape radius)
         (fixture-def :density 1 :friction 0 :restitution 0.7 :shape)
         (body! body :create-fixture))
    body))

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

(def directions [:right :bottom :left])

(def tonalites {1 [9, 11, 12, 14, 16, 17, 19, 21]
                2 [4, 6, 7, 9, 11, 12, 14, 16]
                3 [11, 13, 14, 16, 18, 19, 21, 23]
                4 [6, 8, 9, 11, 13, 14, 16, 18]
                5 [1, 3, 4, 6, 8, 9, 11, 13]})

(defn play-sound [sound-path]
  (pcore/sound! (u/memo-sound sound-path) :play))

(defn cell-move-sound []
  (play-sound (str "sound/mouvement/" (inc (rand-int 4)) ".wav")))

(defn ambiant-sound [{:keys [level]}]
  (println "son ambiance " (str "sound/ambiance/" (nth (tonalites level) (rand-int 8)) ".wav"))
  (when level (play-sound (str "sound/ambiance/" (nth (tonalites level) (rand-int 8)) ".wav"))))

(defn spawn-enemy-sound [{:keys [level]}]
  (let [sound-path (str "sound/apparitions/" (nth (tonalites level) (rand-int 8)) ".wav")]
    (play-sound sound-path)))

(defn kill-enemy-sound [{:keys [level]}]
  (let [sound-path (str "sound/bouffe/" level "/" (inc (rand-int 5)) ".wav")]
    (play-sound sound-path)))

(defn touched-by-enemy-sound []
  (let [sound-path (str "sound/ouille/" (inc (rand-int 8)) ".wav")]
      (play-sound sound-path)))

(defn changed-level-sound [{:keys [level]}]
  (let [sound-path (str "sound/changement/" level ".wav")]
      (play-sound sound-path)))

(def enemy-index
  {1 [25 3]
   2 [50 3]
   3 [75 3]
   4 [105 3]
   5 [135 3]
   6 [170 3]})

(defn spawn-enemy [screen entities]
  (let [{:keys [level]} (find-cell entities)
         index (+ level 1 (u/rand-sign 1))
        sheet (u/memo-texture (str "entities/ennemi" index ".png"))
        tiles (texture! sheet :split (first (enemy-index index)) (first (enemy-index index)))
        en-images (for [col (range (second (enemy-index index)))]
                    (texture (aget tiles 0 col)))
        stand (first en-images)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))
        x-max (- u/z-width width)
        y-max (- u/z-height height)
        z-side (directions (rand-int 3))
        [x y x-velocity y-velocity]
        (condp = z-side
          :right [(+ x-max) (rand-int y-max) (- u/cell-x-velocity) 0]
          :bottom [(rand-int x-max) 0 0 u/cell-y-velocity]
          :left [0 (rand-int y-max) u/cell-x-velocity 0])]
    (println x y x-velocity y-velocity z-side)
    (doto
        (assoc stand
          :stand stand
          :walk (animation 0.1 en-images :set-play-mode (play-mode :loop-pingpong))
          :body (create-enemy-body! screen (/ width 2))
          :width width :height height
          :enemy? true
          :z-side z-side)
      (body-position! x y 0)
      (body! :set-linear-velocity x-velocity y-velocity))))

(defn change-cell-level [entities]
  (let [cell (find-cell entities)]
    (replace {cell (merge cell
                          (u/memo-texture (str "cell" (:level cell) ".png")))}
             entities)))

(defn create-plante-zone!
  [screen]
  (let [sheet (texture "entities/racines.png")
        tiles (texture! sheet :split 130 130)
        plante-images (for [col [0 1 2 3]]
                        (texture (aget tiles 0 col)))
        stand (first plante-images)
        width (u/pixels->world (texture! stand :get-region-width))
        height (u/pixels->world (texture! stand :get-region-height))]
    (assoc stand
      :stand stand
      :move (animation 0.1 plante-images :set-play-mode (play-mode :loop-pingpong))
      :body (doto (create-rect-body! screen width height)
              (body-position! (- (/ u/z-width 2) (/ width 2)) (- u/z-height height) 0))
      :width width :height height
      :plante-zone? true)))

(defn calculate-level [{:keys [life]}]
  (let [mapping [[5 1], [10 2], [15 3], [20 4], [25 5]]]
    (some (fn [[map-life map-level]]
            (when (< life map-life) map-level))
          mapping)))

(defn may-spawn-enemy [{:keys [last-spawn total-time] :as screen} entities]
  (if (and (not= last-spawn (int total-time))
           (= 0 (mod (int total-time) 2)))
    (do
      (println "spawn!")
      (spawn-enemy-sound (find-cell entities))
      [(play-clj.core/update! screen :last-spawn (int total-time)) (conj entities (spawn-enemy screen entities))])
    [screen entities]))

(defn animate-cell [screen entities]
  (let [player (find-cell entities)
        velocity (body! player :get-linear-velocity)
        not-moving (vector-2! velocity :is-zero 1)]
    (if not-moving
      (replace {player (merge player (:stand player))} entities)
      (replace {player (merge player (animation->texture screen (:walk player)))} entities))))

(defn animate-enemies [screen entities]
  (map (fn [entity]
         (if (:enemy? entity)
           (let [velocity (body! entity :get-linear-velocity)
                 not-moving (vector-2! velocity :is-zero 1)]
             (if not-moving
               (merge entity (:stand entity))
               (merge entity (animation->texture screen (:walk entity)))))
           entity))
    entities))

(defn animate-plante [screen entities]
  (let [plante (find-plante-zone entities)]
    (replace {plante (merge plante (animation->texture screen (:move plante)))} entities)))
