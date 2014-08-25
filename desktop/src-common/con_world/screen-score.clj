(in-ns 'con-world.core)

(defn find-level [entities]
  (some #(when (:level? %) %) entities))

(defn find-score [entities]
  (some #(when (:score? %) %) entities))

(defscreen score-screen
           :on-show
           (fn [screen _]
             (update! screen :renderer (stage) :camera (orthographic))
             [(assoc (label "vie 1" (color :white))
                :y (- (game :height) 16)
                :score? true)
              (assoc (label "plante 1" (color :white))
                :y (- (game :height) 16) :x 100
                :plante-vie? true)
              (assoc (label "level 1" (color :white))
                :y (- (game :height) 16) :x 200
                :level? true)])


           :on-render
           (fn [screen entities]
             (render! screen entities))

           :update-score
           (fn [{:keys [score]} entities]
             (let [score-label (find-score entities)]
               (replace {score-label (doto score-label (label! :set-text (str "vie " score)))} entities)))

           :update-level
           (fn [{:keys [level]} entities]
             (let [level-label (find-level entities)]
               (replace {level-label (doto level-label (label! :set-text (str "level " level)))} entities)))

           :on-resize
           (fn [screen entities]
             (size! screen (game :width) (game :height))
             entities))