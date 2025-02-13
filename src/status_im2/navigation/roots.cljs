(ns status-im2.navigation.roots
  (:require [quo2.foundations.colors :as colors]
            [react-native.platform :as platform]
            [status-im2.navigation.view :as views]
            [status-im2.navigation.state :as state]
            [status-im2.common.theme.core :as utils.theme]))

(defn status-bar-options
  []
  (let [dark-mode? (if (= @state/root-id :shell-stack) (colors/dark?) (utils.theme/dark-mode?))]
    (if platform/android?
      {:navigationBar {:backgroundColor colors/neutral-100}
       :statusBar     {:backgroundColor :transparent
                       :style           (if dark-mode? :light :dark)
                       :drawBehind      true}}
      {:statusBar {:style (if dark-mode? :light :dark)}})))

(defn topbar-options
  []
  {:noBorder             true
   :scrollEdgeAppearance {:active   false
                          :noBorder true}
   :elevation            0
   :title                {:color (colors/theme-colors colors/neutral-100 colors/white)}
   :rightButtonColor     (colors/theme-colors colors/neutral-100 colors/white)
   :background           {:color (colors/theme-colors colors/white colors/neutral-100)}
   :backButton           {:color  (colors/theme-colors colors/neutral-100 colors/white)
                          :testID :back-button}})

(defn default-root
  []
  {:layout {:componentBackgroundColor (colors/theme-colors colors/white colors/neutral-100)
            :orientation              "portrait"
            :backgroundColor          (colors/theme-colors colors/white colors/neutral-100)}})

(defn merge-top-bar
  [root-options options]
  (let [options (:topBar options)]
    {:topBar
     (merge root-options
            options
            (when (or (:title root-options) (:title options))
              {:title (merge (:title root-options) (:title options))})
            (when (or (:background root-options) (:background options))
              {:background (merge (:background root-options) (:background options))})
            (when (or (:backButton root-options) (:backButton options))
              {:backButton (merge (:backButton root-options) (:backButton options))})
            (when (or (:leftButtons root-options) (:leftButtons options))
              {:leftButtons (merge (:leftButtons root-options) (:leftButtons options))})
            (when (or (:rightButtons root-options) (:rightButtons options))
              {:rightButtons (merge (:rightButtons root-options) (:rightButtons options))}))}))

(defn get-screen-options
  [screen]
  (merge (get-in views/screens [screen :options])
         (status-bar-options)
         (merge-top-bar (topbar-options)
                        (get-in views/screens [screen :options]))))

;;TODO problem here is that we have two places for screens, here and in screens ns, and we have handler
;;in navigate
(defn old-roots
  []
  ;;TABS
  {;;INTRO (onboarding carousel)
   :intro
   {:root {:stack {:children [{:component {:name    :intro
                                           :id      :intro
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;; ONBOARDING
   :onboarding
   {:root {:stack {:id       :onboarding
                   :children [{:component {:name    :get-your-keys
                                           :id      :get-your-keys
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (assoc (topbar-options)
                                                    :elevation 0
                                                    :noBorder  true
                                                    :animate   false)})}}}

   ;;PROGRESS
   :progress
   {:root {:stack {:children [{:component {:name    :progress
                                           :id      :progress
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;;LOGIN
   :multiaccounts
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :multiaccounts
                                           :id      :multiaccounts
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :login
                                           :id      :login
                                           :options (get-screen-options :login)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (topbar-options)})}}}

   :multiaccounts-keycard
   {:root {:stack {:id       :multiaccounts-stack
                   :children [{:component {:name    :multiaccounts
                                           :id      :multiaccounts
                                           :options (get-screen-options :multiaccounts)}}
                              {:component {:name    :keycard-login-pin
                                           :id      :keycard-login-pin
                                           :options (get-screen-options :keycard-login-pin)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (topbar-options)})}}}

   ;;WELCOME
   :welcome
   {:root {:stack {:children [{:component {:name    :welcome
                                           :id      :welcome
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;;NOTIFICATIONS
   :onboarding-notification
   {:root {:stack {:children [{:component {:name    :onboarding-notification
                                           :id      :onboarding-notification
                                           :options (status-bar-options)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}

   ;; TERMS OF SERVICE
   :tos
   {:root {:stack {:children [{:component {:name    :force-accept-tos
                                           :id      :force-accept-tos
                                           :options (get-screen-options :force-accept-tos)}}]
                   :options  (merge (default-root)
                                    (status-bar-options)
                                    {:topBar (assoc (topbar-options) :visible false)})}}}})

(defn roots
  []
  ;;TABS
  (merge (old-roots)
         {:shell-stack
          {:root
           {:stack {:id       :shell-stack
                    :children [{:component {:name    :shell-stack
                                            :id      :shell-stack
                                            :options (merge (status-bar-options)
                                                            {:topBar {:visible false}})}}]}}}}))
