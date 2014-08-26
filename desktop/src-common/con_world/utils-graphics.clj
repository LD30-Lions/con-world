(in-ns 'con-world.core)

(defn stage-fit-vp [camera]
  (stage :set-viewport (FitViewport. u/res-width u/res-height camera)))

(defn init-graphic-settings [screen]
  (let [camera (orthographic)]
    (update! screen
             :camera camera
             :renderer (stage-fit-vp camera))))

(defn image->sprite [image sprite-w sprite-h nb-sprite]
  (let [sheet (texture image)
        tiles (texture! sheet :split sprite-w sprite-h)]
    (for [col (range nb-sprite)]
      (texture (aget tiles 0 col)))))