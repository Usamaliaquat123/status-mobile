(ns status-im2.contexts.activity-center.notification.mentions.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.gesture :as gesture]
            [status-im2.contexts.activity-center.notification.common.view :as common]
            [status-im2.contexts.activity-center.notification.mentions.style :as style]
            [utils.datetime :as datetime]
            [utils.i18n :as i18n]
            [utils.re-frame :as rf]))

(def tag-params
  {:size           :small
   :override-theme :dark
   :color          colors/primary-50
   :style          style/tag
   :text-style     style/tag-text})

(defn message-body
  [message]
  (let [parsed-text          (get-in message [:content :parsed-text])
        parsed-text-children (:children (first parsed-text))]
    (into [quo/text
           {:number-of-lines     1
            :style               style/tag-text
            :accessibility-label :activity-message-body
            :size                :paragraph-1}]
          (map-indexed (fn [index {:keys [type literal]}]
                         ^{:key index}
                         (case type
                           "mention" [quo/text
                                      {:style style/mention-text
                                       :size  :paragraph-1}
                                      (str "@" (rf/sub [:messages/resolve-mention literal]))]
                           literal))
                       parsed-text-children))))

(defn swipeable
  [{:keys [height active-swipeable notification]} child]
  [common/swipeable
   {:left-button      common/left-swipe-button
    :left-on-press    common/left-swipe-on-press
    :right-button     common/right-swipe-button
    :right-on-press   common/right-swipe-on-press
    :active-swipeable active-swipeable
    :extra-fn         (fn [] {:height @height :notification notification})}
   child])

(defn view
  [{:keys [author chat-name community-id chat-id message read timestamp]}
   set-swipeable-height]
  (let [community-chat? (not (string/blank? community-id))
        community       (rf/sub [:communities/community community-id])
        community-name  (:name community)
        community-image (get-in community [:images :thumbnail :uri])]
    [gesture/touchable-without-feedback
     {:on-press (fn []
                  (rf/dispatch [:hide-popover])
                  (rf/dispatch [:chat/navigate-to-chat chat-id]))}
     [quo/activity-log
      {:title     (i18n/label :t/mention)
       :on-layout set-swipeable-height
       :icon      :i/mention
       :timestamp (datetime/timestamp->relative timestamp)
       :unread?   (not read)
       :context   [[common/user-avatar-tag author]
                   [quo/text {:style style/tag-text} (string/lower-case (i18n/label :t/on))]
                   (if community-chat?
                     [quo/context-tag tag-params {:uri community-image} community-name chat-name]
                     [quo/group-avatar-tag chat-name tag-params])]
       :message   {:body (message-body message)}}]]))
