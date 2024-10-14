(ns map-tile-metrics.squares
  (:require [map-tile-metrics.utils :as utils]))

(defn- every-tile-of-grid-in-cluster?
  "Returns true if every tile in a square grid is in the cluster."
  [grid cluster]
  (every? #(contains? cluster %) grid))

(defn- build-grid-square [tile steps]
  (for [x (range (:x tile) (+ (:x tile) (inc steps)))
        y (range (:y tile) (+ (:y tile) (inc steps)))]
    {:x x :y y}))

(defn- max-square-from-tile
  [tile cluster]
  (loop [step 0]
    (if (every-tile-of-grid-in-cluster? (build-grid-square tile step) cluster)
      (recur (inc step))
      (assoc tile :size step))))

(defn- squares-in-cluster-with-borders
  "Returns tiles in the cluster with the border tiles and the most steps until the border was reached in the given cluster."
  [cluster-with-borders]
  (into #{} (pmap #(max-square-from-tile % cluster-with-borders) cluster-with-borders)))

(defn- add-borders-to-clusters
  "Returns the cluster with the border tiles as a set."
  [cluster tiles]
  (let [neighbors (set (mapcat utils/all-neighbors cluster))
        cluster-with-border-tiles (apply conj neighbors cluster)]
    (into #{} (filter #(contains? tiles %) cluster-with-border-tiles))))

(defn- squares [clusters tiles min-size]
  (let [clusters-with-borders (pmap #(add-borders-to-clusters % tiles) (filter #(>= (count %) 2) clusters))]
    (into #{} (filter #(>= (:size %) min-size) (mapcat squares-in-cluster-with-borders clusters-with-borders)))))

(defn max-squares
  "Returns a set of the max-squares with the minimum size of 4x4.
   
   Pass the clusters and all visited tiles.

   Example:
     clusters: #{#{:x 1 :y 1} #{:x 2 :y 2} ...}
     tiles: #{:x 1 :y 1 :x 2 :y 2 ...}
   
     (max-squares clusters tiles) => #{:x 1 :y 1 :size 4}"
  [clusters tiles]
  (let [min-square-size 4
        squares (squares clusters tiles min-square-size)
        max-square-size (try (apply max (pmap :size squares))
                             (catch Exception e 0))
        max-squares (filter #(= (:size %) max-square-size) squares)]
    (set max-squares)))