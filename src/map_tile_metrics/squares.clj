(ns map-tile-metrics.squares
  (:require [map-tile-metrics.utils :as utils]))

(defn- tile-rows
  "Groups visited tile x coordinates by y coordinate."
  [tiles]
  (reduce (fn [rows {:keys [x y]}]
            (assoc rows y (conj (get rows y #{}) x)))
          {}
          tiles))

(defn- reduce-square-sizes
  "Runs maximal-square DP from bottom-right to top-left.

   Calls f with acc, x, y and the largest square size whose top-left
   coordinate is x/y. Only one row of DP state is kept at a time."
  [tiles init f]
  (let [rows (tile-rows tiles)]
    (loop [ys (seq (sort > (keys rows)))
           below-y nil
           below {}
           acc init]
      (if-let [y (first ys)]
        (let [below (if (= below-y (inc y)) below {})
              [current acc]
              (loop [xs (seq (sort > (get rows y)))
                     current {}
                     acc acc]
                (if-let [x (first xs)]
                  (let [size (inc (min (get current (inc x) 0)
                                       (get below x 0)
                                       (get below (inc x) 0)))]
                    (recur (next xs)
                           (assoc current x size)
                           (f acc x y size)))
                  [current acc]))]
          (recur (next ys) y current acc))
        acc))))

(defn- square-sizes-by-coordinate
  [tiles]
  (reduce-square-sizes tiles {}
                       (fn [sizes x y size]
                         (assoc sizes [x y] size))))

(defn- max-square-from-tile
  [tile cluster]
  (assoc tile
         :size
         (get (square-sizes-by-coordinate cluster)
              [(:x tile) (:y tile)]
              0)))

(defn- squares-in-cluster-with-borders
  "Returns each tile with the largest square size starting at that tile."
  [cluster-with-borders]
  (reduce-square-sizes
   cluster-with-borders
   #{}
   (fn [result x y size]
     (conj result {:x x :y y :size size}))))

(defn- add-borders-to-clusters
  "Returns the cluster with the border tiles as a set.

   Kept for compatibility with the existing tests; square calculation no
   longer needs cluster expansion."
  [cluster tiles]
  (let [neighbors (set (mapcat utils/all-neighbors cluster))
        cluster-with-border-tiles (apply conj neighbors cluster)]
    (into #{} (filter #(contains? tiles %) cluster-with-border-tiles))))

(defn- squares
  "Returns the largest square starting at each visited tile whose size is at
   least min-size. The clusters argument is retained for API compatibility."
  [_clusters tiles min-size]
  (reduce-square-sizes
   tiles
   #{}
   (fn [result x y size]
     (if (>= size min-size)
       (conj result {:x x :y y :size size})
       result))))

(defn max-squares
  "Returns a set of the max-squares with the minimum size of 4x4.

   Pass the clusters and all visited tiles. The clusters argument is retained
   for API compatibility; maximal squares are calculated directly from tiles.

   Example:
     clusters: #{#{:x 1 :y 1} #{:x 2 :y 2} ...}
     tiles: #{:x 1 :y 1 :x 2 :y 2 ...}

     (max-squares clusters tiles) => #{{:x 1 :y 1 :size 4}}"
  [_clusters tiles]
  (:squares
   (reduce-square-sizes
    tiles
    {:max-size 0 :squares #{}}
    (fn [{:keys [max-size squares] :as result} x y size]
      (cond
        (< size 4)
        result

        (> size max-size)
        {:max-size size
         :squares #{{:x x :y y :size size}}}

        (= size max-size)
        {:max-size max-size
         :squares (conj squares {:x x :y y :size size})}

        :else
        result)))))
