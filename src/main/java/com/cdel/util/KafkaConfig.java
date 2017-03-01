package com.cdel.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class KafkaConfig {
    private static Logger log = LoggerFactory.getLogger(KafkaConfig.class);

    public static Properties getProperties(String configFile) throws IOException {
        Properties properties;
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            InputStream input = classLoader.getResourceAsStream(configFile);
            properties = new Properties();
            properties.load(input);
            input.close();
        } catch (IOException e) {
            log.error("", e);
            throw e;
        }
        initConnection(properties);
        return properties;
    }

    /**
     * 用 context 中的内容 替换掉 ${}中的内容
     */
    private static void initConnection(Properties properties) {
        // TODO: 2016/7/5 连接信息可以从容器中Context中获取

    }

}
