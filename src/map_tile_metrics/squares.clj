(ns map-tile-metrics.squares)

(def ^:private min-square-size 4)
(def ^:private min-inner-size (- min-square-size 2))

(defn- required-cluster-size
  "Minimum cluster tile count needed to contain a square of square-size."
  [square-size]
  (let [inner-size (- square-size 2)]
    (* inner-size inner-size)))

(defn- tile-rows
  "Groups cluster x coordinates by y coordinate."
  [cluster]
  (reduce (fn [rows {:keys [x y]}]
            (assoc rows y (conj (get rows y []) x)))
          {}
          cluster))

(defn- keep-max
  [{:keys [max-size squares] :as result} {:keys [size] :as square}]
  (cond
    (> size max-size)
    {:max-size size :squares #{square}}

    (= size max-size)
    {:max-size max-size :squares (conj squares square)}

    :else
    result))

(defn- largest-valid-square
  "Returns the largest visited square represented by an interior cluster square.

   Cluster membership guarantees every outer edge tile except the four corners.
   The top-left corner is shared by all candidate sizes, so a missing corner can
   reject the whole candidate immediately."
  [tiles x y max-inner-size min-worthwhile-inner-size]
  (let [left (dec x)
        top (dec y)]
    (when (contains? tiles {:x left :y top})
      (loop [inner-size max-inner-size]
        (when (>= inner-size min-worthwhile-inner-size)
          (let [right (+ x inner-size)
                bottom (+ y inner-size)]
            (if (and (contains? tiles {:x right :y top})
                     (contains? tiles {:x left :y bottom})
                     (contains? tiles {:x right :y bottom}))
              {:x left
               :y top
               :size (+ inner-size 2)}
              (recur (dec inner-size)))))))))

(defn- scan-cluster
  "Updates the global max-square result using one cluster.

   DP runs bottom-right to top-left and keeps one row of state. Candidates that
   cannot tie or beat the current global maximum are skipped."
  [cluster tiles initial-result]
  (let [rows (tile-rows cluster)]
    (loop [ys (seq (sort > (keys rows)))
           below-y nil
           below {}
           result initial-result]
      (if-let [y (first ys)]
        (let [below (if (= below-y (inc y)) below {})
              [current result]
              (loop [xs (seq (sort > (get rows y)))
                     current {}
                     result result]
                (if-let [x (first xs)]
                  (let [inner-size (inc (min (get current (inc x) 0)
                                             (get below x 0)
                                             (get below (inc x) 0)))
                        current (assoc current x inner-size)
                        min-worthwhile-inner-size
                        (max min-inner-size (- (:max-size result) 2))
                        result
                        (if (< inner-size min-worthwhile-inner-size)
                          result
                          (if-let [square (largest-valid-square
                                           tiles
                                           x y
                                           inner-size
                                           min-worthwhile-inner-size)]
                            (keep-max result square)
                            result))]
                    (recur (next xs) current result))
                  [current result]))]
          (recur (next ys) y current result))
        result))))

(defn max-squares
  "Returns all maximum filled squares with an edge length of at least 4.

   Any filled square of size s >= 4 has a filled (s-2)x(s-2) interior whose
   tiles all belong to one cluster. DP therefore runs only on cluster interiors;
   validating the four outer corners is sufficient to prove the full square.

   Clusters are processed largest-first. Once a maximum is known, processing
   stops when the remaining clusters are too small even to tie it."
  [clusters tiles]
  (let [minimum-cluster-size (required-cluster-size min-square-size)
        eligible-clusters (sort-by count >
                                   (filter #(>= (count %) minimum-cluster-size)
                                           clusters))]
    (:squares
     (loop [remaining (seq eligible-clusters)
            result {:max-size 0 :squares #{}}]
       (if-let [cluster (first remaining)]
         (let [best-size (:max-size result)
               required-size (if (>= best-size min-square-size)
                               (required-cluster-size best-size)
                               minimum-cluster-size)]
           (if (< (count cluster) required-size)
             result
             (recur (next remaining)
                    (scan-cluster cluster tiles result))))
         result)))))
