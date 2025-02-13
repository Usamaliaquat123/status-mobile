(ns status-im.search.core
  (:require [utils.re-frame :as rf]))

(rf/defn home-filter-changed
  {:events [:search/home-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :home-filter] search-filter)})

(rf/defn currency-filter-changed
  {:events [:search/currency-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :currency-filter] search-filter)})

(rf/defn token-filter-changed
  {:events [:search/token-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :token-filter] search-filter)})

(rf/defn recipient-filter-changed
  {:events [:search/recipient-filter-changed]}
  [cofx search-filter]
  {:db (assoc-in (:db cofx) [:ui/search :recipient-filter] search-filter)})
