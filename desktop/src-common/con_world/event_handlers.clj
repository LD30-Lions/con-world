(ns con-world.event-handlers
  (:require [con-world.state :as state]
            [con-world.entity-player :as ply]
            [con-world.entity-enemy :as enm]
            [con-world.sound :as snd]
            [con-world.screen-score :as score]
            [play-clj.core :refer :all]))

(defn clear-events [_]
  (when (pos? (count @state/events)) (println "clearing " @state/events))
  (reset! state/events []))

(defn add-event [screen event]
  (println "adding" event (nil? screen))
  (swap! state/events conj event))

(defmulti apply-event (fn [_ [type & _]] type))

(defn apply-events [entities]
  (when (pos? (count @state/events)) (println "events" @state/events))
  (reduce (fn [entities evt]
            (let [res (apply-event entities evt)]
              (println "event" evt)
              res))
          entities
          @state/events))

(defmethod apply-event :player-moved [entities [_ direction]]
  (snd/cell-move-sound)
  (let [cell (ply/find-player entities)]
    (replace {cell (ply/move-cell cell direction)}
             entities)))

(defn do-player-win [entities {:keys [level life] :as player} enemy]
  (println "do-player win " (count entities))
  (let [new-life (inc life)
        new-level (or (ply/calculate-level player) level)
        new-level? (not= new-level level)]
    (println "new-life" new-life "new-level" new-level)
    (snd/kill-enemy-sound player)
    (when new-level? (snd/changed-level-sound player))
    (run! score/score-screen :update-level :level new-level)
    (run! score/score-screen :update-score :score new-life)
    (->> entities
         (remove #(= % enemy))
         (replace {player (->> (assoc player :life new-life :level new-level)
                               (ply/update-cell-sprite!))}))))

(defmethod apply-event :player-ate-enemy [entities [_ enemy-id]]
  (println "player ate enemy")
  (do-player-win entities (ply/find-player entities) (enm/find-enemy entities enemy-id)))

(defn do-player-lose [entities {:keys [life level] :as player}]
  (let [new-life (- life 5)
        new-level (ply/calculate-level player)
        new-level? (not= new-level level)]
    (println "new-life" new-life "new-level" new-level)
    (snd/touched-by-enemy-sound)
    (when new-level? (snd/changed-level-sound player))
    (run! score/score-screen :update-score :score new-life)
    (run! score/score-screen :update-level :level new-level)
    (replace {player (->> (assoc player :life new-life :level new-level)
                          (ply/update-cell-sprite!))} entities)))

(defmethod apply-event :player-hurt-by-enemy [entities _]
  (println "player hurt by enemy")
  (do-player-lose entities (ply/find-player entities)))
