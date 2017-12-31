package org.helloworld;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.utils.ConfigUtils;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

/**
 * Created by Administrator on 2017/10/20.
 */
public class Producer {
    public final static String QUEUE_NAME="rabbitMQ.test";

    public static void main(String[] args) throws IOException, TimeoutException {
        Scanner sc = new Scanner(System.in);
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ相关信息
        factory.setHost(ConfigUtils.HOST_NAME);
        factory.setUsername(ConfigUtils.USER_NAME);
        factory.setPassword(ConfigUtils.PASSWORD);
        factory.setPort(5672);
        //创建一个新的连接
        Connection connection = factory.newConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //  声明一个队列        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        //String message = "Hello RabbitMQ";
        while(sc.hasNextLine()){
            String message = sc.nextLine();
            channel.queueDeclare( QUEUE_NAME , false, false, false, null) ;
            //发送消息到队列中
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes("UTF-8"));
            System.out.println("Producer Send +'" + message + "'");
        }

        //关闭通道和连接
        channel.close();
        connection.close();
    }


}
