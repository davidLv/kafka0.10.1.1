package com.cdel.topic;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.security.JaasUtils;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.server.ConfigType;
import kafka.utils.ZkUtils;

public class TopicOpt {

	private static ZkUtils zkUtils = ZkUtils.apply("192.168.192.145:2181,192.168.192.146:2181,192.168.192.147:2181", 30000, 30000, JaasUtils.isZkSecurityEnabled());

	public static void createTopic(String topicName){
		// 创建一个单分区单副本名为t1的topic
		AdminUtils.createTopic(zkUtils, topicName, 1, 1, new Properties(), RackAwareMode.Enforced$.MODULE$);
	}

	public static void deleteTopic(String topicName){
		// 删除topic 'topicName'
		AdminUtils.deleteTopic(zkUtils, topicName);
	}

	public static void checkTopic(String topicName){
		// 获取topic 'test'的topic属性属性
		Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topicName);
		// 查询topic-level属性
		Iterator it = props.entrySet().iterator();
		while(it.hasNext()){
		    Map.Entry entry=(Map.Entry)it.next();
		    Object key = entry.getKey();
		    Object value = entry.getValue();
		    System.out.println(key + " = " + value);
		}
	}

	public static void modifyTopic(String topicName){
		Properties props = AdminUtils.fetchEntityConfig(zkUtils, ConfigType.Topic(), topicName);
		// 增加topic级别属性
		props.put("min.cleanable.dirty.ratio", "0.3");
		// 删除topic级别属性
		props.remove("max.message.bytes");
		// 修改topic 'test'的属性
		AdminUtils.changeTopicConfig(zkUtils, topicName, props);
	}

	public static void main(String[] args) {
		//TopicOpt.createTopic("testp12");
		//TopicOpt.deleteTopic("testp12");
		TopicOpt.checkTopic("testp11");
		System.out.println("----------------");
		TopicOpt.checkTopic("testp15");
		zkUtils.close();
	}

}
