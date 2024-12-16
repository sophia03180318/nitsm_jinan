package com.jcca.web.test;

import com.jcca.web.alarm.entity.AlarmInfo;
import com.jcca.web.asset.entity.Asset;
import com.jcca.web.asset.service.AssetService;
import com.jcca.web.common.service.ThreeDService;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 测试3D机房
 *
 * @author sophia
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ThreeDTest {
    @Resource
    private ThreeDService threeDService;
    @Resource
    private AssetService assetService;


    @Test
    public void mqTest(){
        String s="测试消息";
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("192.168.51.104");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("root");
        connectionFactory.setPassword("1qaz");
        connectionFactory.setVirtualHost("/");
        Connection connection = null;
        try {
            connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            channel.basicPublish("dcim_3d", "dcim_3d", MessageProperties.PERSISTENT_TEXT_PLAIN, s.getBytes());
        } catch (IOException e) {
            System.out.println(e.toString());
        } catch (TimeoutException e) {
            System.out.println(e.toString());
        }

    }

    @Test
    public void syncAssetByRoom() {
        threeDService.syncAssetByRoom();
    }



    @Test
    public void pushAssetAdd() {
        Asset asset = assetService.getById("1680861863933648896");
        asset.setRoomId("4-110");
        asset.setCabinetId("JG_00000332");
        asset.setStartPosition(2);
        asset.setAssetImage("213");
        threeDService.pushAssetAdd(asset);

    }

    @Test
    public void pushAssetChange() {
        Asset asset = assetService.getById("1680861863933648896");
        asset.setRoomId("4-110");
        asset.setCabinetId("JG_00000332");
        asset.setStartPosition(23);
        asset.setAssetImage("213");
        threeDService.pushAssetChange(asset);

    }

    @Test
    public void pushAssetRemove() {
        threeDService.pushAssetRemove("1680861863933648896");
    }


    @Test
    public void pushAlarm() {
        threeDService.pushAlarm();
    }

    @Test
    public void cancelAlarm(){
        AlarmInfo alarmInfo = new AlarmInfo();
        alarmInfo.setId("1742818198937673728");
        alarmInfo.setAssetId("1742485418932445184");
        threeDService.cancelAlarm(alarmInfo);
    }

    @Test
    public void pushProperty() {
        threeDService.pushProperty();

    }



    @Test
    public void pushLink() {
        threeDService.pushLink();

    }
    @Test
    public void pro() throws IOException, TimeoutException {

        //创建连接工厂
        //创建连接mq的连接工厂对象
        ConnectionFactory factory = new ConnectionFactory();
        //设置连接rabbitmq主机
        factory.setHost("192.168.51.121");
        //设置端口号
        factory.setPort(5672);
        //设置访问虚拟主机的用户名和密码
        factory.setUsername("root");
        factory.setPassword("1qaz");
        //设置连接那个虚拟主机
        factory.setVirtualHost("/");

        //获取连接对象
        Connection connection = factory.newConnection();
        //获取连接中通道
        Channel channel = connection.createChannel();

        //通道绑定对应消息队列
        //参数1:  队列名称 如果队列不存在自动创建
        //参数2:  用来定义队列特性是否要持久化 true 持久化队列   false 不持久化
        //参数3:  exclusive 是否独占队列，表示声明的当前队列只允许当前的连接所使用  true 独占队列   false  不独占
        //参数4:  autoDelete: 是否在消费完成后自动删除队列  true 自动删除  false 不自动删除
        //参数5:  额外附加参数
        channel.queueDeclare("addAsset", true, false, false, null);

        //发布消息
        //参数1: 交换机名称 参数2:队列名称  参数3:传递息额外设置（MessageProperties.PERSISTENT_TEXT_PLAIN 设置消息持久化）  参数4:消息的具体内容
        channel.basicPublish("", "addAsset", MessageProperties.PERSISTENT_TEXT_PLAIN, "2222 rabbitmq".getBytes());

        //关闭连接
        channel.close();
        connection.close();
    }
}
