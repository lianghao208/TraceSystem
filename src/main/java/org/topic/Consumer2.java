package org.topic;

import com.rabbitmq.client.*;
import org.utils.ConfigUtils;
import org.utils.ConnectionUtils;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * TODO 封装好整个连接功能
 * ROUTING_KEY 路由键: *（星号）可以代替一个任意标识符 ；#（井号）可以代替零个或多个标识符
 */
public class Consumer2 {
    private final static String QUEUE_NAME = "rabbitMQ.topic";
    public final static String EXCHANGE_NAME="rabbitMQ.topic";
    public final static String ROUTING_KEY="key.2";

    public static void main(String[] args) throws IOException, TimeoutException {
        /*// 创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置RabbitMQ地址
        factory.setHost("192.168.175.128");
        factory.setUsername("admin");
        factory.setPassword("admin");
        factory.setPort(5672);
        //创建一个新的连接
        Connection connection = factory.newConnection();*/
        Connection connection = ConnectionUtils
                .init(  ConfigUtils.HOST_NAME,
                        ConfigUtils.USER_NAME,
                        ConfigUtils.PASSWORD,
                        ConfigUtils.PORT)
                .getConnection();
        //创建一个通道
        Channel channel = connection.createChannel();
        //声明要关注的队列
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println("Consumer Waiting Received messages");

        //绑定队列到交换机，订阅key.后面任意字符的消息
        channel.queueBind(QUEUE_NAME,EXCHANGE_NAME,ROUTING_KEY);
        // 同一时刻服务器只会发一条消息给消费者
        //channel.basicQos(1);
        //DefaultConsumer类实现了Consumer接口，通过传入一个频道，
        // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
        com.rabbitmq.client.Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println("Consumer Received '" + message + "'");
            }
        };
        //自动回复队列应答 -- RabbitMQ中的消息确认机制
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }
}
