(in-ns 'con-world.core)

(def debug-margin 10)

(defn show-entities []
  (-> main-screen :entities deref pprint))

(defn draw-physics-bodies [screen]
  (let [debug-renderer (Box2DDebugRenderer. true true true true true true)
        projection (-> main-screen :screen deref :camera .combined)
        world (:world screen)]
    (.render debug-renderer world projection)))
