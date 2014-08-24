
~~~clojure
(in-ns 'con-world.core)
(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn)
                         (catch Exception e
                           (.printStackTrace e)
                           (Thread/sleep 7777)
                           (set-screen! con-world main-screen)))))
                           
(con-world.core.desktop-launcher/-main)

(in-ns 'con-world.core)
(on-gl (set-screen! con-world main-screen))

(defn show-entities []
  (-> main-screen :entities deref clojure.pprint/pprint))
~~~
