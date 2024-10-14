(ns map-tile-metrics.test-utils
  (:require [clojure.test :refer :all]
            [map-tile-metrics.utils :refer :all]))

(deftest test-neighbors
  (testing "neighbors function"
    (is (= (set (neighbors {:x 1 :y 1}))
           #{{:x 1 :y 0} {:x 2 :y 1} {:x 1 :y 2} {:x 0 :y 1}}))))

(deftest test-all-neighbors
  (testing "all-neighbors function"
    (is (= (set (all-neighbors {:x 1 :y 1}))
           #{{:x 1 :y 0} {:x 2 :y 0} {:x 2 :y 1}
             {:x 2 :y 2} {:x 1 :y 2} {:x 0 :y 0}
             {:x 0 :y 1} {:x 0 :y 2}}))))

(deftest test-real-neighbors
  (testing "real-neighbors function"
    (let [lut #{{:x 1 :y 0} {:x 1 :y 1} {:x 2 :y 1}}]
      (is (= (set (real-neighbors {:x 1 :y 1} lut))
             #{{:x 1 :y 0} {:x 2 :y 1}}))
      (is (= (set (real-neighbors {:x -1 :y -1} lut))
             #{})))))