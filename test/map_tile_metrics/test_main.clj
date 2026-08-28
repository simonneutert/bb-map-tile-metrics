(ns map-tile-metrics.test-main
  (:require [clojure.test :refer [deftest is testing]]
            [map-tile-metrics.main :as main]))

(deftest test-inline-input
  (testing "JSON input is canonicalized to x/y"
    (is (= #{{:x 1 :y 2}}
           (main/from-json {:json "[{\"x\":1,\"y\":2,\"z\":14}]"}))))

  (testing "EDN input with keyword keys is canonicalized to x/y"
    (is (= #{{:x 1 :y 2}}
           (main/from-edn {:edn "[{:x 1 :y 2 :z 14}]"}))))

  (testing "EDN input with string keys is supported"
    (is (= #{{:x 1 :y 2}}
           (main/from-edn {:edn "[{\"x\" 1 \"y\" 2 \"z\" 14}]"})))))
