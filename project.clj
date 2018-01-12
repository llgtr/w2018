(defproject w2018 "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.10.758"]
                 [reagent "0.7.0"]
                 [re-frame "0.10.2"]
                 [compojure "1.6.0"]
                 [yogthos/config "0.8"]
                 [ring "1.4.0"]
                 [org.clojure/java.jdbc "0.7.5"]
                 [org.postgresql/postgresql "42.1.4"]
                 [cheshire "5.8.0"]
                 [clj-time "0.14.2"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [day8.re-frame/http-fx "0.1.4"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler w2018.handler/handler}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [figwheel-sidecar "0.5.13"]
                   [com.cemerick/piggieback "0.2.2"]
                   [re-frisk "0.5.3"]]

    :plugins      [[lein-figwheel "0.5.20"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "w2018.core/mount-root"}
     :compiler     {:main                 w2018.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload
                                           re-frisk.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}}}
    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            w2018.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :simple
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false
                    :infer-externs true}}]}

  :main w2018.server

  :aot [w2018.server]

  :uberjar-name "w2018.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "compile"])
