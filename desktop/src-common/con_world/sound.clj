(in-ns 'con-world.core)

(def tonalites {1 [9, 11, 12, 14, 16, 17, 19, 21]
                2 [4, 6, 7, 9, 11, 12, 14, 16]
                3 [11, 13, 14, 16, 18, 19, 21, 23]
                4 [6, 8, 9, 11, 13, 14, 16, 18]
                5 [1, 3, 4, 6, 8, 9, 11, 13]})

(def memo-sound (memoize (fn [name] (sound name))))

(defn play-sound [sound-path]
  (sound! (memo-sound sound-path) :play))

(defn cell-move-sound []
  (play-sound (str "sound/mouvement/" (inc (rand-int 4)) ".wav")))

(defn ambiant-sound [{:keys [level]}]
  (when level (play-sound (str "sound/ambiance/" (nth (tonalites level) (rand-int 8)) ".wav"))))

(defn kill-enemy-sound [{:keys [level]}]
  (let [sound-path (str "sound/bouffe/" level "/" (inc (rand-int 5)) ".wav")]
    (play-sound sound-path)))

(defn touched-by-enemy-sound []
  (let [sound-path (str "sound/ouille/" (inc (rand-int 8)) ".wav")]
      (play-sound sound-path)))

(defn changed-level-sound [{:keys [level]}]
  (let [sound-path (str "sound/changement/" level ".wav")]
      (play-sound sound-path)))

(defn spawn-enemy-sound [{:keys [level]}]
  (let [sound-path (str "sound/apparitions/" (nth (tonalites level) (rand-int 8)) ".wav")]
    (play-sound sound-path)))