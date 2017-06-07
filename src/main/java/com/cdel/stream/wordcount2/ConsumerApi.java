package com.cdel.stream.wordcount2;

import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;

import com.cdel.util.KafkaConfig;

/**ConsumerApi和ConsumerApi2都是消费者，消费同一个主题，因为属于不同的组，所有每个组都可以消费一次。
 * Kafka 用Consumer Groups提供并发消费模型。同一Consumer Groups里的多个消费者共同从Kafka中消费记录。每个Consumer得到的所有内容的子集。Consumer Groups提供了弹性的消费分担模型。当某个Consumer挂掉或者新的Consumer加入，可以rebalence。举个栗子：

	场景
	1.某个Topic 有4个分区p0、p1、p3、p4；
	2.有两个消费者consumer1和consumer2，同属于一个Consumer* Groups（名字不重要啊）；

	效果
	1.初始状态
	  consumer1消费p0 和 p1;
	  consumer2消费p2和p3；
	2.consumer2 down掉后
	  consumer1消费p0、p1、p3、p4；
	3.consumer2活过来了，还来了个consumer3，可能的效果就是：
	  consumer1消费p0 和 p1;
	  consumer2消费p2;
	  consumer3消费p3；
 *
 * 自动提交demo（需要设置enable.auto.commit=true，offset自动提交到zk）
 */
public class ConsumerApi {

	public static void main(String[] args) throws IOException {
		Properties properties = KafkaConfig.getProperties("kafka/consumer4.properties");
		KafkaConsumer<byte[], Long> consumer = new KafkaConsumer<>(properties);
		// 消息处理
		consumer.subscribe(Arrays.asList("qz2"));
		//Kafka为分区内的每条记录分配一个Offset。该Offset作为记录在分区的唯一标识。也方面记录消费者在分区中的位置。例如某消费者的Position为5，意味着：
		//1. 已经消费0-4的记录；
		//2. 下次从记录5开始消费。
		//消费者获取数据的同时，会不停的确认已经消费的position。一旦发生错误或者重启，可以从该position继续处理。确认的方式有两种可选：
		//1. 自动确认；
		//2. 手工控制，通过commitSync/commitAsync 进行同步或异步的确认。
		while (true) {
			ConsumerRecords<byte[], Long> records = consumer.poll(100);
			for (ConsumerRecord<byte[], Long> record : records){
				System.out.printf("offset = %d, key = %s, value = %s", record.offset(), new String(record.key()), record.value());
				System.out.println("");
			}
		}
	}

}
