(ns quo2.components.drawers.drawer-buttons.style
  (:require [quo2.foundations.colors :as colors]))

(def outer-container {:height 216})

(def top-card
  {:flex               1
   :padding-vertical   12
   :padding-horizontal 20
   :border-radius      20
   :background-color   colors/neutral-80})

(def bottom-card
  {:position           :absolute
   :top                80
   :left               0
   :right              0
   :bottom             0
   :padding-vertical   12
   :padding-horizontal 20
   :border-radius      20
   :background-color   (colors/alpha colors/white 0.05)})

(def bottom-container
  {:flex-direction  :row
   :justify-content :space-between})

(def bottom-icon
  {:border-radius   40
   :border-width    1
   :margin-left     24
   :height          28
   :width           28
   :justify-content :center
   :align-items     :center
   :border-color    (colors/alpha colors/white 0.05)})

(def bottom-text
  {:flex  1
   :color (colors/alpha colors/white 0.7)})

(def top-text
  {:color (colors/alpha colors/white 0.7)})

(defn heading-text
  [gap]
  {:color         colors/white
   :margin-bottom gap})
