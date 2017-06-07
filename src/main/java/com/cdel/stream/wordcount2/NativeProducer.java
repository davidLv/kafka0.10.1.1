package com.cdel.stream.wordcount2;

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
        Properties properties = KafkaConfig.getProperties("kafka/producer.properties");
        Producer<String, String> producer = new KafkaProducer<>(properties);

        String[] str = {"Ming-chieh Pan and Sung-ting Tsai presented",
        		"about Mac OS X Rootkits (paper and slides). They describe some very cool",
        		"techniques to access kernel memory in different ways than the usual ones. ",
        		"The slides and paper aren’t very descriptive about all the techniques so",
        		"this weekend I decided to give it a try and replicate the described vulnerability",
        		"to access kernel memory."
        };

        for (int nEvents = 0; nEvents < str.length; nEvents++) {
        	String msg = str[nEvents];
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
