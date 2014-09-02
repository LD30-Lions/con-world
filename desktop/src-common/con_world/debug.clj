(ns con-world.debug
  (:import (com.badlogic.gdx.graphics Camera))
  (:import (com.badlogic.gdx.physics.box2d Box2DDebugRenderer))
  (:require [clojure.pprint :refer [pprint]]))

(declare main-screen)

(def debug-margin 10)

(defn show-entities []
  (-> main-screen :entities deref pprint))

(defn draw-physics-bodies [screen]
  (let [debug-renderer (Box2DDebugRenderer. true true true true true true)
        projection (-> main-screen :screen deref :camera .combined)
        world (:world screen)]
    (.render debug-renderer world projection)))

(defn move-camera [x y]
  (let [camera (-> main-screen :screen deref :camera)]
    (.set (.position camera) x y 0)))
