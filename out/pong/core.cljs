(ns pong.core
  (:require [quil.core :as q :include-macros true]
            [quil.middleware :as m]
            [pong.setup :as pong]
            [pong.input :as input]
            [pong.bot :as bot]
            [pong.physics :as physics]
            [pong.fairydust :as fairydust]))

(defn draw-rect [r]
  (apply q/fill (:color r))
  (q/rect (:x r) (:y r) (:w r) (:h r)))

(defn draw-str [s point]
  (q/text s (:x point) (:y point)))

(defn next-ball [state ball [dx dy]]
  (let [hits (:hits (:player state))
        vel-bump (+ (/ hits 5) 1)] ; speed up 5 times slower than usual
        (assoc ball :x (+ (:x ball) (* dx vel-bump))
                    :y (+ (:y ball) dy))))

(defn update-fn [state]
  (cond
    (= (:game state) :over)
    state
    :else
  (-> state
    ; move the ball
    (assoc-in  [:ball] (next-ball state (:ball state) (:ball-dir state)))
    ; bot paddle move
    (bot/move)
    ; collide the ball (if any)
    (physics/collision)
    ; make things interesting
    (fairydust/sprinkle)
    )))

; draw
(defn draw [state]
  ; set background color to dark gray, draw color to white
  (q/background-float 0x20)
  (q/fill 0xff)
  (if (= (:game state) :over)
    (draw-str "GAME _VER" {:x (/ pong/court-width 2) :y (/ pong/court-height 2)})
    (do
      (doseq [wall (:walls state)] (draw-rect wall))
      (draw-rect (:paddle (:bot state)))
      (draw-rect (:paddle (:player state)))
      (draw-rect (:ball state))
      (draw-str (:hits (:bot state)) (:score-pos (:bot state)))
      (draw-str (:hits (:player state)) (:score-pos (:player state))))))

; run
(q/defsketch pong
  :host "pong"
  :size [pong/court-width pong/court-height]
  :setup pong/setup
  :update update-fn
  :draw draw
  ;:key-pressed input/key-pressed
  :mouse-moved input/mouse
  :middleware [m/fun-mode])
