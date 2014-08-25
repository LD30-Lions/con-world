(in-ns 'con-world.core)

(defscreen game-over-screen
           :on-show
           (fn [screen _]
             (init-graphic-settings screen)
             ; FIX son de Ben
             #_(sound! (u/memo-sound "sound/gameover.wav") :play)
             (label "Game over" (color :red)))

           :on-render
           (fn [screen entities]
             (clear! 0 0 0 1)
             (render! screen entities))

           :on-key-down
           (fn [_ entities]
             (start-main-screens)
             entities)

           :on-key-down
           (fn [_ entities]
             (set-screen! con-world main-screen score-screen)
             entities)

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))