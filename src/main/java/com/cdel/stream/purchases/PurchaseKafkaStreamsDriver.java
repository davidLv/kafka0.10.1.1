package com.cdel.stream.purchases;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import com.cdel.stream.purchases.model.Purchase;
import com.cdel.stream.purchases.model.PurchasePattern;
import com.cdel.stream.purchases.model.RewardAccumulator;
import com.cdel.stream.util.JsonDeserializer;
import com.cdel.stream.util.JsonSerializer;

import java.util.Properties;

public class PurchaseKafkaStreamsDriver {

	public static void main(String[] args) {
        StreamsConfig streamsConfig = new StreamsConfig(getProperties());

        JsonSerializer<Purchase> purchaseJsonSerializer = new JsonSerializer<>();
        JsonDeserializer<Purchase> purchaseJsonDeserializer = new JsonDeserializer<>(Purchase.class);

        JsonSerializer<RewardAccumulator> rewardAccumulatorJsonSerializer = new JsonSerializer<>();
        JsonDeserializer<RewardAccumulator> rewardAccumulatorJsonDeserializer = new JsonDeserializer<>(RewardAccumulator.class);

        JsonSerializer<PurchasePattern> purchasePatternJsonSerializer = new JsonSerializer<>();
        JsonDeserializer<PurchasePattern> purchasePatternJsonDeserializer = new JsonDeserializer<>(PurchasePattern.class);

        Serde<Purchase> purchaseSerde = Serdes.serdeFrom(purchaseJsonSerializer, purchaseJsonDeserializer);
        Serde<RewardAccumulator> rewardAccumulatorSerde = Serdes.serdeFrom(rewardAccumulatorJsonSerializer, rewardAccumulatorJsonDeserializer);
        Serde<PurchasePattern> purchasePatternSerde = Serdes.serdeFrom(purchasePatternJsonSerializer, purchasePatternJsonDeserializer);

        Serde<String> stringSerde = Serdes.String();

        KStreamBuilder kStreamBuilder = new KStreamBuilder();

        KStream<String, Purchase> purchaseKStream = kStreamBuilder
        		.stream(stringSerde, purchaseSerde, "qz")
                .mapValues(p -> Purchase.builder(p).maskCreditCard().build());

        purchaseKStream.mapValues(purchase -> PurchasePattern.builder(purchase).build())
        	.to(stringSerde, purchasePatternSerde, "patterns");

        purchaseKStream.mapValues(purchase -> RewardAccumulator.builder(purchase).build())
        	.to(stringSerde, rewardAccumulatorSerde, "rewards");

        purchaseKStream.to(stringSerde, purchaseSerde, "purchases");

        System.out.println("Starting PurchaseStreams Example");
        KafkaStreams kafkaStreams = new KafkaStreams(kStreamBuilder, streamsConfig);
        kafkaStreams.start();
        System.out.println("Now started PurchaseStreams Example");

    }

    private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "Example-Kafka-Streams-Job");
        props.put("group.id", "streams-purchases");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testing-streams-api");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.192.145:9092,192.168.192.146:9092,192.168.192.147:9092");
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "192.168.192.145:2181,192.168.192.146:2181,192.168.192.147:2181");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }

}
