(ns status-im2.contexts.communities.menus.request-to-join.view
  (:require [react-native.core :as rn]
            [react-native.safe-area :as safe-area]
            [status-im2.contexts.communities.menus.community-rules-list.view :as community-rules]
            [reagent.core :as reagent]
            [status-im2.contexts.communities.menus.request-to-join.style :as style]
            [quo2.core :as quo]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]
            [utils.requests :as requests]))

(defn request-to-join-text
  [is-open?]
  (if is-open?
    (i18n/label :t/join-open-community)
    (i18n/label :t/request-to-join)))

(defn request-to-join
  [{:keys [permissions
           name
           id
           images
           can-join?
           can-request-access?
           requested-to-join-at]}]
  (let [agreed-to-rules? (reagent/atom false)]
    [safe-area/consumer
     (fn [insets]
       [:f>
        (fn []
          (let [{window-height :height} (rn/use-window-dimensions)
                is-open?                (not= 3 (:access permissions))]
            [rn/scroll-view {:style {:max-height (- window-height (:top insets))}}
             [rn/view style/page-container
              [rn/view
               {:style style/title-container}
               [quo/text
                {:accessibility-label :communities-join-community
                 :weight              :semi-bold
                 :size                :heading-1}
                (request-to-join-text is-open?)]
               [rn/view
                {:style style/request-icon}
                [quo/icon :i/info]]]
              [quo/context-tag
               {:style
                {:margin-right :auto
                 :margin-top   8}}
               (:thumbnail images) name]
              [quo/text
               {:style               {:margin-top 24}
                :accessibility-label :communities-rules-title
                :weight              :semi-bold
                :size                :paragraph-1}
               (i18n/label :t/community-rules)]
              [community-rules/view community-rules/rules]
              [quo/disclaimer
               {:accessibility-label :rules-disclaimer-checkbox
                :container-style     {:margin-top 20}
                :on-change           #(swap! agreed-to-rules? not)}
               (i18n/label :t/accept-community-rules)]
              [rn/view {:style (style/bottom-container insets)}
               [quo/button
                {:accessibility-label :cancel
                 :on-press            #(rf/dispatch [:bottom-sheet/hide])
                 :type                :grey
                 :style               style/cancel-button} (i18n/label :t/cancel)]
               [quo/button
                {:accessibility-label :join-community-button
                 :on-press            (fn []
                                        (if can-join?
                                          (do
                                            (rf/dispatch [:communities/join id])
                                            (rf/dispatch [:bottom-sheet/hide]))
                                          (do (and can-request-access?
                                                   (not (pos? requested-to-join-at))
                                                   (requests/can-request-access-again?
                                                    requested-to-join-at))
                                              (rf/dispatch [:communities/request-to-join id])
                                              (rf/dispatch [:bottom-sheet/hide]))))
                 :disabled            (not @agreed-to-rules?)
                 :style               {:flex 1}} (request-to-join-text is-open?)]]]]))])]))
