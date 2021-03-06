package com.cdel.producer;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;

import com.cdel.util.KafkaConfig;

public class NativeProducer {

	public static void main(String[] args) throws IOException {
        long events = 10;
        Random rand = new Random();

        Properties properties = KafkaConfig.getProperties("kafka/producer.properties");
        Producer<String, String> producer = new KafkaProducer<>(properties);

        for (long nEvents = 0; nEvents < events; nEvents++) {
        	String msg = "NativeMessage-" + rand.nextInt() ;
        	producer.send(
    			new ProducerRecord<String, String>("qz", "abc", msg),
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
    			}
    		);
        }
        producer.flush();
        producer.close();
    }

}
