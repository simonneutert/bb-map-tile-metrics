(ns map-tile-metrics.test-utils
  (:require [clojure.test :refer [deftest is testing]]
            [map-tile-metrics.utils :as utils]))

(deftest test-neighbors
  (is (= #{{:x 1 :y 0}
           {:x 2 :y 1}
           {:x 1 :y 2}
           {:x 0 :y 1}}
         (set (utils/neighbors {:x 1 :y 1})))))

(deftest test-all-neighbors
  (is (= #{{:x 1 :y 0}
           {:x 2 :y 0}
           {:x 2 :y 1}
           {:x 2 :y 2}
           {:x 1 :y 2}
           {:x 0 :y 0}
           {:x 0 :y 1}
           {:x 0 :y 2}}
         (set (utils/all-neighbors {:x 1 :y 1})))))

(deftest test-real-neighbors
  (let [tiles #{{:x 1 :y 0}
                {:x 1 :y 1}
                {:x 2 :y 1}}]
    (is (= #{{:x 1 :y 0} {:x 2 :y 1}}
           (set (utils/real-neighbors {:x 1 :y 1} tiles))))
    (is (empty? (utils/real-neighbors {:x -1 :y -1} tiles)))))

(deftest test-into-lookup-table
  (testing "canonicalizes keyword-keyed tiles and ignores extra metadata"
    (is (= #{{:x 1 :y 2}}
           (utils/into-lookup-table [{:x 1 :y 2 :z 14}]))))

  (testing "canonicalizes string-keyed tiles"
    (is (= #{{:x 1 :y 2}}
           (utils/into-lookup-table [{"x" 1 "y" 2 "z" 14}]))))

  (testing "accepts mixed string and keyword coordinate keys"
    (is (= #{{:x 1 :y 2}}
           (utils/into-lookup-table [{:x 1 "y" 2}])))))
