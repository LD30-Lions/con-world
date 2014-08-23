
~~~clojure
(in-ns 'con-world.core)
(set-screen-wrapper! (fn [screen screen-fn]
                       (try (screen-fn)
                         (catch Exception e
                           (.printStackTrace e)
                           (Thread/sleep 7777)
                           (set-screen! con-world main-screen)))))
                           
(con-world.core.desktop-launcher/-main)

(on-gl (set-screen! con-world main-screen))
~~~
