(ns map-tile-metrics.test-main
  (:require [clojure.test :refer :all]
            [map-tile-metrics.clusters :refer :all]
            [map-tile-metrics.utils :refer :all]
            [map-tile-metrics.main :refer :all]))

(deftest test-cluster-for-tiles
  (testing "cluster for tiles function"
    (let [data (read-data-from-file "test/map_tile_metrics/resources/test-data.json")
          ;; opts {:json data}
          clusters (clusters data)
          max-cluster (first (max-clusters clusters))]
      (is (= (count clusters) 44))
      (is (= (filter #(= (count %) 1) (sort-by count clusters))
             '(#{{:x 4271, :y 2642}}
               #{{:x 4284, :y 2798}}
               #{{:x 4274, :y 2803}}
               #{{:x 4275, :y 2805}}
               #{{:x 4261, :y 2789}}
               #{{:x 4306, :y 2785}}
               #{{:x 4261, :y 2791}}
               #{{:x 4303, :y 2802}}
               #{{:x 4271, :y 2647}}
               #{{:x 4310, :y 2786}}
               #{{:x 4304, :y 2803}}
               #{{:x 4291, :y 2768}}
               #{{:x 4284, :y 2653}}
               #{{:x 4278, :y 2652}}
               #{{:x 4273, :y 2767}}
               #{{:x 4290, :y 2807}}
               #{{:x 4269, :y 2767}}
               #{{:x 4300, :y 2768}}
               #{{:x 4272, :y 2769}}
               #{{:x 4292, :y 2786}}
               #{{:x 4304, :y 2805}}
               #{{:x 4307, :y 2767}}
               #{{:x 4304, :y 2777}}
               #{{:x 4300, :y 2789}})))
      (is (= 24 (count (filter #(= (count %) 1) clusters))))
      (is (= (count max-cluster) 726)))))