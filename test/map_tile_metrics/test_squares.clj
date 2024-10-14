(ns map-tile-metrics.test-squares
  (:require [clojure.test :refer :all]
            [map-tile-metrics.utils :refer :all]
            [map-tile-metrics.clusters :refer :all]
            [map-tile-metrics.squares :refer :all]))

(deftest test-max-square-from-tile
  (testing "max-square-from-tile"
    (let [cluster #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1}
                    {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2}
                    {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}
                    {:x 1 :y 4}
                    {:x 1 :y 10}}]
      (is (= (max-square-from-tile {:x 1 :y 1} cluster)
             {:x 1 :y 1, :size 3})))))

(deftest test-squares-in-cluster-with-borders
  (testing "squares-in-cluster-with-borders in a square cluster-with-borders"
    (let [cluster-including-borders #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1}
                                      {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2}
                                      {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}}]

      (is (= (squares-in-cluster-with-borders cluster-including-borders)
             #{{:x 3, :y 2, :size 1}
               {:x 2, :y 1, :size 2}
               {:x 2, :y 2, :size 2}
               {:x 1, :y 1, :size 3}
               {:x 3, :y 3, :size 1}
               {:x 1, :y 2, :size 2}
               {:x 3, :y 1, :size 1}
               {:x 2, :y 3, :size 1}
               {:x 1, :y 3, :size 1}}))))


  (testing "squares-in-cluster-with-borders crunchy"
    (let [cluster-including-borders-with-hole #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 4 :y 1}
                                                {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 4 :y 2}
                                                {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3} {:x 4 :y 3}
                                                {:x 1 :y 4} {:x 2 :y 4} {:x 3 :y 4}
                                                {:x 1 :y 5} {:x 2 :y 5}
                                                {:x 1 :y 6} {:x 2 :y 6} {:x 3 :y 6}
                                                {:x 1 :y 7} {:x 2 :y 7} {:x 3 :y 7} {:x 4 :y 7}
                                                {:x 1 :y 8} {:x 2 :y 8} {:x 3 :y 8} {:x 4 :y 8}}]

      (is (= (apply max (map :size (squares-in-cluster-with-borders cluster-including-borders-with-hole)))
             3))))

  (testing "squares-in-cluster-with-borders crunchy corners"
    (let [cluster-including-borders-with-hole #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1} {:x 4 :y 1}
                                                {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2} {:x 4 :y 2}
                                                {:x 1 :y 3} {:x 2 :y 3}             {:x 4 :y 3}
                                                {:x 1 :y 4} {:x 2 :y 4} {:x 3 :y 4}
                                                {:x 1 :y 5} {:x 2 :y 5} {:x 3 :y 5} {:x 4 :y 5}
                                                {:x 1 :y 6} {:x 2 :y 6} {:x 3 :y 6} {:x 4 :y 6}
                                                {:x 1 :y 7} {:x 2 :y 7} {:x 3 :y 7} {:x 4 :y 7}
                                                {:x 1 :y 8} {:x 2 :y 8} {:x 3 :y 8} {:x 4 :y 8}}]

      (is (= (apply max (map :size (squares-in-cluster-with-borders cluster-including-borders-with-hole)))
             4))))

  (testing "squares-in-cluster-with-borders with example data"
    (let [big-example-all-tiles (read-data-from-file "test/map_tile_metrics/resources/test-data2.json")
          big-example-clusters (clusters big-example-all-tiles)
          big-example-clusters-with-borders (map #(add-borders-to-clusters (set %) big-example-all-tiles) big-example-clusters)]

      (is (= (count (mapcat squares-in-cluster-with-borders big-example-clusters-with-borders))
             804))
      (is (= (apply max (mapv :size (mapcat squares-in-cluster-with-borders big-example-clusters-with-borders)))
             13)))))

(deftest test-squares
  (testing "test-squares test-data"
    (let [tiles (read-data-from-file "test/map_tile_metrics/resources/test-data.json")
          clusters (clusters tiles)
          max-cluster (apply max (map count clusters))]
      (is (= (count clusters) 44))
      (is (= max-cluster 726))
      (is (= (apply max (map :size (squares clusters tiles 16))) 16))
      (is (= (squares clusters tiles 16)
             #{{:x 4266, :y 2777, :size 16}
               {:x 4267, :y 2777, :size 16}
               {:x 4268, :y 2777, :size 16}
               {:x 4269, :y 2777, :size 16}
               {:x 4270, :y 2777, :size 16}
               {:x 4271, :y 2777, :size 16}}))
      (is (= (count (squares clusters tiles 16)) 6))))

  (testing "test-squares test-data2"
    (let [tiles (read-data-from-file "test/map_tile_metrics/resources/test-data2.json")
          clusters (clusters tiles)
          max-cluster (apply max (map count clusters))]
      (is (= (count clusters) 33))
      (is (= max-cluster 340))
      (is (= (apply max (map :size (squares clusters tiles 13))) 13))
      (is (= (squares clusters tiles 13)
             #{{:x 4275, :y 2773, :size 13}
               {:x 4275, :y 2774, :size 13}
               {:x 4275, :y 2775, :size 13}}))

      (is (= (count (squares clusters tiles 13)) 3))))

  (testing "test-squares test-data3"
    (let [tiles (read-data-from-file "test/map_tile_metrics/resources/test-data3.json")
          clusters (clusters tiles)
          max-cluster (apply max (map count clusters))]

      (is (= max-cluster 25))

      (is (= (apply max (map :size (squares clusters tiles 4))) 5))

      (is (= (squares clusters tiles (apply max (map :size (squares clusters tiles 4))))
             #{{:x 8543, :y 5559, :size 5}
               {:x 8544, :y 5559, :size 5}
               {:x 8550, :y 5563, :size 5}}))))

  (testing "test-squares test-data-micro"
    (let [tiles (read-data-from-file "test/map_tile_metrics/resources/test-data-micro.json")
          clusters (clusters tiles)
          max-cluster (apply max (map count clusters))]

      (is (= max-cluster 1102))
      (is (= (apply max (map :size (max-squares clusters tiles))) 13))
      (is (= (count (max-squares clusters tiles)) 2)))))

(deftest test-cluster-with-borders
  (testing "cluster-with-borders"
    (let [result (add-borders-to-clusters #{{:x 2 :y 2}}
                                          #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1}
                                            {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2}
                                            {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}
                                            {:x 1 :y 4}
                                            {:x 1 :y 10}})]
      (is (= (count result) 9))
      (is (= result
             #{{:x 1 :y 1} {:x 2 :y 1} {:x 3 :y 1}
               {:x 1 :y 2} {:x 2 :y 2} {:x 3 :y 2}
               {:x 1 :y 3} {:x 2 :y 3} {:x 3 :y 3}})))))