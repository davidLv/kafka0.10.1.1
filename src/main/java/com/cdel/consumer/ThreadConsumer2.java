package com.cdel.consumer;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.cdel.util.KafkaConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**2 维护一个或多个KafkaConsumer，同时维护多个事件处理线程(worker thread)

优点
可独立扩展consumer数和worker数，伸缩性好

缺点
实现麻烦
通常难于维护分区内的消息顺序
处理链路变长，导致难以保证提交位移的语义正确性

总结一下，这两种方法或是模型都有各自的优缺点，在具体使用时需要根据自己实际的业务特点来选取对应的方法。就我个人而言，
我比较推崇第二种方法以及背后的思想，即不要将很重的处理逻辑放入消费者的代码中，很多Kafka consumer使用者碰到的各种rebalance超时、coordinator重新选举、心跳无法维持等问题都来源于此。
 * @author dell
 *
 */
class Worker implements Runnable {

	private ConsumerRecord<String, String> consumerRecord;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Worker(ConsumerRecord record) {
	    this.consumerRecord = record;
	}

	@Override
	public void run() {
	    // 这里写你的消息处理逻辑，本例中只是简单地打印消息
	    System.out.println(Thread.currentThread().getName() + " consumed " + consumerRecord.partition() + "th message with offset: " + consumerRecord.offset());
	}

}

class ConsumerHandler {

	// 本例中使用一个consumer将消息放入后端队列，你当然可以使用前一种方法中的多实例按照某张规则同时把消息放入后端队列
	private final KafkaConsumer<String, String> consumer;
	private ExecutorService executors;

	public ConsumerHandler(String topic, String propPath) {
		Properties props = null;
		try {
			props = KafkaConfig.getProperties(propPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	    consumer = new KafkaConsumer<>(props);
	    consumer.subscribe(Arrays.asList(topic));
	}

	public void execute(int workerNum) {
	    executors = new ThreadPoolExecutor(workerNum, workerNum, 0L, TimeUnit.MILLISECONDS,
	            new ArrayBlockingQueue<>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

	    while (true) {
	        ConsumerRecords<String, String> records = consumer.poll(200);
	        for (final ConsumerRecord record : records) {
	            executors.submit(new Worker(record));
	        }
	    }
	}

	public void shutdown() {
	    if (consumer != null) {
	        consumer.close();
	    }
	    if (executors != null) {
	        executors.shutdown();
	    }
	    try {
	        if (!executors.awaitTermination(10, TimeUnit.SECONDS)) {
	            System.out.println("Timeout.... Ignore for this case");
	        }
	    } catch (InterruptedException ignored) {
	        System.out.println("Other thread interrupted this shutdown, ignore for this case.");
	        Thread.currentThread().interrupt();
	    }
	}

}

public class ThreadConsumer2 {

	public static void main(String[] args) {
		String propPath = "kafka/consumer.properties";
		String topic = "test-topic";
		int workerNum = 5;

		ConsumerHandler consumers = new ConsumerHandler(topic, propPath);
		consumers.execute(workerNum);
		try {
		    Thread.sleep(1000000);
		} catch (InterruptedException ignored) {}
		consumers.shutdown();
	}
}
