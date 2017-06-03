package com.cdel.stream.wordcount;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

public class MyProcessor extends AbstractProcessor<String, String> {

	private ProcessorContext context;
    private KeyValueStore<String, String> kvStore;

    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.context.schedule(1000);
        this.kvStore = (KeyValueStore<String, String>) context.getStateStore("Counts");
    }

    @Override
    public void process(String dummy, String line) {
        String[] words = line.toLowerCase().split(" ");

        for (String word : words) {
            String oldValue = this.kvStore.get(word);

            if (oldValue == null) {
                this.kvStore.put(word, "1");
            } else {
                this.kvStore.put(word, (Integer.parseInt(oldValue) + 1) + "");
            }
        }
    }

    @Override
    public void punctuate(long timestamp) {
        KeyValueIterator<String, String> iter = this.kvStore.all();

        while (iter.hasNext()) {
            KeyValue<String, String> entry = iter.next();
            context.forward(entry.key, entry.value);
        }

        iter.close();
        context.commit();
    }

}
