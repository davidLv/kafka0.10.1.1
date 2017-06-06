package com.cdel.stream.wordcount2;

import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KStreamBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;

//http://www.orchome.com/335
public class WordCountLambdaExample {

	public static void main(String[] args) {
		StreamsConfig config = new StreamsConfig(getProperties());
		KStreamBuilder builder = new KStreamBuilder();

		//序列化/反序列化Sting和Long类型
		final Serde<String> stringSerde = Serdes.String();
		final Serde<Integer> longSerde = Serdes.Integer();

		//通过指定输入topic “streams-file-input”来构造KStream实例，
		//输入数据就以文本的形式保存在topic “streams-file-input” 中。
		//(在本示例中，我们忽略所有消息的key.)
		KStream<String, String> textLines = builder.stream(stringSerde, stringSerde, "qz");
		textLines
		//以空格为分隔符，将每行文本数据拆分成多个单词。
		//这些文本行就是从输入topic中读到的每行消息的Value。
		//我们使用flatMapValues方法来处理每个消息Value，而不是更通用的flatMap
		.flatMapValues(value -> {
			System.out.println(value);
			return Arrays.asList(value.toLowerCase().split("\\W+"));
		})
		//所以我们将每个单词作为map的key。
		.map((key, value) -> {
			System.out.println(key);
			System.out.println(value);
			return new KeyValue<>(value, value);
		})
		.to(stringSerde, stringSerde, "qz2");
		//通过key来统计每个单词的次数
		//这会将流类型从KStream<String,String>转为KTable<String,Long> (word-count).
		//因此我们必须提供String和long的序列化反序列化方法。
		//.groupByKey().count("Counts")
		//转化KTable<String,Long>到KStream<String,Long>
		//.toStream();
		//将KStream<String,Long>写入到输出topic中。
		//wordCounts.to(stringSerde, longSerde, "qz2");

		System.out.println("Starting PurchaseProcessor Example");
		KafkaStreams streams = new KafkaStreams(builder, config);
	    streams.start();
	    System.out.println("Now started PurchaseProcessor Example");
	}

	private static Properties getProperties() {
        Properties props = new Properties();
        props.put(StreamsConfig.CLIENT_ID_CONFIG, "Example-Kafka-Streams-Job2");
        props.put("group.id", "streams-wordcount");
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "testing-streams-api2");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.192.145:9092,192.168.192.146:9092,192.168.192.147:9092");
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "192.168.192.145:2181,192.168.192.146:2181,192.168.192.147:2181");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }

}
