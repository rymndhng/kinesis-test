(defproject rymndhng/kinesis-test "0.1.0-SNAPSHOT"
  :description "Kinesis Testing"
  :url "https://github.com/rymndhng/kinesis-test"

  :profiles
  {:dev {:source-paths ["src" "dev"]
         :repl-options {:init-ns dev}
         :dependencies [[com.amazonaws/amazon-kinesis-client "1.7.2" :classifier "sources"]
                        [com.amazonaws/amazon-kinesis-client "1.7.2" :classifier "javadoc"]
                        ]
         :plugins [[s3-wagon-private "1.3.0-alpha2"]]}}

  :dependencies
  [[org.clojure/clojure "1.9.0-alpha12"]
   [org.clojure/tools.logging "0.3.1"]
   [com.amazonaws/aws-java-sdk-kinesis "1.10.77"]
   [com.amazonaws/amazon-kinesis-client "1.7.2"]]

  :jvm-opts
  [
   ;; for kinesalite to work in development
   "-Dcom.amazonaws.sdk.disableCbor=true"
   ])
