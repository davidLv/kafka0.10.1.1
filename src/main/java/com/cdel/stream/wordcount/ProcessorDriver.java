package com.cdel.stream.wordcount;

import org.apache.kafka.common.serialization.Serdes;
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
        //源处理器（Source Processor）：源处理器是一个没有任何上游处理器的特殊类型的流处理器。它从一个或多个kafka主题生成输入流。通过消费这些主题的消息并将它们转发到下游处理器。
        topologyBuilder.addSource("SOURCE", stringDeserializer, stringDeserializer, "qz")
        //随后使用addProcessor方法加入三个处理器节点。这里第一个处理器是“SOURCE”节点的子节点，且是后两个节点的父节点。
                .addProcessor("PROCESS", MyProcessor::new, "SOURCE")
                //处理器API不仅可以处理当前到达的记录，也可以管理本地状态仓库以使得已到达的记录都可用于有状态的处理操作中（如聚合或开窗连接）。
                //为利用本地状态仓库的优势，可使用TopologyBuilder.addStateStore方法以便在创建处理器拓扑时创建一个相应的本地状态仓库；
                //或将一个已创建的本地状态仓库与现有处理器节点连接，通过TopologyBuilder.connectProcessorAndStateStores方法。
                .addStateStore(Stores.create("Counts").withStringKeys().withStringValues().inMemory().build(), "PROCESS")
                //Processor API不仅限于当有消息到达时候调用process()方法,也可以保存记录到本地状态仓库（如汇总或窗口连接）。
                //利用这个特性，开发者可以使用StateStore接口定义一个状态仓库（Kafka Streams库也有一些扩展的接口，
                //如KeyValueStore）。在实际开发中，开发者通常不需要从头开始自定义这样的状态仓库，可以很简单使用Stores工厂来设定状态仓库是持久化的或日志备份等。
                //在下面的例子中，创建一个名为”Counts“的持久化的key-value仓库，key类型String和value类型Long。
                //为了利用这些状态仓库，开发者可以在构建处理器拓扑时使用TopologyBuilder.addStateStore方法来创建本地状态，
                //并将它与需要访问它的处理器节点相关联，或者也可以通过TopologyBuilder.connectProcessorAndStateStores将创建的状态仓库与现有的处理器节点连接。
                //.addStateStore(Stores.create("Counts").withKeys(Serdes.String()).withValues(Serdes.Long()).persistent().build(), "PROCESS")
                //.connectProcessorAndStateStores("PROCESS", "COUNTS")
                //.addProcessor("PROCESS2", MyProcessor::new, "PROCESS")
                //.addProcessor("PROCESS3", MyProcessor::new, "PROCESS")
                //最后使用addSink方法加入三个sink节点，每个都从一个父处理器节点中获取数据并写到一个topic中。
                //Sink处理器：sink处理器是一个没有下游流处理器的特殊类型的流处理器。它接收上游流处理器的消息发送到一个指定的Kafka主题。
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
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "192.168.170.104:9092,192.168.170.105:9092,192.168.170.106:9092");
        props.put(StreamsConfig.ZOOKEEPER_CONNECT_CONFIG, "192.168.170.104:2181,192.168.170.105:2181,192.168.170.106:2181");
        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 1);
        props.put(StreamsConfig.TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class);
        return props;
    }
}