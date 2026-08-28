(ns map-tile-metrics.clusters
  (:require [map-tile-metrics.utils :as utils]))

(defn- cluster-tile?
  [tile tiles]
  (every? #(contains? tiles %) (utils/neighbors tile)))

(defn- cluster-lut
  [tiles]
  (into #{} (filter #(cluster-tile? % tiles)) tiles))

(defn- consume-cluster
  "Consumes one connected component from unseen.
   Returns [cluster remaining-unseen]."
  [start unseen]
  (loop [stack [start]
         unseen (disj unseen start)
         cluster #{}]
    (if (empty? stack)
      [cluster unseen]
      (let [tile (peek stack)
            stack (pop stack)
            unseen-neighbors (into []
                                   (filter #(contains? unseen %))
                                   (utils/neighbors tile))]
        (recur (into stack unseen-neighbors)
               (reduce disj unseen unseen-neighbors)
               (conj cluster tile))))))

(defn- calculate-clusters
  [cluster-tiles]
  (loop [unseen cluster-tiles
         result []]
    (if (empty? unseen)
      result
      (let [start (first unseen)
            [cluster unseen] (consume-cluster start unseen)]
        (recur unseen (conj result cluster))))))

(defn clusters
  "Returns all connected components of cluster tiles.

   A cluster tile has visited neighbors on all four cardinal sides."
  [tiles]
  (calculate-clusters (cluster-lut tiles)))

(defn max-clusters
  "Returns all clusters tied for maximum size."
  [clusters]
  (:clusters
   (reduce (fn [{:keys [size] :as result} cluster]
             (let [cluster-size (count cluster)]
               (cond
                 (> cluster-size size)
                 {:size cluster-size :clusters #{cluster}}

                 (= cluster-size size)
                 (update result :clusters conj cluster)

                 :else
                 result)))
           {:size 0 :clusters #{}}
           clusters)))
