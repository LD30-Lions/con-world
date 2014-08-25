(in-ns 'con-world.core)

(defn stage-fit-vp [camera]
  (stage :set-viewport (FitViewport. u/res-width u/res-height camera)))

(defn init-graphic-settings [screen]
  (let [camera (orthographic)]
    (update! screen
             :camera camera
             :renderer (stage-fit-vp camera))))