(ns map-tile-metrics.test-squares
  (:require [clojure.test :refer [deftest is testing]]
            [map-tile-metrics.clusters :as clusters]
            [map-tile-metrics.squares :as squares]
            [map-tile-metrics.utils :as utils]))

(defn- filled-square
  [x y size]
  (into #{}
        (for [dy (range size)
              dx (range size)]
          {:x (+ x dx) :y (+ y dy)})))

(defn- max-squares-for
  [tiles]
  (squares/max-squares (clusters/clusters tiles) tiles))

(deftest test-max-squares-small-grids
  (testing "finds a complete 4x4 square"
    (is (= #{{:x 0 :y 0 :size 4}}
           (max-squares-for (filled-square 0 0 4)))))

  (testing "returns all equally large disconnected squares"
    (let [tiles (into (filled-square 0 0 4)
                      (filled-square 10 10 4))]
      (is (= #{{:x 0 :y 0 :size 4}
               {:x 10 :y 10 :size 4}}
             (max-squares-for tiles)))))

  (testing "falls back when the largest interior has a missing outer corner"
    (let [tiles (disj (filled-square 0 0 5) {:x 4 :y 4})]
      (is (= #{{:x 0 :y 0 :size 4}
               {:x 1 :y 0 :size 4}
               {:x 0 :y 1 :size 4}}
             (max-squares-for tiles)))))

  (testing "rejects candidates immediately when the shared top-left corner is missing"
    (let [tiles (disj (filled-square 0 0 5) {:x 0 :y 0})]
      (is (= #{{:x 1 :y 0 :size 4}
               {:x 0 :y 1 :size 4}
               {:x 1 :y 1 :size 4}}
             (max-squares-for tiles)))))

  (testing "returns no square below the 4x4 minimum"
    (is (= #{} (max-squares-for (filled-square 0 0 3))))))

(deftest test-max-squares-fixtures
  (testing "test-data"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data.json")
          result (max-squares-for tiles)]
      (is (= #{{:x 4266 :y 2777 :size 16}
               {:x 4267 :y 2777 :size 16}
               {:x 4268 :y 2777 :size 16}
               {:x 4269 :y 2777 :size 16}
               {:x 4270 :y 2777 :size 16}
               {:x 4271 :y 2777 :size 16}}
             result))))

  (testing "test-data2"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data2.json")
          result (max-squares-for tiles)]
      (is (= #{{:x 4275 :y 2773 :size 13}
               {:x 4275 :y 2774 :size 13}
               {:x 4275 :y 2775 :size 13}}
             result))))

  (testing "test-data3"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data3.json")
          result (max-squares-for tiles)]
      (is (= #{{:x 8543 :y 5559 :size 5}
               {:x 8544 :y 5559 :size 5}
               {:x 8550 :y 5563 :size 5}}
             result))))

  (testing "test-data-micro"
    (let [tiles (utils/read-data-from-file
                 "test/map_tile_metrics/resources/test-data-micro.json")
          result (max-squares-for tiles)]
      (is (= 13 (apply max (map :size result))))
      (is (= 2 (count result))))))
