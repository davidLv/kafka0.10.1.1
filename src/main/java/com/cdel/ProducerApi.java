package com.cdel;

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

	public static void main(String[] args) throws IOException {
		Properties properties = KafkaConfig.getProperties("kafka/producer.properties");
		//创建生产者
		Producer<String, String> producer = new KafkaProducer<>(properties);
		producer.send(
			new ProducerRecord<String, String>("topic1", "abc", "1111"),
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
