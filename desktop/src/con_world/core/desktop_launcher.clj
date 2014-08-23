(ns con-world.core.desktop-launcher
  (:require [con-world.core :refer :all])
  (:import [com.badlogic.gdx.backends.lwjgl LwjglApplication]
           [org.lwjgl.input Keyboard])
  (:gen-class))

(defn -main
  []
  (LwjglApplication. con-world "con-world" 800 600)
  (Keyboard/enableRepeatEvents true))
