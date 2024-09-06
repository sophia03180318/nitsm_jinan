package com.jcca.web.common.util;

import cn.hutool.core.util.ObjectUtil;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @description: rabbitMQ工具类
 * @author: sophia
 * @create: 2023/12/11 14:38
 **/
@Component
@Log4j
public class MQUtil {

    private static String host;
    private static Integer port;
    private static String username;
    private static String virtual;
    private static String password;

    @Value("${threeD.rabbitMQhost}")
    public void setHost(String host) {
        MQUtil.host = host;
    }

    @Value("${threeD.rabbitMQPort}")
    public void setPort(Integer port) {
        MQUtil.port = port;
    }

    @Value("${threeD.rabbitMQVirtual}")
    public void setVirtual(String virtual) {
        MQUtil.virtual = virtual;
    }

    @Value("${threeD.rabbitMQUsername}")
    public void setUsername(String username) {
        MQUtil.username = username;
    }

    @Value("${threeD.rabbitMQPassword}")
    public void setPassword(String password) {
        MQUtil.password = password;
    }


    private static Connection connection;
    private static Channel channel;

    public static Channel getChannel() {
        try {
            if (ObjectUtil.isNull(channel)) {
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(getHost());
                connectionFactory.setPort(getPort());
                connectionFactory.setUsername(getUsername());
                connectionFactory.setPassword(getPassword());
                connectionFactory.setVirtualHost(getVirtual());
                if (ObjectUtil.isNull(connection)){
                    connection = connectionFactory.newConnection();
                }
                channel = connection.createChannel();
            }
            return channel;
        } catch (Exception e) {
            log.error("获取RabbitMQ通道失败：" + e.getMessage());
            try {
                connection.close();
                channel.close();
            } catch (IOException ex) {
            } catch (TimeoutException ex) {
            }
            return null;
        }
    }


    public static Connection getConnection() {
        try {
            if (ObjectUtil.isNull(connection)) {
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(host);
                connectionFactory.setPort(port);
                connectionFactory.setUsername(username);
                connectionFactory.setPassword(password);
                connectionFactory.setVirtualHost(virtual);
                connection = connectionFactory.newConnection();
            }
            return connection;
        } catch (Exception e) {
            log.error("获取RabbitMQ连接失败：" + e.getMessage());
            return null;
        }
    }

    public static void closeChannelAndConnection() {
        try {
            if (ObjectUtil.isNotNull(channel)) {
                channel.close();
                channel = null;
            }
            if (ObjectUtil.isNotNull(connection)) {
                connection.close();
                connection = null;
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }


    public static String getHost() {
        return host;
    }

    public static Integer getPort() {
        return port;
    }

    public static String getUsername() {
        return username;
    }

    public static String getVirtual() {
        return virtual;
    }

    public static String getPassword() {
        return password;
    }


}