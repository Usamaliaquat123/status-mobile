(ns status-im2.contexts.communities.discover.view
  (:require [utils.i18n :as i18n]
            [oops.core :as oops] ;; TODO move to status-im2
            [quo2.core :as quo]
            [quo2.foundations.colors :as colors]
            [react-native.core :as rn]
            [reagent.core :as reagent]
            [status-im2.common.resources :as resources]
            [status-im2.contexts.communities.menus.community-options.view :as options]
            [status-im.ui.screens.communities.community :as community]
            [status-im.ui.components.react :as react]
            [react-native.platform :as platform]
            [status-im2.common.scroll-page.view :as scroll-page]
            [status-im2.contexts.communities.discover.style :as style]
            [utils.re-frame :as rf]))

(def mock-community-item-data ;; TODO: remove once communities are loaded with this data.
  {:data {:community-color "#0052FF"
          :status          :gated
          :locked?         true
          :cover           (resources/get-mock-image :community-cover)
          :tokens          [{:id    1
                             :group [{:id         1
                                      :token-icon (resources/get-mock-image :status-logo)}]}]
          :tags            [{:id        1
                             :tag-label (i18n/label :t/music)
                             :resource  (resources/get-image :music)}
                            {:id        2
                             :tag-label (i18n/label :t/lifestyle)
                             :resource  (resources/get-image :lifestyle)}
                            {:id        3
                             :tag-label (i18n/label :t/podcasts)
                             :resource  (resources/get-image :podcasts)}]}})

(defn render-fn
  [community-item _ _ {:keys [width view-type]}]
  (let [item (merge community-item
                    (get mock-community-item-data :data))]
    (if (= view-type :card-view)
      [quo/community-card-view-item (assoc item :width width)
       #(rf/dispatch [:navigate-to :community-overview (:id item)])]
      [quo/communities-list-view-item
       {:on-press      (fn []
                         (rf/dispatch [:communities/load-category-states (:id item)])
                         (rf/dispatch [:dismiss-keyboard])
                         (rf/dispatch [:navigate-to :community {:community-id (:id item)}]))
        :on-long-press #(rf/dispatch
                         [:bottom-sheet/show-sheet
                          {:content (fn []
                                      [options/community-options-bottom-sheet (:id item)])}])}])))

(defn screen-title
  []
  [rn/view
   {:style style/screen-title-container}
   [quo/text
    {:accessibility-label :communities-screen-title
     :weight              :semi-bold
     :size                :heading-1}
    (i18n/label :t/discover-communities)]])

(defn featured-communities-header
  [communities-count]
  [rn/view
   {:style style/featured-communities-header}
   [rn/view
    {:style style/featured-communities-title-container}
    [quo/text
     {:accessibility-label :featured-communities-title
      :weight              :semi-bold
      :size                :paragraph-1
      :style               {:margin-right 6}}
     (i18n/label :t/featured)]
    [quo/counter {:type :grey} communities-count]]
   [quo/icon :i/info
    {:container-style style/communities-header-container
     :resize-mode     :center
     :size            20
     :color           (colors/theme-colors
                       colors/neutral-50
                       colors/neutral-40)}]])

(defn discover-communities-segments
  [selected-tab fixed]
  [rn/view
   {:style (style/discover-communities-segments fixed)}
   [quo/tabs
    {:size           32
     :on-change      #(reset! selected-tab %)
     :default-active :all
     :data           [{:id                  :all
                       :label               (i18n/label :t/all)
                       :accessibility-label :all-communities-tab}
                      {:id                  :open
                       :label               (i18n/label :t/open)
                       :accessibility-label :open-communities-tab}
                      {:id                  :gated
                       :label               (i18n/label :t/gated)
                       :accessibility-label :gated-communities-tab}]}]])


(defn featured-list
  [communities view-type]
  (let [view-size (reagent/atom 0)]
    (fn []
      [rn/view
       {:style     style/featured-list-container
        :on-layout #(swap! view-size
                      (fn []
                        (- (oops/oget % "nativeEvent.layout.width") 20)))}
       (when-not (= @view-size 0)
         [rn/flat-list
          {:key-fn                            :id
           :horizontal                        true
           :keyboard-should-persist-taps      :always
           :shows-horizontal-scroll-indicator false
           :separator                         [rn/view {:width 12}]
           :data                              communities
           :render-fn                         render-fn
           :render-data                       {:width     @view-size
                                               :view-type view-type}}])])))

(defn discover-communities-header
  [{:keys [featured-communities-count
           featured-communities
           view-type
           selected-tab]}]
  [react/animated-view
   [screen-title]
   [featured-communities-header featured-communities-count]
   [featured-list featured-communities view-type]
   [quo/separator]
   [discover-communities-segments selected-tab false]])

(defn other-communities-list
  [{:keys [communities communities-ids view-type]}]
  [rn/view {:flex 1}
   (map-indexed
    (fn [inner-index item]
      (let [community-id (when communities-ids item)
            community    (if communities
                           item
                           [rf/sub [:communities/home-item community-id]])]
        [rn/view
         {:key           (str inner-index (:id community))
          :margin-bottom 16}
         (if (= view-type :card-view)
           [quo/community-card-view-item
            (merge community
                   (get mock-community-item-data :data))
            #(rf/dispatch [:navigate-to :community-overview (:id community)])]
           [quo/communities-list-view-item
            {:on-press      (fn []
                              (rf/dispatch [:communities/load-category-states (:id community)])
                              (rf/dispatch [:dismiss-keyboard])
                              (rf/dispatch [:navigate-to :community (:id community)]))
             :on-long-press #(rf/dispatch [:bottom-sheet/show-sheet
                                           {:content (fn []
                                                       ;; TODO implement with quo2
                                                       [community/community-actions community])}])}
            (merge community
                   (get mock-community-item-data :data))])]))
    (if communities communities communities-ids))])


(defn communities-lists
  [selected-tab view-type]
  (let [ids-by-user-involvement (rf/sub [:communities/community-ids-by-user-involvement])
        all-communities         (rf/sub [:communities/sorted-communities])
        tab                     @selected-tab]
    [rn/view {:style {:flex 1}}
     (case tab
       :all
       (other-communities-list {:communities all-communities
                                :view-type   view-type})

       :open
       (other-communities-list {:communities-ids (:open ids-by-user-involvement)
                                :view-type       view-type})

       :gated
       (other-communities-list {:communities-ids (:gated ids-by-user-involvement)
                                :view-type       view-type})

       [quo/information-box
        {:type :error
         :icon :i/info}
        (i18n/label :t/error)])]))


(defn render-communities
  [selected-tab
   featured-communities-count
   featured-communities
   view-type]
  (fn []
    [rn/view
     {:style style/render-communities-container}
     [discover-communities-header
      {:selected-tab               selected-tab
       :view-type                  view-type
       :featured-communities-count featured-communities-count
       :featured-communities       featured-communities}]
     [communities-lists selected-tab view-type]]))

(defn render-sticky-header
  [{:keys [selected-tab scroll-height]}]
  (fn []
    (when (> @scroll-height 360)
      [rn/view
       {:style (style/blur-tabs-header)}
       [discover-communities-segments selected-tab true]])))

(defn discover-screen-content
  [featured-communities]
  (let [view-type                  (reagent/atom :card-view)
        selected-tab               (reagent/atom :all)
        scroll-height              (reagent/atom 0)
        featured-communities-count (count featured-communities)]
    (fn []
      [scroll-page/scroll-page
       {:name             (i18n/label :t/discover-communities)
        :on-scroll        #(reset! scroll-height %)
        :background-color (colors/theme-colors
                           colors/white
                           colors/neutral-95)
        :navigate-back?   :true
        :height           (if platform/ios?
                            (if (> @scroll-height 360)
                              156
                              100)
                            (if (> @scroll-height 360)
                              208
                              148))}
       [render-sticky-header
        {:selected-tab  selected-tab
         :scroll-height scroll-height}]
       [render-communities
        selected-tab
        featured-communities-count
        featured-communities
        @view-type]])))

(defn discover
  []
  (let [featured-communities (rf/sub [:communities/featured-communities])]
    [rn/view
     {:style (style/discover-screen-container (colors/theme-colors
                                               colors/white
                                               colors/neutral-95))}
     [discover-screen-content featured-communities]]))
