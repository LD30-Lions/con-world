(in-ns 'con-world.core)

(defn show-entities []
  (-> main-screen :entities deref pprint))

(defn draw-phisics-bodies [screen]
  (let [debug-renderer (Box2DDebugRenderer.)
        projection (-> main-screen :screen deref :camera .combined)
        world (:world screen)]
    (.render debug-renderer world projection)))
