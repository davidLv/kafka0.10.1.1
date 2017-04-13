package com.cdel.producer;

import java.io.IOException;
import java.util.Properties;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.cdel.util.KafkaConfig;

//简单生产者
public class ProducerApi {

	//配置文件里的ack：
	//acks=1：只需要接收lead写入后，返回的ack
	//acks=0：不接收ack
	//acks=all：所有同步followers都写入，都要返回ack
	public static void main(String[] args) throws IOException {
		Properties properties = KafkaConfig.getProperties("kafka/producer.properties");
		//创建生产者
		Producer<String, String> producer = new KafkaProducer<>(properties);
		producer.send(
			new ProducerRecord<String, String>("qz", "abc", "4444"),
			new Callback() {
				@Override
				public void onCompletion(RecordMetadata metadata, Exception exception) {
					if (metadata != null) {
						System.out.println("kafka callback: meta -> " + metadata.toString());
					}
					if (exception != null) {
						System.out.println("kafka callback: error -> " + exception.toString());
					}
				}
			});
		producer.flush();
		producer.close();
	}

}
