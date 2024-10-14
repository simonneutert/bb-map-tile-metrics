#!/usr/bin/env bb

(require '[clojure.test :as t]
         '[babashka.classpath :as cp])

(cp/add-classpath "src:test")

(require
 'map-tile-metrics.test-utils
 'map-tile-metrics.test-clusters
 'map-tile-metrics.test-squares
 'map-tile-metrics.test-main)

(def test-results
  (t/run-tests 'map-tile-metrics.test-utils
               'map-tile-metrics.test-clusters
               'map-tile-metrics.test-squares
               'map-tile-metrics.test-main))

(let [{:keys [fail error]} test-results]
  (when (pos? (+ fail error))
    (System/exit 1)))