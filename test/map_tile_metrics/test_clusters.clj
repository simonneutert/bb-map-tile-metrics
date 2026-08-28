(ns map-tile-metrics.test-clusters
  (:require [clojure.test :refer [deftest is testing]]
            [map-tile-metrics.clusters :as clusters]
            [map-tile-metrics.utils :as utils]))

(defn- filled-square
  [x y size]
  (into #{}
        (for [dy (range size)
              dx (range size)]
          {:x (+ x dx) :y (+ y dy)})))

(deftest test-clusters
  (testing "finds cluster tiles"
    (is (= [#{{:x 2 :y 2}}]
           (clusters/clusters (filled-square 1 1 3)))))

  (testing "keeps disconnected components separate"
    (let [tiles (into (filled-square 0 0 3)
                      (filled-square 10 10 3))]
      (is (= #{#{{:x 1 :y 1}}
               #{{:x 11 :y 11}}}
             (set (clusters/clusters tiles))))))

  (testing "test-data2"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data2.json")
          result (clusters/clusters tiles)]
      (is (= 340 (apply max (map count result))))))

  (testing "test-data"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data.json")
          result (clusters/clusters tiles)
          sizes (map count result)]
      (is (= 2011 (count tiles)))
      (is (= 24 (count (filter #{1} sizes))))
      (is (= 44 (count result)))
      (is (= 726 (apply max sizes))))))

(deftest test-max-clusters
  (let [small #{{:x 1 :y 1}}
        large-a #{{:x 1 :y 1} {:x 2 :y 1}}
        large-b #{{:x 4 :y 4} {:x 5 :y 4}}]
    (is (= #{large-a large-b}
           (clusters/max-clusters [small large-a large-b]))))
  (is (= #{} (clusters/max-clusters []))))
