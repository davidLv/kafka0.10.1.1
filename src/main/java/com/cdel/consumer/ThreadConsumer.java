package com.cdel.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import com.cdel.util.KafkaConfig;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

/**Kafka 0.9版本开始推出了Java版本的consumer，优化了coordinator的设计以及摆脱了对zookeeper的依赖。社区最近也在探讨正式用这套consumer API替换Scala版本的consumer的计划。鉴于目前这方面的资料并不是很多，本文将尝试给出一个利用KafkaConsumer编写的多线程消费者实例，希望对大家有所帮助。
    这套API最重要的入口就是KafkaConsumer(o.a.k.clients.consumer.KafkaConsumer)，普通的单线程使用方法官网API已有介绍，这里不再赘述了。因此，我们直奔主题——讨论一下如何创建多线程的方式来使用KafkaConsumer。KafkaConsumer和KafkaProducer不同，后者是线程安全的，因此我们鼓励用户在多个线程中共享一个KafkaProducer实例，这样通常都要比每个线程维护一个KafkaProducer实例效率要高。但对于KafkaConsumer而言，它不是线程安全的，所以实现多线程时通常由两种实现方法：
1 每个线程维护一个KafkaConsumer

优点
方便实现
速度较快，因为不需要任何线程间交互
易于维护分区内的消息顺序

缺点
更多的TCP连接开销(每个线程都要维护若干个TCP连接)
consumer数受限于topic分区数，扩展性差
频繁请求导致吞吐量下降
线程自己处理消费到的消息可能会导致超时，从而造成rebalance
 * @author dell
 *
 */
class ConsumerRunnable implements Runnable {

	// 每个线程维护私有的KafkaConsumer实例
	private final KafkaConsumer<String, String> consumer;

	public ConsumerRunnable(String topic, String propPath) {
		Properties props = null;
		try {
			props = KafkaConfig.getProperties(propPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.consumer = new KafkaConsumer<>(props);
		consumer.subscribe(Arrays.asList(topic));   // 本例使用分区副本自动分配策略
	}

	@Override
	public void run() {
	    while (true) {
	        ConsumerRecords<String, String> records = consumer.poll(200);   // 本例使用200ms作为获取超时时间
	        for (ConsumerRecord<String, String> record : records) {
	            // 这里面写处理消息的逻辑，本例中只是简单地打印消息
	            System.out.println(Thread.currentThread().getName() + " consumed " + record.partition() + "th message with offset: " + record.offset());
	        }
	    }
	}

}

class ConsumerGroup {

    private List<ConsumerRunnable> consumers;

    public ConsumerGroup(int consumerNum, String topic, String propPath) {
        consumers = new ArrayList<>(consumerNum);
        for (int i = 0; i < consumerNum; ++i) {
            ConsumerRunnable consumerThread = new ConsumerRunnable(topic, propPath);
            consumers.add(consumerThread);
        }
    }

    public void execute() {
        for (ConsumerRunnable task : consumers) {
            new Thread(task).start();
        }
    }

}

public class ThreadConsumer {

	public static void main(String[] args) {
		String propPath = "kafka/consumer.properties";
		String topic = "test-topic";
		int consumerNum = 3;

		ConsumerGroup consumerGroup = new ConsumerGroup(consumerNum, topic, propPath);
		consumerGroup.execute();
	}
}
