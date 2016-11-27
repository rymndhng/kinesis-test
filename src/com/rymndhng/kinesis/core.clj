(ns com.rymndhng.kinesis.core
  (:require [cheshire.core :as json])
  (:import
   (com.amazonaws.auth DefaultAWSCredentialsProviderChain)
   (com.amazonaws.services.kinesis
     AmazonKinesisClient)
   (com.amazonaws.services.kinesis.model
     PutRecordRequest CreateStreamRequest GetRecordsRequest ShardIteratorType)
   (com.amazonaws.services.kinesis.clientlibrary.interfaces.v2
     IRecordProcessor
     IRecordProcessorFactory)
   (com.amazonaws.services.kinesis.clientlibrary.lib.worker
     InitialPositionInStream)
   ))

;; -- Boilerplate  -------------------------------------------------------------
(defrecord MyRecordProcessor [state]
    IRecordProcessor
  (initialize [_ initializationInput])
  (processRecords [_ processRecordsInput])
  (shutdown [_ shutdownInput]))

(defrecord MyRecordProcessorFactory []
    IRecordProcessorFactory
  (createProcessor [_]
    (MyRecordProcessor. nil)))


;; -- Application  -------------------------------------------------------------
(def stream-name "my-first-stream")
(def my-application "leads-kinesis-test")
(def initial-stream-position InitialPositionInStream/LATEST)
(def credentials-provider (DefaultAWSCredentialsProviderChain.))


;; -- Producer  ----------------------------------------------------------------
(def message {:type           "IntegrationLogV1.1"
              :integration_id "abcd"
              :lead_id        "efgh"
              :message_type   "delivery-succeeded"})

(defn send-message!
  [client stream-name message]
  (.putRecord client
    (doto (PutRecordRequest.)
      (.setStreamName stream-name)
      (.setPartitionKey "there-is-no-partition-key")
      (.setData (-> message
                  json/generate-smile
                  java.nio.ByteBuffer/wrap)))))

(defn get-shard-iterator!
  "Gets a shard iterator of the first shard."
  [client stream-name]
  (let [shard (-> (.describeStream client stream-name)
                .getStreamDescription
                .getShards
                first)

        shard-id (.getShardId shard)
        starting-sequence-number (-> shard
                                   .getSequenceNumberRange
                                   .getStartingSequenceNumber)]
    (.getShardIterator (.getShardIterator client
                         stream-name
                         shard-id
                         "AT_SEQUENCE_NUMBER"
                         starting-sequence-number))))


(comment
  "Testing it out"

  (defonce client (doto (AmazonKinesisClient.)
                    (.setEndpoint "http://localhost:4567")))

  (send-message! client "foo"
    {:type           "IntegrationLogV1.1"
     :integration_id "integration-2"
     :lead_id        "lead-3"
     :message_type   "delivery-succeeded"})

  (send-message! client "foo"
    {:type           "IntegrationLogV1.1"
     :integration_id "integration-1"
     :lead_id        "lead-2"
     :message_type   "delivery-succeeded"})


  (.createStream client "foo" (int 1))
  ;; now read the shard description and get some messages

  (def shard-piterator (get-shard-iterator! client "foo"))

  (def records (.getRecords client (doto (GetRecordsRequest.)
                                     (.setShardIterator shard-iterator))))

  (json/parse-smile (-> records .getRecords first .getData .array))
  )
