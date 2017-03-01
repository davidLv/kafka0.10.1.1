package com.cdel;

import java.util.Map;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;

/**重写分区规则
 * @author dell
 *
 */
public class MyPartition implements Partitioner {

	@Override
	public void configure(Map<String, ?> map) {
		// TODO Auto-generated method stub

	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	/* topic：主题
	/* key：主题的key
	/* content：主题的内容
	 * 本例中如果key可以被解析为整数则将对应的整数与partition总数取余，该消息会被发送到该数对应的partition。（每个parition都会有个序号）
	 */
	@Override
	public int partition(String topic, Object key, byte[] abyte0, Object content,
			byte[] abyte1, Cluster cluster) {
		//强制返回第二个分区
		//return 2;
		int numPartitions = cluster.partitionCountForTopic(topic);
		try {
            int partitionNum = Integer.parseInt((String) key);
            return Math.abs(partitionNum % numPartitions);
        } catch (Exception e) {
        	int num = key.hashCode() % numPartitions;
            return num;
        }
	}

}
