(ns map-tile-metrics.clusters
  (:require [map-tile-metrics.utils :as utils]))

(defn- tile-neighbors-nwse? [tile lut]
  (= 4 (count (utils/real-neighbors tile lut))))

(defn- cluster-lut [lut]
  (into #{} (filter #(tile-neighbors-nwse? % lut) lut)))

(defn- consume-cluster
  "Consumes the connected component starting at tile from unseen.
   Returns [cluster remaining-unseen]."
  [tile unseen]
  (loop [stack [tile]
         unseen (disj unseen tile)
         cluster #{}]
    (if (empty? stack)
      [cluster unseen]
      (let [current (peek stack)
            stack (pop stack)
            neighbors (into []
                            (filter #(contains? unseen %))
                            (utils/neighbors current))
            unseen (reduce disj unseen neighbors)]
        (recur (into stack neighbors)
               unseen
               (conj cluster current))))))

(defn- cluster-for-tile [tile lut init-done]
  (let [unseen (reduce disj lut init-done)]
    (first (consume-cluster tile unseen))))

(defn- calculate-clusters [cluster-lut]
  (loop [unseen cluster-lut
         clusters []]
    (if (empty? unseen)
      clusters
      (let [tile (first unseen)
            [cluster unseen] (consume-cluster tile unseen)]
        (recur unseen (conj clusters cluster))))))

(defn clusters
  "Returns all clusters of the given tiles"
  [tiles]
  (calculate-clusters (cluster-lut tiles)))

(defn max-clusters
  "Returns all clusters of the maximum size

   Example:
     Clusters: #{ #{:x 2 :y 2, :x 3 :y 3} ...}
     Tiles: #{:x 1 :y 1, :x 2 :y 2, :x 3 :y 3 ...}
     (max-clusters clusters) => #{ #{:x 2 :y 2, :x 3 :y 3} ...}"
  [clusters]
  (if (empty? clusters)
    #{}
    (let [max-size (apply max (map count clusters))]
      (into #{} (filter #(= max-size (count %)) clusters)))))
