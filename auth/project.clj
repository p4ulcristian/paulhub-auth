(defproject auth "latest"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [http-kit "2.3.0"]
                 [buddy/buddy-sign "3.1.0"]
                 [buddy/buddy-core "1.6.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-json "0.4.0"]
                 [compojure "1.6.1"]
                 [hickory "0.7.1"]
                 ;[ring-transit "0.1.6"]
                 [ring-server "0.5.0"]
                 [ring "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [hiccup "1.0.5"]
                 [metosin/reitit "0.3.7"]
                 [ring/ring-jetty-adapter "1.8.0"]]

  :target-path "target/"
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
  :native-image {:graal-bin "/usr/bin"
                 :jvm-opts ["-Dclojure.compiler.direct-linking=true"]
                 :opts ["-H:EnableURLProtocols=http"
                        "--report-unsupported-elements-at-runtime" ;; ignore native-image build errors
                        "--initialize-at-build-time"
                        "--no-fallback"
                        "--no-server" ;; TODO issue with subsequent builds failing on same server
                        "--verbose"]
                 :name "server"}
  :main auth.core
  :profiles {:dev {:dependencies [[org.clojure/test.check "0.9.0"]]}
             :uberjar {:aot :all}})
