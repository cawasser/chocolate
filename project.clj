(defproject chocolate "lein-git-inject/version"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[buddy/buddy-auth "2.2.0"]
                 [buddy/buddy-core "1.6.0"]
                 [buddy/buddy-hashers "1.4.0"]
                 [buddy/buddy-sign "3.1.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.9.0"]
                 [cljs-ajax "0.8.0"]
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "0.8.319"]
                 [conman "0.8.4"]
                 [cprop "0.1.15"]
                 [day8.re-frame/http-fx "0.1.6"]
                 [expound "0.8.3"]
                 [funcool/struct "1.4.0"]
                 ;[luminus-jetty "0.1.7"]
                 [luminus-http-kit "0.1.6"]
                 [luminus-migrations "0.6.6"]
                 [luminus-transit "0.1.2"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.1"]
                 [metosin/muuntaja "0.6.6"]
                 [metosin/reitit "0.3.10"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597" :scope "provided"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.webjars.npm/bulma "0.8.0"]
                 [org.webjars.npm/material-icons "0.3.1"]
                 [org.webjars/webjars-locator "0.38"]
                 [org.xerial/sqlite-jdbc "3.28.0"]
                 [re-frame "0.10.9"]
                 [reagent "0.9.0-rc3"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.8.0"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.18"]

                 [nomnom/bunnicula "2.1.0"]
                 [clojms/CloJMS "0.0.3"]
                 [com.stuartsierra/component "0.4.0"]
                 [com.google.protobuf/protobuf-java "3.11.1"]
                 [clojusc/protobuf "3.5.1-v1.1"]

                 [cljsjs/toastr "2.1.2-1"]

                 [com.taoensso/sente "1.15.0"]
                 [trptcolin/versioneer "0.2.0"]]
                 ;[cljsjs/codemirror "5.44.0-1"]]

  :min-lein-version "2.0.0"

  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot chocolate.core

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-kibit "0.1.2"]
            [lein-marginalia "0.9.1"]
            [leancloud-lein-protobuf "0.5.4"]
            [day8/lein-git-inject "0.0.11"]
            [lein-pprint "1.3.2"]]

  :middleware [leiningen.git-inject/middleware]

  :proto-path "resources"
  :protoc "protoc"

  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :server-logfile "log/figwheel-logfile.log"
   :nrepl-port 7702
   :css-dirs ["resources/public/css"]
   :nrepl-middleware [cider.piggieback/wrap-cljs-repl]}


  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["protobuf" "compile" ["cljsbuild" "once" "min"]]
             :dependencies [[day8.re-frame/tracing-stubs "0.5.1"]]
             :cljsbuild{:builds
                        {:min
                         {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                          :compiler
                          {:output-dir "target/cljsbuild/public/js"
                           :output-to "target/cljsbuild/public/js/app.js"
                           :source-map "target/cljsbuild/public/js/app.js.map"
                           :optimizations :advanced
                           :pretty-print false
                           :infer-externs true
                           :closure-warnings
                           {:externs-validation :off :non-standard-jsdoc :off}
                           :externs ["react/externs/react.js"]}}}}

             :aot :all
             :uberjar-name "chocolate.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.11"]
                                 [cider/piggieback "0.4.2"]
                                 [doo "0.1.11"]
                                 [figwheel-sidecar "0.5.19"]
                                 [pjstadig/humane-test-output "0.10.0"]
                                 [prone "2019-07-08"]
                                 [day8.re-frame/re-frame-10x "0.4.5"]
                                 [day8.re-frame/tracing "0.5.3"]
                                 [ring/ring-devel "1.8.0"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]
                                 [jonase/eastwood "0.3.5"]
                                 [lein-doo "0.1.11"]
                                 [lein-figwheel "0.5.19"]]
                  :cljsbuild{:builds
                             {:app
                              {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                               :figwheel {:on-jsload "chocolate.core/mount-components"}
                               :compiler
                               {:output-dir "target/cljsbuild/public/js/out"
                                :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true
                                                  "day8.re_frame.tracing.trace_enabled_QMARK_"  true}
                                :optimizations :none
                                :preloads [day8.re-frame-10x.preload]
                                :output-to "target/cljsbuild/public/js/app.js"
                                :asset-path "/js/out"
                                :source-map true
                                :main chocolate.core ;"chocolate.app"
                                :pretty-print true}}}}


                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"] ;"target/%s/classes"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "chocolate.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}}


   :profiles/dev {}
   :profiles/test {}})
