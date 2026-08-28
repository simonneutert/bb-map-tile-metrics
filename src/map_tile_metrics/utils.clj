(ns map-tile-metrics.utils
  (:require [cheshire.core :as json]
            [clojure.edn :as edn]
            [clojure.string :as str]))

(defn neighbors
  "Returns the four cardinal neighbors of a tile."
  [{:keys [x y]}]
  [{:x x :y (dec y)}
   {:x (inc x) :y y}
   {:x x :y (inc y)}
   {:x (dec x) :y y}])

(defn all-neighbors
  "Returns all eight surrounding neighbors of a tile."
  [{:keys [x y]}]
  [{:x x :y (dec y)}
   {:x (inc x) :y (dec y)}
   {:x (inc x) :y y}
   {:x (inc x) :y (inc y)}
   {:x x :y (inc y)}
   {:x (dec x) :y (dec y)}
   {:x (dec x) :y y}
   {:x (dec x) :y (inc y)}])

(defn real-neighbors
  "Returns cardinal neighbors of tile that are present in lut.
   lut must support contains?, normally a set of {:x ... :y ...} maps."
  [tile lut]
  (filter #(contains? lut %) (neighbors tile)))

(defn- coordinate
  [tile key]
  (if (contains? tile key)
    (get tile key)
    (get tile (name key))))

(defn- normalize-tile
  "Canonicalizes a tile to the x/y point representation used internally.
   String keys are accepted and extra keys such as z are intentionally ignored."
  [tile]
  {:x (coordinate tile :x)
   :y (coordinate tile :y)})

(defn into-lookup-table
  "Returns input tiles as a canonical x/y lookup set.

   Both keyword and string coordinate keys are accepted. Extra tile metadata,
   including an optional common zoom level, is discarded because all metrics
   operate on x/y coordinates only."
  [data]
  (into #{} (map normalize-tile) data))

(defn read-data-from-file
  "Reads a .json or .edn tile file into a canonical x/y lookup set."
  [filename]
  (let [content (slurp filename)]
    (cond
      (str/ends-with? filename ".json")
      (into-lookup-table (json/parse-string content true))

      (str/ends-with? filename ".edn")
      (into-lookup-table (edn/read-string content))

      :else
      (throw (ex-info "Unsupported file type" {:filename filename})))))
