package com.cdel.stream.wordcount;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.processor.AbstractProcessor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;

/**
 * Kafka streams提供2种方式来定义流处理器拓扑：Kafka Streams DSL提供了更常用的数据转换操作，如map和filter；
 * 低级别Processor API允许开发者定义和连接自定义的处理器，以及和状态仓库交互。
 * @author dell
 *
 */
public class MyProcessor extends AbstractProcessor<String, String> {

	private ProcessorContext context;
    private KeyValueStore<String, String> kvStore;

    //在init初始化方法中。processor可以保持当前的ProcessorContext实例变量，利用上下文来计划周期地（context().schedule）puncuation，
    //转发修改后的/新的键值对(key-value)到下游系统（context().forward），提交当前的处理进度（context().commit）,等。
    //在init方法，定义每1秒调度 punctuate ，并检索名为“Counts”的本地状态存储。
    @Override
    @SuppressWarnings("unchecked")
    public void init(ProcessorContext context) {
        this.context = context;
        this.context.schedule(1000);
        this.kvStore = (KeyValueStore<String, String>) context.getStateStore("Counts");
    }

    //process方法执行接收的消息；并根据时间进行周期性地执行punctuate方法。
    //在process方法中，每个接收一个记录，将字符串的值分割成单词，并更新他们的数量到状态存储。
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

    //在puncuate方法，迭代本地状态仓库并发送总量数到下游的处理器，并提交当前的流状态。
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
