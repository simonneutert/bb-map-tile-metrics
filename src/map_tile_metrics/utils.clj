(ns map-tile-metrics.utils
  (:require [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.edn :as edn]))

(defn neighbors [tile]
  (for [[x y] [[0 -1] [1 0] [0 1] [-1 0]]]
    {:x (+ x (:x tile))
     :y (+ y (:y tile))}))

(defn all-neighbors [tile]
  (for [[x y] [[0 -1] [1 -1] [1 0] [1 1] [0 1] [-1 -1] [-1 0] [-1 1]]]
    {:x (+ x (:x tile))
     :y (+ y (:y tile))}))

(defn real-neighbors
  "lut should be a set, due to `contains?`"
  [tile lut]
  (let [neighbors (neighbors tile)]
    (filter #(contains? lut %) neighbors)))

(defn read-data-from-file
  "Returns a set of the tiles in the given file.
   
   #{{:x 1 :y 1} {:x 2 :y 1} ...}
   "
  [filename]
  (let [content (slurp filename)]
    (cond
      (str/ends-with? filename ".json") (into #{} (json/parse-string content true))
      (str/ends-with? filename ".edn") (into #{} (pmap clojure.walk/keywordize-keys (edn/read-string content)))
      :else
      (throw (ex-info "Unsupported file type" {:filename filename})))))

(defn into-lookup-table [data] (into #{} data))