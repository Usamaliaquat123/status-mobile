(ns status-im2.contexts.chat.home.chat-list-item.view
  (:require [clojure.string :as string]
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [utils.datetime :as datetime]
            [status-im2.common.home.actions.view :as actions] ;;TODO move to status-im2
            [status-im2.contexts.chat.home.chat-list-item.style :as style]
            [utils.re-frame :as rf]))

(def max-subheader-length 50)

(defn open-chat
  [chat-id]
  (fn []
    (rf/dispatch [:dismiss-keyboard])
    (rf/dispatch [:chat/navigate-to-chat chat-id])
    (rf/dispatch [:search/home-filter-changed nil])))

(defn truncate-literal
  [literal]
  (when literal
    (let [size (min max-subheader-length (.-length literal))]
      {:components (.substring literal 0 size)
       :length     size})))

(defn add-parsed-to-subheader
  [acc {:keys [type destination literal children]}]
  (let [result (case type
                 "paragraph"
                 (reduce
                  (fn [{:keys [_ length] :as acc-paragraph} parsed-child]
                    (if (>= length max-subheader-length)
                      (reduced acc-paragraph)
                      (add-parsed-to-subheader acc-paragraph parsed-child)))
                  {:components [quo/text]
                   :length     0}
                  children)

                 "mention"
                 {:components [quo/text (rf/sub [:messages/resolve-mention literal])]
                  :length     4} ;; we can't predict name length so take the
                                 ;; smallest possible

                 "status-tag"
                 (truncate-literal (str "#" literal))

                 "link"
                 (truncate-literal destination)

                 (truncate-literal literal))]
    {:components (conj (:components acc) (:components result))
     :length     (+ (:length acc) (:length result))}))

(defn render-subheader
  "Render the preview of a last message to a maximum of max-subheader-length characters"
  [parsed-text]
  (let [result
        (reduce
         (fn [{:keys [_ length] :as acc-text} new-text-chunk]
           (if (>= length max-subheader-length)
             (reduced acc-text)
             (add-parsed-to-subheader acc-text new-text-chunk)))
         {:components [quo/text
                       {:size                :paragraph-2
                        :style               {:color (colors/theme-colors colors/neutral-50
                                                                          colors/neutral-40)
                                              :width "90%"}
                        :number-of-lines     1
                        :ellipsize-mode      :tail
                        :accessibility-label :chat-message-text}]
          :length     0}
         parsed-text)]
    (:components result)))

(defn verified-or-contact-icon
  [{:keys [ens-verified added?]}]
  (if ens-verified
    [rn/view {:style {:margin-left 5 :margin-top 4}}
     [quo/icon :i/verified
      {:no-color true
       :size     12
       :color    (colors/theme-colors colors/success-50 colors/success-60)}]]
    (when added?
      [rn/view {:style {:margin-left 5 :margin-top 4}}
       [quo/icon :i/contact
        {:no-color true
         :size     12
         :color    (colors/theme-colors colors/primary-50 colors/primary-60)}]])))

(defn name-view
  [display-name contact timestamp]
  [rn/view {:style {:flex-direction :row}}
   [quo/text
    {:weight              :semi-bold
     :accessibility-label :chat-name-text}
    display-name]
   [verified-or-contact-icon contact]
   [quo/text
    {:size  :label
     :style (style/timestamp)}
    (datetime/to-short-str timestamp)]])

(defn avatar-view
  [{:keys [contact chat-id full-name color]}]
  (if contact ; `contact` is passed when it's not a group chat
    (let [online?    (rf/sub [:visibility-status-updates/online? chat-id])
          photo-path (rf/sub [:chats/photo-path chat-id])
          image-key  (if (seq (:images contact)) :profile-picture :ring-background)]
      [quo/user-avatar
       {:full-name full-name
        :size      :small
        :online?   online?
        image-key  photo-path}])
    [quo/group-avatar
     {:color color
      :size  :medium}]))

(defn chat-list-item
  [{:keys [chat-id group-chat color name unviewed-messages-count unviewed-mentions-count
           timestamp last-message]
    :as   item}]
  (let [display-name (if group-chat
                       name
                       (first (rf/sub [:contacts/contact-two-names-by-identity chat-id])))
        contact      (when-not group-chat
                       (rf/sub [:contacts/contact-by-address chat-id]))]
    [rn/touchable-opacity
     {:style         (style/container)
      :on-press      (open-chat chat-id)
      :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                    {:content (fn [] [actions/chat-actions item false])}])}
     [avatar-view
      {:contact   contact
       :chat-id   chat-id
       :full-name display-name
       :color     color}]
     [rn/view {:style {:margin-left 8}}
      [name-view display-name contact timestamp]
      (if (string/blank? (get-in last-message [:content :parsed-text]))
        [quo/text
         {:size  :paragraph-2
          :style {:color (colors/theme-colors colors/neutral-50 colors/neutral-40)}}
         (get-in last-message [:content :text])]
        [render-subheader (get-in last-message [:content :parsed-text])])]
     (if (> unviewed-mentions-count 0)
       [quo/info-count {:style {:top 16}}
        unviewed-mentions-count]
       (when (> unviewed-messages-count 0)
         [rn/view {:style (style/count-container)}]))]))
