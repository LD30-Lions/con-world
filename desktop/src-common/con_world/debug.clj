(in-ns 'con-world.core)

(defn show-entities []
  (-> main-screen :entities deref pprint))
