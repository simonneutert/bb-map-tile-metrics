(ns map-tile-metrics.main
  (:require [babashka.cli :as cli]
            [babashka.fs :as fs]
            [cheshire.core :as json]
            [map-tile-metrics.utils :as utils]
            [map-tile-metrics.clusters :as clusters]
            [map-tile-metrics.squares :as squares]))

(defn show-help
  [spec]
  (cli/format-opts (merge spec {:order (vec (keys (:spec spec)))})))

(def cli-spec
  {:spec
   {:json {:desc "json content string"
           :alias :j ; adds -j alias for --json
           :require false}
    :edn {:desc "edn content string"
          :alias :e ; adds -e alias for --edn
          :require false}
    :file {:desc "file to read"
           :alias :f ; adds -f alias for --file
           :validate fs/exists?}} ; tests if --file exists 
   :error-fn ; a function to handle errors
   (fn [{:keys [spec type cause msg option] :as data}]
     (if (= :org.babashka/cli type)
       (case cause
         :require
         (println
          (format "Missing required argument: %s\n" option))
         :validate
         (println
          (format "%s does not exist!\n" msg)))))})

(def cli-options {:json {:default "[{\"x:\" 0, \"y:\" 0}]"}
                  :help {:coerce :boolean}})

(def opts (cli/parse-opts *command-line-args* {:spec cli-options}))

(defn from-json [opts]
  (->> (json/parse-string (:json opts) true)
       (utils/into-lookup-table)))

(defn from-edn [opts]
  (->> (pmap clojure.walk/keywordize-keys (read-string (:edn opts)))
       (utils/into-lookup-table)))

(defn -main
  [& args]
  (let [opts (cli/parse-opts args cli-spec)]
    (if (or (:help opts) (:h opts))
      (println (show-help cli-spec))
      (let [tiles (cond (or (:j opts) (:json opts)) (from-json opts)
                        (or (:e opts) (:edn opts)) (from-edn opts)
                        (or (:f opts) (:file opts)) (utils/read-data-from-file (:file opts)))
            clusters (clusters/clusters tiles)
            max-clusters (clusters/max-clusters clusters)
            max-squares (squares/max-squares clusters tiles)]
        (json/encode {:clusters clusters
                      :max_clusters max-clusters
                      :squares max-squares})))))