package org.workqueue;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Created by Liang Hao on 2017/10/20.
 */
public class Producer {
    public final static String QUEUE_NAME="rabbitMQ.work.queue";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost("192.168.175.128");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setPort(5672);
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //  声明一个队列        channel.queueDeclare(QUEUE_NAME, false, false, false, null);

        for(int i=0;i<100;i++){
            String message = "Message "+i;
            channel.queueDeclare( QUEUE_NAME ,false,false,false, null) ;
            //发送消息到队列中
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println("Producer Send +'" + message + "'");
            Thread.sleep(i * 10);
        }

        //关闭通道和连接
        channel.close();
        connection.close();
    }


}
