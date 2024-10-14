(ns map-tile-metrics.test-clusters
  (:require [clojure.test :refer :all]
            [map-tile-metrics.clusters :refer :all]
            [map-tile-metrics.utils :as utils]))

(deftest test-clusters
  (testing "clusters function"
    (let [tiles #{{:x 1, :y 1} {:x 2, :y 1} {:x 3, :y 1}
                  {:x 1, :y 2} {:x 2, :y 2} {:x 3, :y 2}
                  {:x 1, :y 3} {:x 2, :y 3} {:x 3, :y 3}}]
      (is (= (clusters tiles) [#{{:x 2 :y 2}}]))))

  (testing "with huge dataset example2"
    (let [tiles (utils/read-data-from-file "test/map_tile_metrics/resources/test-data2.json")]
      (is (= (apply max (sort (pmap count (clusters tiles))))
             340))))

  (testing "with huge dataset example1"
    (let [tiles (utils/read-data-from-file "test/map_tile_metrics/resources/test-data.json")]
      (is (= (count tiles) 2011))
      (is (= (count (filter #(= 1 %) (pmap count (clusters tiles)))) 24))
      (is (= (count (clusters tiles)) 44))
      (is (= (apply max (sort (pmap count (clusters tiles))))
             726))
      ;; only a single max cluster
      (is (= (count (filter #(= % 726) (pmap count (clusters tiles))))
             1)))))

(deftest test-cluster-for-tile
  (testing "cluster-for-tile function"
    (let [lut #{{:x 0 :y 0} {:x 1 :y 0} {:x 0 :y 1} {:x 1 :y 1}
                {:x 2 :y 2} {:x 3 :y 2} {:x 2 :y 3} {:x 3 :y 3}}]
      (is (= (set (cluster-for-tile {:x 0 :y 0} lut #{}))
             #{{:x 0 :y 0} {:x 1 :y 0} {:x 0 :y 1} {:x 1 :y 1}}))
      (is (= (set (cluster-for-tile {:x 2 :y 2} lut #{}))
             #{{:x 2 :y 2} {:x 3 :y 2} {:x 2 :y 3} {:x 3 :y 3}})))))

(deftest test-cluster-lut
  (testing "cluster-lut"
    (let [lut #{{:x 0 :y 0} {:x 1 :y 0} {:x 2 :y 0}
                {:x 0 :y 1} {:x 1 :y 1} {:x 2 :y 1}
                {:x 0 :y 2} {:x 1 :y 2} {:x 2 :y 2}
                {:x 3 :y 3}}]
      (is (= (cluster-lut lut) #{{:x 1 :y 1}})))))