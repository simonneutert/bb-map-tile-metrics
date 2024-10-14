(ns map-tile-metrics.clusters
  (:require [map-tile-metrics.utils :as utils]))

(defn- tile-neighbors-nwse? [tile lut]
  (= 4 (count (utils/real-neighbors tile lut))))

(defn- cluster-lut [lut]
  (into #{} (filter #(tile-neighbors-nwse? % lut) lut)))

(defn- remaining-neighbors [tiles lut done]
  (->> (filter #(not (contains? done %)) tiles)
       (into #{})
       (mapcat #(utils/real-neighbors % lut))))

(defn- cluster-for-tile [tile lut init-done]
  (loop [tiles (utils/real-neighbors tile lut)
         cluster #{tile}
         done (conj init-done tile)]
    (if (empty? tiles)
      cluster
      (recur (remaining-neighbors tiles lut done)
             (apply conj cluster tiles)
             (apply conj done cluster)))))

(defn- calculate-clusters [cluster-lut]
  (loop [tiles cluster-lut
         clusters []
         done #{}]
    (if (= done cluster-lut)
      clusters
      (let [tile (first tiles)
            cluster (cluster-for-tile tile cluster-lut done)
            new-done (conj (apply conj done cluster) tile)]
        (recur
         (remove #(contains? new-done %) tiles)
         (conj clusters cluster)
         new-done)))))

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