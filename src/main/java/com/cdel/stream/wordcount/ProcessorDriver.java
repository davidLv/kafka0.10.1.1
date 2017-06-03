package com.cdel.stream.wordcount;

import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.processor.TopologyBuilder;
import org.apache.kafka.streams.processor.WallclockTimestampExtractor;
import org.apache.kafka.streams.state.Stores;

import java.util.Properties;

/**
 * User: Bill Bejeck
 * Date: 11/5/15
 * Time: 10:22 PM
 */
//先启动ProcessorDriver等待处理，然后调用NativeProducer往qz主题发消息，打开客户端监听qz2主题的消费
public class ProcessorDriver {

    public static void main(String[] args) throws Exception {

        StreamsConfig streamingConfig = new StreamsConfig(getProperties());

        StringDeserializer stringDeserializer = new StringDeserializer();
        StringSerializer stringSerializer = new StringSerializer();

        //有了在处理器API中自定义的处理器，然后就可以使用TopologyBuilder来将处理器连接到一起从而构建处理器拓扑。
        TopologyBuilder topologyBuilder = new TopologyBuilder();

        //首先一个名为“SOURCE”的源节点被加入拓扑，使用addSource方法，且使用“qz”这一Kafka topic来提供数据。
        topologyBuilder.addSource("SOURCE", stringDeserializer, stringDeserializer, "qz")
        //随后使用addProcessor方法加入三个处理器节点。这里第一个处理器是“SOURCE”节点的子节点，且是后两个节点的父节点。
                .addProcessor("PROCESS", MyProcessor::new, "SOURCE")
                //处理器API不仅可以处理当前到达的记录，也可以管理本地状态仓库以使得已到达的记录都可用于有状态的处理操作中（如聚合或开窗连接）。
                //为利用本地状态仓库的优势，可使用TopologyBuilder.addStateStore方法以便在创建处理器拓扑时创建一个相应的本地状态仓库；
                //或将一个已创建的本地状态仓库与现有处理器节点连接，通过TopologyBuilder.connectProcessorAndStateStores方法。
                .addStateStore(Stores.create("Counts").withStringKeys().withStringValues().inMemory().build(), "PROCESS")
                //.connectProcessorAndStateStores("PROCESS", "COUNTS")
                //.addProcessor("PROCESS2", MyProcessor::new, "PROCESS")
                //.addProcessor("PROCESS3", MyProcessor::new, "PROCESS")
                //最后使用addSink方法加入三个sink节点，每个都从一个父处理器节点中获取数据并写到一个topic中。
                .addSink("SINK", "qz2", stringSerializer, stringSerializer, "PROCESS");

        System.out.println("Starting PurchaseProcessor Example");
        KafkaStreams streaming = new KafkaStreams(topologyBuilder, streamingConfig);
        streaming.start();
        System.out.println("Now started PurchaseProcessor Example");

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